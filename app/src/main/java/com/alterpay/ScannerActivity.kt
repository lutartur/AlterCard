package com.alterpay

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.alterpay.databinding.ActivityScannerBinding
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                //.setTargetResolution(Size(1280, 720)) // You can set target resolution if needed
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                        // When barcode is detected, stop the camera and return the result
                        cameraExecutor.shutdown()
                        val intent = Intent().apply {
                            putExtra(EXTRA_BARCODE_DATA, barcode.rawValue)
                            putExtra(EXTRA_BARCODE_FORMAT, barcode.format.toString())
                        }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    })
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private class BarcodeAnalyzer(private val listener: (barcode: Barcode) -> Unit) : ImageAnalysis.Analyzer {
        private val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().build())

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: androidx.camera.core.ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            // We only need one barcode, so take the first one
                            listener(barcodes[0])
                        }
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Barcode scanning failed", it)
                    }
                    .addOnCompleteListener {
                        imageProxy.close() // ALWAYS close the imageProxy
                    }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
    }

    companion object {
        private const val TAG = "ScannerActivity"
        const val EXTRA_BARCODE_DATA = "com.alterpay.scanner.BARCODE_DATA"
        const val EXTRA_BARCODE_FORMAT = "com.alterpay.scanner.BARCODE_FORMAT"
    }
}
