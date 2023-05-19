package com.magicleap.a3dmodelthumbnail;

import static android.graphics.Color.LTGRAY;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String[] models = {"models/dragon.glb", "models/cube.glb", "models/DragonAttenuation.glb", "models/Gumball.glb", "models/iphone_14_pro.glb", "models/DamagedHelmet.glb", "models/Hair.glb", "models/ML2-Controller.glb", "models/ML2-Compute-Pack.glb", "models/teapot.glb", "models/sculpture.glb", "models/violin.glb", "models/ML2-Headset.glb", "models/ML2-Headset.gltf", "models/ML2-Headset.mtl", "models/ML2-Headset.obj", "models/ML2-Headset.usdc"};
    private SceneView sceneView;
    private TextView infoTextView;
    private CheckBox checkboxView;
    private long startLoadingTime;
    private Spinner modelSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button thumbnailButton = findViewById(R.id.button);
        sceneView = findViewById(R.id.scene_view);
        sceneView.getRenderer().setClearColor(new Color(LTGRAY));
        infoTextView = findViewById(R.id.infoTextView);
        checkboxView = findViewById(R.id.checkbox);
        thumbnailButton.setOnClickListener(v -> {
            // Load the GLB file from the assets directory
            generateThumbnailFromModel();
        });
        Button clearButton = findViewById(R.id.clearbutton);
        clearButton.setOnClickListener(v -> {
            removeAllNodes();
            infoTextView.setText("");
        });
        modelSpinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, models);
        modelSpinner.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            sceneView.resume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sceneView.pause();
    }

    private void generateThumbnailFromModel() {
        boolean usingBackground = checkboxView.isChecked();
        String modelName = modelSpinner.getSelectedItem().toString();
        startLoadingTime = System.currentTimeMillis();
        CompletableFuture<ModelRenderable> objModel = ModelRenderable
                .builder()
                .setSource(this
                        , Uri.parse(modelName))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build();

        if (usingBackground) {

            CompletableFuture<ModelRenderable> backdrop = ModelRenderable
                    .builder()
                    .setSource(this
                            , Uri.parse("models/backdrop.glb"))
                    .setIsFilamentGltf(true)
                    .setAsyncLoadEnabled(true)
                    .build();

            CompletableFuture.allOf(objModel, backdrop)
                    .handle((ok, ex) -> {
                        try {
//                            removeAllNodes();
                            Node modelNode1 = new Node();
                            modelNode1.setRenderable(objModel.get());
                            modelNode1.setLocalScale(new Vector3(0.33f, 0.3f, 0.33f));
                            modelNode1.setLocalRotation(Quaternion.multiply(
                                    Quaternion.axisAngle(new Vector3(1f, 0f, 0f), 45),
                                    Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0)));
                            modelNode1.setLocalPosition(new Vector3(0f, 0f, -1.0f));
                            sceneView.getScene().addChild(modelNode1);

                            Node modelNode2 = new Node();
                            modelNode2.setRenderable(backdrop.get());
                            modelNode2.setLocalScale(new Vector3(0.3f, 0.3f, 0.3f));
                            modelNode2.setLocalRotation(Quaternion.multiply(
                                    Quaternion.axisAngle(new Vector3(1f, 0f, 0f), 45),
                                    Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 75)));
                            modelNode2.setLocalPosition(new Vector3(0f, 0f, -1.0f));
                            sceneView.getScene().addChild(modelNode2);

                            infoTextView.setText("Time Spent: " + (System.currentTimeMillis() - startLoadingTime) + " millisecs");

                        } catch (InterruptedException | ExecutionException ignore) {

                        }
                        return null;
                    });
        } else {
            CompletableFuture.allOf(objModel)
                    .handle((ok, ex) -> {
                        try {
//                            removeAllNodes();
                            Node modelNode1 = new Node();
                            modelNode1.setRenderable(objModel.get());
                            modelNode1.setWorldScale(new Vector3(4f, 4f, 4f));
                            modelNode1.setLocalRotation(Quaternion.multiply(
                                    Quaternion.axisAngle(new Vector3(1f, 0f, 0f), 45),
                                    Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0)));
                            modelNode1.setLocalPosition(new Vector3(0f, 0f, -1.0f));
                            sceneView.getScene().addChild(modelNode1);

                            infoTextView.setText("Time Spent: " + (System.currentTimeMillis() - startLoadingTime) + " millisecs");
                        } catch (InterruptedException | ExecutionException ignore) {
                            infoTextView.setText(ignore.getLocalizedMessage());
                        }
                        return null;
                    });
        }
    }

    private void removeAllNodes() {
        List<Node> children = new ArrayList<>(sceneView.getScene().getChildren());
        for (Node node : children) {
            if (node instanceof AnchorNode) {
                if (((AnchorNode) node).getAnchor() != null) {
                    ((AnchorNode) node).getAnchor().detach();
                }
            }
            if (!(node instanceof Camera)) {
                node.setParent(null);
            }
        }
    }
}