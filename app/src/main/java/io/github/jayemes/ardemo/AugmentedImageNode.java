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
    private static CompletableFuture<ModelRenderable> model;
    private static CompletableFuture<ViewRenderable> menu;

    private Node modelNode;

    AugmentedImageNode(Context context) {
        if (model == null) {
            model = ModelRenderable.builder()
                    .setSource(context, R.raw.tmp)
                    .build();
        }


        View menuView = View.inflate(context, R.layout.floating_menu, null);

        SeekBar levelBar = menuView.findViewById(R.id.levelSeekBar);
        SeekBar tempBar = menuView.findViewById(R.id.tempSeekBar);

        levelBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (modelNode != null) {
                    modelNode.setLocalScale(new Vector3(.5f, i / 100f, .5f));
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
                Color newColor = new Color(i/100f, .5f - i/200f, .5f - i / 200f);

                CompletableFuture<Material> materialCompletableFuture =
                        MaterialFactory.makeOpaqueWithColor(context, newColor);

                materialCompletableFuture.thenAccept(material -> {
                    modelNode.getRenderable().setMaterial(material);
                });


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        if (menu == null) {
            menu = ViewRenderable.builder()
                    .setView(context, menuView)
                    .build();
        }
    }

    void setImage(AugmentedImage image) {
        this.image = image;

        if (!model.isDone() || !menu.isDone()) {
            CompletableFuture.allOf(model, menu)
                    .thenAccept(aVoid -> setImage(image))
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Exception loading models", throwable);
                        return null;
                    });
        }


        setAnchor(image.createAnchor(image.getCenterPose()));

        modelNode = new Node();
        modelNode.setParent(this);
        modelNode.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));
        modelNode.setLocalPosition(new Vector3(0, 0, 0));
        modelNode.setRenderable(model.getNow(null));

        Node menuNode = new Node();
        menuNode.setParent(this);
        menuNode.setLocalPosition(new Vector3(1, 0.3f, 0));
        menuNode.setLocalRotation(Quaternion.axisAngle(new Vector3(1, 0, 0), -90));
        menuNode.setRenderable(menu.getNow(null));
    }

}
