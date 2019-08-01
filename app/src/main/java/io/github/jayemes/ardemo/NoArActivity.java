package io.github.jayemes.ardemo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.Timer;
import java.util.concurrent.CompletableFuture;

public class NoArActivity extends AppCompatActivity {
    Timer timer;
    SceneView sceneView;
    Scene scene;
    AugmentedImageNode augNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_ar);

        sceneView = findViewById(R.id.scene_view);

        scene = sceneView.getScene();

        HeatedTankODE tankODE = new HeatedTankODE(0.07d, 10, 10, 2000, 1, 0.1d, 0.015d);

        timer = new Timer();
        timer.schedule(new ArActivity.ODETask(tankODE), 100, 100);

        Vector3 camVector = new Vector3(0.0f, 0.3f, -0.3f);
        Quaternion camRotation = Quaternion.multiply(
                Quaternion.axisAngle(Vector3.up(), 180),
                Quaternion.axisAngle(Vector3.right(), -10));

        Camera camera = scene.getCamera();
        camera.setLocalPosition(camVector);
        camera.setLocalRotation(camRotation);

        Context context = this;

        augNode = new AugmentedImageNode(context, tankODE);

        augNode.setParent(scene);
        augNode.setImage(null);

    }

    @Override
    protected void onPause() {
        Log.e("Pause", "Pause");
        super.onPause();
        sceneView.pause();
//        augNode.menuCF = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            sceneView.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.purge();
        timer.cancel();

    }
}
