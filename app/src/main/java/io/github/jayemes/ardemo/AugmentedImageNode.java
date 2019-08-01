package io.github.jayemes.ardemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.concurrent.CompletableFuture;

class AugmentedImageNode extends AnchorNode {

    private static final String TAG = "AugmentedImageNode";
    private static final float OBJECTS_SCALE = 1.2f;

    private AugmentedImage image;
    private static CompletableFuture<ModelRenderable> tankCF, waterCF, handleCF;
    protected static CompletableFuture<ViewRenderable> menuCF;

    private ValueAnimator handleAnimator;
    private Node tankNode, waterNode, handleNode, baseNode, menuNode;
    private HeatedTankODE tankODE;
    private Context context;

    AugmentedImageNode(Context context, HeatedTankODE tankOD) {
        this.tankODE = tankOD;
        this.context = context;

        View menuView = View.inflate(context, R.layout.floating_menu, null);

        handleAnimator = ValueAnimator.ofInt(1, 180);
        handleAnimator.setDuration(1000);

        SeekBar levelBar = menuView.findViewById(R.id.levelSeekBar);
        SeekBar tempBar = menuView.findViewById(R.id.tempSeekBar);

        levelBar.setOnSeekBarChangeListener(new LevelBarChangeListener());
        tempBar.setOnSeekBarChangeListener(new TempBarChangeListener());


        if (tankCF == null) {
            tankCF = ModelRenderable.builder()
                    .setSource(context, R.raw.new_tank)
                    .build();
            waterCF = ModelRenderable.builder()
                    .setSource(context, R.raw.new_water)
                    .build();
            handleCF = ModelRenderable.builder()
                    .setSource(context, R.raw.new_handle)
                    .build();
            menuCF = ViewRenderable.builder()
                    .setView(context, menuView)
                    .build();
        } else {
            menuCF = ViewRenderable.builder()
                    .setView(context, menuView)
                    .build();
        }

    }

    void setImage(@Nullable AugmentedImage image) {
        this.image = image;

        if (!tankCF.isDone() || !menuCF.isDone() || !waterCF.isDone() || !handleCF.isDone()) {
            CompletableFuture.allOf(tankCF, menuCF, waterCF, handleCF)
                    .thenAccept((Void v) -> setImage(image))
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Exception loading models", throwable);
                        return null;
                    });
        }

        if (image != null) {
            setAnchor(image.createAnchor(image.getCenterPose()));
        }

        Vector3 scaleVector = Vector3.one().scaled(OBJECTS_SCALE);

        Log.e("setImage()", "Dentro de setImage");

        for (Node node : getChildren()) {
            removeChild(node);
        }

        baseNode = new Node();
        baseNode.setName("BASE");
        baseNode.setParent(this);
        baseNode.setLocalPosition(Vector3.zero());

        tankNode = new Node();
        tankNode.setParent(baseNode);
        tankNode.setLocalPosition(Vector3.zero());
        tankNode.setRenderable(tankCF.getNow(null));

        waterNode = new Node();
        waterNode.setParent(baseNode);
        waterNode.setLocalPosition(new Vector3(0, 0.02f, 0));
        waterNode.setRenderable(waterCF.getNow(null));

        handleNode = new Node();
        handleNode.setParent(baseNode);
        handleNode.setLocalPosition(new Vector3(-0.1500f, 0.0494f, -0.0290f));
        handleNode.setRenderable(handleCF.getNow(null));

        menuNode = new Node();
        menuNode.setParent(baseNode);
        menuNode.setLocalPosition(new Vector3(0.3f, 0.2f, 0));
        menuNode.setLocalRotation(Quaternion.axisAngle(Vector3.up(), 180));
        menuNode.setLocalScale(Vector3.one().scaled(.8f));
        menuNode.setRenderable(menuCF.getNow(null));

    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);

        if (waterNode != null) {
            waterNode.setLocalScale(new Vector3(
                    1,
                    (float) (tankODE.getH() / 5f * 1),
                    1));

            float temp = (float) tankODE.getTOut();

            int newColor = interpolateColor(0x00ffff, 0xff0000, (temp - 10f) / 70f);

            if (waterNode.getRenderable() != null) {
                CompletableFuture<Material> materialCompletableFuture =
                        MaterialFactory.makeOpaqueWithColor(context,
                                new com.google.ar.sceneform.rendering.Color(newColor));

                materialCompletableFuture.thenAccept(
                        mat -> waterNode.getRenderable().setMaterial(mat));
            }
        }

        if (handleNode != null) {
            if (handleAnimator.isRunning()) {
                handleNode.setLocalRotation(Quaternion.axisAngle(
                        Vector3.back(),
                        (int) handleAnimator.getAnimatedValue()));
            }
        }

    }


    private float interpolate(float a, float b, float proportion) {
        return (a + ((b - a) * proportion));
    }

    private int interpolateColor(int a, int b, float proportion) {
        float[] hsva = new float[3];
        float[] hsvb = new float[3];

        Color.colorToHSV(a, hsva);
        Color.colorToHSV(b, hsvb);
        for (int i = 0; i < 3; i++) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
        }
        return Color.HSVToColor(hsvb);
    }

    private class LevelBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            tankODE.setkValve(i / 50f);
            if (!handleAnimator.isRunning()) {
                handleAnimator.start();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private class TempBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            tankODE.setQDot(i * 200);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}
