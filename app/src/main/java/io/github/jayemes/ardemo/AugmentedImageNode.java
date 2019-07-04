package io.github.jayemes.ardemo;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.concurrent.CompletableFuture;

class AugmentedImageNode extends AnchorNode {

    private static final String TAG = "AugmentedImageNode";
    private AugmentedImage image;
    private static CompletableFuture<ModelRenderable> model;

    AugmentedImageNode(Context context) {
        if (model == null) {
            model = ModelRenderable.builder()
                    .setSource(context, R.raw.tmp)
                    .build();
        }
    }

    void setImage(AugmentedImage image) {
        this.image = image;

        if (!model.isDone()) {
            CompletableFuture.allOf(model)
                    .thenAccept(aVoid -> setImage(image))
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Exception", throwable);
                        return null;
                    });
        }

        setAnchor(image.createAnchor(image.getCenterPose()));

        Vector3 localPosition = new Vector3();
        Node node = new Node();
        node.setLocalScale(new Vector3(0.1f, 2, 0.1f));

        localPosition.set(0, 0, 0);

        node.setParent(this);
        node.setLocalPosition(localPosition);
        node.setRenderable(model.getNow(null));
    }

}
