package com.samupb.arappgoogle

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position

data class Model(
    val fileLocation: String,
    val scaleUnits: Float? = null,
    val placementMode: PlacementMode = PlacementMode.BEST_AVAILABLE,
    val applyPoseRotation: Boolean = true
)

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var statusText: TextView
    var modelAdded = false
    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    var modelNode: ArModelNode? = null

    val model = Model("converser.glb")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        sceneView = findViewById<ArSceneView?>(R.id.sceneView).apply {
            onArTrackingFailureChanged = { reason ->
                statusText.text = reason?.getDescription(context)
                // statusText.isGone = reason == null
            }
        }
        loadingView = findViewById(R.id.loadingView)
        sceneView.onTapAr = { hitResult, motionEvent ->
            if (!modelAdded) {
                newModelNode()
                placeModelNode()
            }
        }
    }

    fun placeModelNode() {
        modelNode?.anchor()
        sceneView.planeRenderer.isVisible = false
    }

    @SuppressLint("SetTextI18n")
    fun newModelNode() {
        isLoading = true
        modelNode?.takeIf { !it.isAnchored }?.let {
            sceneView.removeChild(it)
            it.destroy()
        }

        modelNode = ArModelNode(model.placementMode).apply {
            applyPoseRotation = model.applyPoseRotation
            loadModelGlbAsync(
                glbFileLocation = model.fileLocation,
                autoAnimate = true,
                scaleToUnits = model.scaleUnits,
                // Place the model origin at the bottom center
                centerOrigin = Position(y = -1.0f)
            ) {
                sceneView.planeRenderer.isVisible = true
                isLoading = false
            }
            onAnchorChanged = { anchor ->
                statusText.text = "onHitResult  placeModelButton.isGone = $anchor != null"

            }
            onHitResult = { node, _ ->
                statusText.text = "onHitResult  placeModelButton.isGone = ${!node.isTracking}"
            }
        }
        sceneView.addChild(modelNode!!)
        // Select the model node by default (the model node is also selected on tap)
        sceneView.selectedNode = modelNode
    }
}