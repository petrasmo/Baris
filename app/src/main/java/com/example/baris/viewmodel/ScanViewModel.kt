package com.example.baris.viewmodel

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baris.ui.model.ScanResult
import com.example.baris.util.BarcodeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ScanViewModel : ViewModel() {

    private val _scanResult = MutableLiveData<ScanResult>()
    val scanResult: LiveData<ScanResult> = _scanResult

    fun processBarcode(barcode: String) {
        val country = BarcodeHelper.getCountryData(barcode)
        val isLt = BarcodeHelper.isLithuanian(barcode)

        val baseStatusText = if (isLt) "Tikrinama lietuviška prekė..." else "Tikrinama užsienio prekė..."
        val baseColor = if (isLt) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
        val baseBg = if (isLt) "#E8F5E9" else "#F5F5F5"

        _scanResult.value = ScanResult(country, baseStatusText, baseColor, baseBg)

        viewModelScope.launch(Dispatchers.IO) {
            val details = fetchProductDetails(barcode)

            withContext(Dispatchers.Main) {
                val finalStatusText = if (isLt) {
                    "Valio! Tai lietuviška prekė 🇱🇹\n\n$details"
                } else {
                    "Prekė nėra lietuviška\n\n$details"
                }

                _scanResult.value = ScanResult(
                    countryName = "$country ($barcode)",
                    statusText = finalStatusText,
                    statusColor = baseColor,
                    backgroundColor = baseBg
                )
            }
        }
    }

    private fun fetchProductDetails(barcode: String): String {
        return try {
            val url = URL("https://world.openfoodfacts.org/api/v0/product/$barcode.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)

            if (json.has("status") && json.getInt("status") == 1) {
                val product = json.getJSONObject("product")

                // 1. Pavadinimas
                val name = product.optString("product_name_lt").ifEmpty {
                    product.optString("product_name", "Pavadinimas nerastas")
                }

                // 2. Gamintojas
                val brand = product.optString("brands", "Gamintojas nežinomas")

                // 3. Kiekis
                val quantity = product.optString("quantity", "")

                // 4. Nutri-Score (A-E reitingas)
                val nutriScore = product.optString("nutriscore_grade", "Nenurodyta").uppercase()

                // 5. Alergenai (išvalome nereikalingus priedus 'en:' arba 'lt:')
                var allergens = product.optString("allergens_from_ingredients", "Nenurodyta")
                    .replace("en:", "")
                    .replace("lt:", "")
                    .replace(",", ", ")

                if (allergens.isEmpty()) allergens = "Nenurodyta"

                // 6. E-priedai (Priedai prie maisto)
                var additives = product.optString("additives_tags", "Nėra")
                    .replace("en:", "")
                    .replace("[", "")
                    .replace("]", "")
                    .replace(",", ", ")
                    .uppercase()

                if (additives.isEmpty()) additives = "Nėra"

                // 7. Sudėtis
                val ingredients = product.optString("ingredients_text_lt").ifEmpty {
                    product.optString("ingredients_text", "Sudėtis nepateikta")
                }

                // Rezultato formavimas
                val quantityText = if (quantity.isNotEmpty()) " ($quantity)" else ""

                "📦 $name$quantityText\n" +
                        "🏭 Gamintojas: $brand\n" +
                        "🥗 Nutri-Score: $nutriScore\n" +
                        "⚠️ Alergenai: $allergens\n" +
                        "🧪 Priedai: $additives\n\n" +
                        "🌿 Sudėtis:\n$ingredients"

            } else {
                "Prekės aprašymas nerastas duomenų bazėje."
            }
        } catch (e: Exception) {
            "Nepavyko gauti informacijos: ${e.message}"
        }
    }
}