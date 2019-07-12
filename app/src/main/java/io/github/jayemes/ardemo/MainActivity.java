package io.github.jayemes.ardemo;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.ar.core.TrackingState.STOPPED;
import static com.google.ar.core.TrackingState.TRACKING;



public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    public static final String TAG = "Main Activity";
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        TextView versionTV = findViewById(R.id.versionTV);
        versionTV.setText(String.format("AR Demo %s", BuildConfig.VERSION_NAME));

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
                    AugmentedImageNode node = new AugmentedImageNode(this);
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
            }

        }

    }


}
