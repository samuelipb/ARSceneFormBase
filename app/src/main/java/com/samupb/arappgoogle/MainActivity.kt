package com.samupb.arappgoogle

import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.gorisse.thomas.sceneform.scene.await
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private val arSceneView get() = arFragment.arSceneView
    private val scene get() = arSceneView.scene

    private var model: Renderable? = null
    private var modelView: ViewRenderable? = null
    var modelAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Load model.glb from assets folder or http url
        arFragment = (supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment).apply {
            setOnSessionConfigurationListener { session, config ->
                // Modify the AR session configuration here
            }
            setOnViewCreatedListener { arSceneView ->
                arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)
            }
            setOnTapArPlaneListener(::onTapPlane)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadModels()
            }

        }

    }

    private suspend fun loadModels() {
        model = ModelRenderable.builder()
            .setSource(this, Uri.parse("spiderbot.glb"))
            .setIsFilamentGltf(true)
            .await()
        modelView = ViewRenderable.builder()
            .setView(this, R.layout.view_renderable_infos)
            .await()
    }

    private fun onTapPlane(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
        if (model == null || modelView == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show()
            return
        }
        if (!modelAdded) {
            modelAdded = true
            // Create the Anchor.
            scene.addChild(AnchorNode(hitResult.createAnchor()).apply {
                // Create the transformable model and add it to the anchor.
                addChild(TransformableNode(arFragment.transformationSystem).apply {
                    renderable = model
                    renderableInstance.setCulling(false)
                    renderableInstance.animate(true).start()
                    scaleController.minScale = 0.1f
                    scaleController.maxScale = 1.3f
                    // Add the View
                    addChild(Node().apply {
                        // Define the relative position
                        localPosition = Vector3(0.0f, 1f, 0.0f)
                        localScale = Vector3(0.7f, 0.7f, 0.7f)
                        renderable = modelView
                    })
                })
            })
        }
    }
}