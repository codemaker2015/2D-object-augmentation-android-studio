package com.example.arcorefirst;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.util.Linkify;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    ArFragment arFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this))
            return;

        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.setOnTapArPlaneListener(
                (HitResult hitresult, Plane plane, MotionEvent motionevent) -> {
                    if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING)
                        return;

                    Anchor anchor = hitresult.createAnchor();
                    placeObject(arFragment, anchor, R.layout.layout_bg);
                }
        );
    }

    private void placeObject(ArFragment arFragment, Anchor anchor, int uri) {
        ViewRenderable.builder()
            .setView(this, uri)
            .build()
            .thenAccept(viewRenderable -> addNodeToScene(arFragment, anchor, viewRenderable))
            .exceptionally(throwable -> {
                    Toast.makeText(arFragment.getContext(), "Error:" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    return null;
                }
            );
    }

    private void addNodeToScene(ArFragment arFragment, Anchor anchor, ViewRenderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
        setNodeData(renderable);
    }

    private void setNodeData(ViewRenderable viewRenderable){
        View view = viewRenderable.getView();
        ImageView imgView = view.findViewById(R.id.img_view);
        TextView txtTitle = view.findViewById(R.id.txt_title);
        TextView txtGreeting = view.findViewById(R.id.txt_greeting);
        TextView txtInfo = view.findViewById(R.id.txt_info);

        imgView.setImageResource(R.mipmap.logo);
        txtTitle.setText("ViewRenderable Demo");
        txtGreeting.setText("Experience AR in Android Studio");
        txtInfo.setText("For more: \nhttps://developers.google.com/sceneform/reference/com/google/ar/sceneform/rendering/ViewRenderable");
        Linkify.addLinks(txtInfo, Linkify.WEB_URLS);
    }

    private boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
}
