package com.pedrojtmartins.facetracker

import android.annotation.SuppressLint
import android.graphics.Rect
import android.media.Image
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.math.roundToInt

class FaceAnalyzer(private val listener: Listener) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .build()
    )

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            detector.process(InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees))
                    .addOnSuccessListener { listener.onFaceAnalyzerResult(processFaces(image, it)) }
                    .addOnFailureListener { listener.onFaceAnalyserFailure(it) }
                    .addOnCompleteListener { imageProxy.close() }
        }
    }

    private fun processFaces(image: Image, faces: List<Face>): FaceAnalyzerResult {
        val capturedImageSize = Size(image.width, image.height)
        val processedFaces = faces.map { face ->
            val facePosition = calculateFacePosition(face.boundingBox, image)
            FaceResult(face.trackingId, facePosition)
        }
        return FaceAnalyzerResult(capturedImageSize, processedFaces)
    }

    private fun calculateFacePosition(boundingBox: Rect, image: Image) = boundingBox.apply {
        val xCenter = image.height - boundingBox.centerX()
        val yCenter = boundingBox.centerY()
        val xOffset = (boundingBox.width() / 2.0f).roundToInt()
        val yOffset = (boundingBox.height() / 2.0f.roundToInt())

        left = xCenter - xOffset
        top = yCenter - yOffset
        right = xCenter + xOffset
        bottom = yCenter + yOffset
    }

    data class FaceAnalyzerResult(
            val capturedImageSize: Size,
            val faces: List<FaceResult>
    )

    data class FaceResult(
            val trackingId: Int?,
            val facePosition: Rect
    )

    interface Listener {

        fun onFaceAnalyzerResult(result: FaceAnalyzerResult)
        fun onFaceAnalyserFailure(exception: Exception)
    }
}