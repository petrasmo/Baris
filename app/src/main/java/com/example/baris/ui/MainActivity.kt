package com.example.baris.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.baris.databinding.ActivityMainBinding
import com.example.baris.ui.model.ScanResult
import com.example.baris.viewmodel.ScanViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ScanViewModel by viewModels()
    private lateinit var cameraExecutor: ExecutorService
    private var lastScannedCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewFinder.scaleType = PreviewView.ScaleType.FILL_CENTER

        cameraExecutor = Executors.newSingleThreadExecutor()

        viewModel.scanResult.observe(this) { result ->
            updateUI(result)
        }

        binding.btnScan.setOnClickListener {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), 10
                )
            }
        }

        binding.btnClear.setOnClickListener {
            binding.resultCard.visibility = View.INVISIBLE
            lastScannedCode = ""
        }

        // Programėlės uždarymo mygtukas
        binding.btnCloseApp.setOnClickListener {
            finishAffinity()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Toast.makeText(this, "Klaida paleidžiant kamerą", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val code = barcode.rawValue ?: ""
                        if (code.isNotEmpty()) {
                            runOnUiThread { viewResult(code) }
                        }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    private fun viewResult(barcode: String) {
        if (barcode == lastScannedCode) return
        lastScannedCode = barcode
        viewModel.processBarcode(barcode)
    }

    private fun updateUI(result: ScanResult) {
        binding.resultCard.visibility = View.VISIBLE
        binding.resultLayout.setBackgroundColor(Color.WHITE)

        val isLithuania = result.countryName.contains("Lietuva", ignoreCase = true)

        if (isLithuania) {
            binding.txtStatus.text = "PUIKU! Prekė lietuviška"
            binding.txtStatus.textSize = 30f
            binding.txtStatus.setTextColor(Color.parseColor("#2E7D32"))
            binding.txtFlag.text = "🇱🇹"
            binding.txtFlag.textSize = 90f
        } else {
            binding.txtStatus.text = "Kilmė: ${result.countryName}"
            binding.txtStatus.textSize = 20f
            binding.txtStatus.setTextColor(Color.BLACK)
            binding.txtFlag.text = getFlagEmoji(result.countryName)
            binding.txtFlag.textSize = 45f
        }

        binding.txtCountry.text = "Barkodas: ${result.statusText}"
    }

    fun getFlagEmoji(countryName: String): String {
        return when {
            countryName.contains("Lietuva", true) -> "🇱🇹"
            countryName.contains("Lenkija", true) -> "🇵🇱"
            countryName.contains("Vokietija", true) -> "🇩🇪"
            countryName.contains("Latvija", true) -> "🇱🇻"
            countryName.contains("Estija", true) -> "🇪🇪"
            countryName.contains("Ukraina", true) -> "🇺🇦"
            else -> "🌍"
        }
    }

    private fun allPermissionsGranted() = arrayOf(Manifest.permission.CAMERA).all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}