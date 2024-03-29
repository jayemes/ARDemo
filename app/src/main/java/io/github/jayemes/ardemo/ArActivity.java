package io.github.jayemes.ardemo;

import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.ar.core.TrackingState.STOPPED;
import static com.google.ar.core.TrackingState.TRACKING;


public class ArActivity extends AppCompatActivity {

    private ArFragment arFragment;
    public static final String TAG = "Main Activity";
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

    HeatedTankODE tankODE;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);


        checkArAvailability();

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        Button runButton = findViewById(R.id.run_button);

        tankODE = new HeatedTankODE(0.07d, 10, 10, 2000, 1, 0.1d, 0.015d);

        timer = new Timer();
        runButton.setOnClickListener(view -> timer.schedule(new ODETask(tankODE), 100, 100));
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.purge();
        timer.cancel();
    }

    void checkArAvailability() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(this::checkArAvailability, 200);
        }
        if (!availability.isSupported()) {
            this.onBackPressed();
        }
    }


    private void onUpdateFrame(FrameTime ft) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        if (frame == null) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            if (augmentedImage.getTrackingState() == TRACKING) {
                if (!augmentedImageMap.containsKey(augmentedImage)) {
                    AugmentedImageNode node = new AugmentedImageNode(this, tankODE);
                    node.setImage(augmentedImage);
                    augmentedImageMap.put(augmentedImage, node);
                    arFragment.getArSceneView().getScene().addChild(node);
                    Log.e(TAG, "Added Node");
                }
            } else if (augmentedImage.getTrackingState() == STOPPED) {
                augmentedImageMap.remove(augmentedImage);
                Log.e(TAG, "Removed Node");
            } else {
                Log.e(TAG, "Paused");
                Toast.makeText(this, "Detected (Paused)", Toast.LENGTH_SHORT).show();
            }
        }
    }


    static class ODETask extends TimerTask {
        private HeatedTankODE tankODE;
        private int counter = 1;

        public ODETask(HeatedTankODE tankODE) {
            super();
            this.tankODE = tankODE;
        }

        @Override
        public void run() {
            counter++;
            tankODE.run();
        }
    }
}
