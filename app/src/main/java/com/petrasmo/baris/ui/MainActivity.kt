package com.petrasmo.baris.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.petrasmo.baris.databinding.ActivityMainBinding
import com.petrasmo.baris.ui.model.ScanResult
import com.petrasmo.baris.viewmodel.ScanViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.petrasmo.baris.R

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ScanViewModel by viewModels()
    private lateinit var cameraExecutor: ExecutorService
    private var lastScannedCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Nustatome kalbos mygtuko tekstą/vėliavėlę
        val currentLang = resources.configuration.locales[0].language
        binding.btnChangeLanguage.text = if (currentLang == "en") "🇬🇧" else "🇱🇹"

        viewModel.scanResult.observe(this) { updateUI(it) }

        binding.btnScan.setOnClickListener {
            if (allPermissionsGranted()) startCamera()
            else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
        }

        binding.btnChangeLanguage.setOnClickListener { showLanguageDialog() }

        binding.btnClear.setOnClickListener {
            binding.resultCard.visibility = View.INVISIBLE
            lastScannedCode = ""
        }

        binding.btnCloseApp.setOnClickListener { finishAffinity() }
    }

    private fun updateUI(result: ScanResult) {
        binding.resultCard.visibility = View.VISIBLE

        // Spalvų nustatymas
        val baseColor = if (result.isLt) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
        val bgColor = if (result.isLt) "#E8F5E9" else "#F5F5F5"
        binding.resultLayout.setBackgroundColor(Color.parseColor(bgColor))

        // Kilmės vertimas: naudojame getString su resursų ID
        val countryName = if (result.countryResId != 0) getString(result.countryResId) else ""
        val flag = getFlagEmoji(result.countryResId)

        // PATAISYTA EILUTĖ: Pridėtas barkodas skliausteliuose
        binding.txtCountry.text = "${getString(R.string.origin)}: $countryName $flag (${result.barcode})"
        binding.txtCountry.setTextColor(baseColor)

        if (result.isNotFound) {
            binding.txtStatus.text = getString(R.string.not_found)
        } else if (result.error != null) {
            binding.txtStatus.text = "Error: ${result.error}"
        } else {
            val statusTitle = if (result.isLt) getString(R.string.status_lithuanian) else getString(R.string.status_foreign)
            val qty = if (result.quantity.isNotEmpty()) " (${result.quantity})" else ""

            // Formuojame pilną informaciją
            binding.txtStatus.text = """
                $statusTitle
                
                📦 ${result.productName}$qty
                🏭 ${getString(R.string.label_brand)}: ${result.brand}
                🥗 ${getString(R.string.label_nutriscore)}: ${result.nutriScore}
                ⚠️ ${getString(R.string.label_allergens)}: ${result.allergens.ifEmpty { "---" }}
                🧪 ${getString(R.string.label_additives)}: ${result.additives.ifEmpty { "---" }}
                
                🌿 ${getString(R.string.label_ingredients)}:
                ${result.ingredients.ifEmpty { "---" }}
            """.trimIndent()
        }
        binding.txtStatus.setTextColor(Color.BLACK)
    }

    private fun getFlagEmoji(resId: Int): String {
        return when (resId) {
            R.string.country_lithuania -> "🇱🇹"
            R.string.country_usa_canada -> "🇺🇸🇨🇦"
            R.string.country_france -> "🇫🇷"
            R.string.country_germany -> "🇩🇪"
            R.string.country_poland -> "🇵🇱"
            R.string.country_latvia -> "🇱🇻"
            R.string.country_estonia -> "🇪🇪"
            R.string.country_ukraine -> "🇺🇦"
            R.string.country_russia -> "🇷🇺"
            R.string.country_japan -> "🇯🇵"
            R.string.country_uk -> "🇬🇧"
            R.string.country_italy -> "🇮🇹"
            R.string.country_spain -> "🇪🇸"
            R.string.country_china -> "🇨🇳"
            R.string.country_belarus -> "🇧🇾"
            R.string.country_finland -> "🇫🇮"
            R.string.country_norway -> "🇳🇴"
            R.string.country_sweden -> "🇸🇪"
            R.string.country_bulgaria -> "🇧🇬"
            R.string.country_slovenia -> "🇸🇮"
            R.string.country_croatia -> "🇭🇷"
            R.string.country_saudi_arabia -> "🇸🇦"
            else -> "🌍"
        }
    }

    private fun showLanguageDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_language, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

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

    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            BarcodeScanning.getClient().process(image).addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull()?.rawValue?.let { code ->
                                    if (code != lastScannedCode) {
                                        lastScannedCode = code
                                        runOnUiThread { viewModel.processBarcode(code) }
                                    }
                                }
                            }.addOnCompleteListener { imageProxy.close() }
                        }
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}