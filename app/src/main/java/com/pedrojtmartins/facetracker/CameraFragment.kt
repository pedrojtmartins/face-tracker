package com.pedrojtmartins.facetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.pedrojtmartins.facetracker.FaceAnalyzer.FaceAnalyzerResult
import com.pedrojtmartins.facetracker.databinding.FragmentCameraBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_camera.*
import java.util.concurrent.Executors.newSingleThreadExecutor

// Permissions
private const val REQUEST_CODE_PERMISSIONS = 1
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

// Camera config
private val DEFAULT_CAMERA_SELECTOR = CameraSelector.DEFAULT_FRONT_CAMERA

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            showPermissionsRequest()
        }
    }

    // Camera setup
    private fun startCamera(cameraSelector: CameraSelector = DEFAULT_CAMERA_SELECTOR) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                with(cameraProviderFuture.get()) {
                    unbindAll()
                    bindToLifecycle(
                            this@CameraFragment,
                            cameraSelector,
                            buildCameraPreview(),
                            buildFaceUseCase()
                    )
                }
            } catch (exc: Exception) {
                Log.e(CameraFragment::class.simpleName, "Use case binding failed. $exc")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun buildCameraPreview() = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

    private fun buildFaceUseCase() = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(newSingleThreadExecutor(), FaceAnalyzer(FaceAnalyserListener()))
            }

    // Permissions
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startCamera()
        } else {
            // TODO Improve permissions handling
            requireActivity().finish()
        }
    }

    private fun showPermissionsRequest() {
        // TODO Improve permissions handling
        requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private inner class FaceAnalyserListener : FaceAnalyzer.Listener {

        override fun onFaceAnalyzerResult(result: FaceAnalyzerResult) {
            // TODO Handle face
        }

        override fun onFaceAnalyserFailure(exception: Exception) {
            // TODO Handle failure
        }
    }
}