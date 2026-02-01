package com.example.baris.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.baris.R
import com.example.baris.databinding.ActivityMainBinding
import com.example.baris.ui.model.ScanResult
import com.example.baris.viewmodel.ScanViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.*
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

        // Sutvarkome viršutinę juostą (status bar), kad nesimatytų "kiaurai"
        window.statusBarColor = Color.parseColor("#1A237E")

        binding.viewFinder.scaleType = PreviewView.ScaleType.FILL_CENTER
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Nustatome vėliavą ant mygtuko pagal tai, kokia kalba dabar nustatyta
        val currentLang = resources.configuration.locales[0].language
        binding.btnChangeLanguage.text = if (currentLang == "en") "🇬🇧" else "🇱🇹"

        viewModel.scanResult.observe(this) { result ->
            updateUI(result)
        }

        // Skenavimo mygtukas
        binding.btnScan.setOnClickListener {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), 10
                )
            }
        }

        // Kalbos keitimo mygtukas
        binding.btnChangeLanguage.setOnClickListener {
            showLanguageDialog()
        }

        binding.btnClear.setOnClickListener {
            binding.resultCard.visibility = View.INVISIBLE
            lastScannedCode = ""
        }

        binding.btnCloseApp.setOnClickListener {
            finishAffinity()
        }
    }

    // --- KALBOS KEITIMO LOGIKA ---

    private fun showLanguageDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_language, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<View>(R.id.lnLithuanian).setOnClickListener {
            setLocale("lt")
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.lnEnglish).setOnClickListener {
            setLocale("en")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)

        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        // Perkrauname Activity, kad pasikeistų visi tekstai ir vėliava
        recreate()
    }

    // --- KAMEROS IR SKENAVIMO LOGIKA ---

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
                Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show()
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
        binding.resultLayout.setBackgroundColor(Color.parseColor(result.backgroundColor))

        val originLabel = getString(R.string.origin)
        binding.txtCountry.text = "$originLabel: ${result.countryName}"
        binding.txtCountry.setTextColor(result.statusColor)

        binding.txtFlag.text = getFlagEmoji(result.countryName)

        binding.txtStatus.text = result.statusText
        binding.txtStatus.setTextColor(Color.BLACK)
        binding.txtStatus.textSize = 15f
    }

    private fun getFlagEmoji(countryName: String): String {
        return when {
            countryName.contains("Lietuva", true) || countryName.contains("Lithuania", true) -> "🇱🇹"
            countryName.contains("Lenkija", true) || countryName.contains("Poland", true) -> "🇵🇱"
            countryName.contains("Vokietija", true) || countryName.contains("Germany", true) -> "🇩🇪"
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