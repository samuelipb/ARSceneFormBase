package com.samupb.arappgoogle

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {

    var arFragment = ArFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            val anchor = hitResult.createAnchor()
            ModelRenderable.builder()
                .setSource(this, Uri.parse("Trainers_01.sfb"))
                .build()
                .thenAccept { modelRenderable -> addModelToScene(anchor, modelRenderable) }
                .exceptionally { throwable ->
                    println("errorAr: ${throwable.message}")
                    val toast = android.widget.Toast.makeText(this, "Error", android.widget.Toast.LENGTH_SHORT)
                    toast.show()
                    null
                }
        }
    }

    private fun addModelToScene(anchor: Anchor?, modelRenderable: ModelRenderable?) {
        val anchorNode = AnchorNode(anchor)
        val transformableNode = TransformableNode(arFragment.transformationSystem)
        transformableNode.setParent(anchorNode)
        transformableNode.renderable = modelRenderable
        arFragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }
}