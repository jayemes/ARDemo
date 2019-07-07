package io.github.jayemes.ardemo;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.concurrent.CompletableFuture;

class AugmentedImageNode extends AnchorNode {

    private static final String TAG = "AugmentedImageNode";
    private AugmentedImage image;
    private static CompletableFuture<ModelRenderable> tankCF, waterCF;
    private static CompletableFuture<ViewRenderable> menuCF;

    private Node tankNode, waterNode;

    AugmentedImageNode(Context context) {
        if (tankCF == null) {
            tankCF = ModelRenderable.builder()
                    .setSource(context, R.raw.tank)
                    .build();
        }

        if (waterCF == null) {
            waterCF = ModelRenderable.builder()
                    .setSource(context, R.raw.water)
                    .build();
        }

        View menuView = View.inflate(context, R.layout.floating_menu, null);

        SeekBar levelBar = menuView.findViewById(R.id.levelSeekBar);
        SeekBar tempBar = menuView.findViewById(R.id.tempSeekBar);

        levelBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (waterNode != null) {
                    waterNode.setLocalScale(new Vector3(.5f, i / 100f, .5f));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tempBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Color newColor = new Color(i / 100f, .5f - i / 200f, .5f - i / 200f);

                CompletableFuture<Material> materialCompletableFuture =
                        MaterialFactory.makeOpaqueWithColor(context, newColor);

                materialCompletableFuture.thenAccept(material -> {
                    waterNode.getRenderable().setMaterial(material);
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (menuCF == null) {
            menuCF = ViewRenderable.builder()
                    .setView(context, menuView)
                    .build();
        }
    }

    void setImage(AugmentedImage image) {
        this.image = image;

        if (!tankCF.isDone() || !menuCF.isDone() || !waterCF.isDone()) {
            CompletableFuture.allOf(tankCF, menuCF, waterCF)
                    .thenAccept(aVoid -> setImage(image))
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Exception loading models", throwable);
                        return null;
                    });
        }


        setAnchor(image.createAnchor(image.getCenterPose()));

        tankNode = new Node();
        tankNode.setParent(this);
        tankNode.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));
        tankNode.setLocalPosition(new Vector3(0, 0, 0));
        tankNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1, 0), 180));
        tankNode.setRenderable(tankCF.getNow(null));

        waterNode = new Node();
        waterNode.setParent(this);
        waterNode.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));
        waterNode.setLocalPosition(new Vector3(0, 0, 0));
        waterNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1, 0), 180));
        waterNode.setRenderable(waterCF.getNow(null));

        Node menuNode = new Node();
        menuNode.setParent(this);
        menuNode.setLocalPosition(new Vector3(0.5f, 0.2f, 0));
        menuNode.setLocalRotation(Quaternion.axisAngle(new Vector3(1, 0, 0), 0));
        menuNode.setRenderable(menuCF.getNow(null));
    }

}
