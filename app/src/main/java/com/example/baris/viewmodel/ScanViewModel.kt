package com.example.baris.viewmodel

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
        val isLt = BarcodeHelper.isLithuanian(barcode)
        // PATAISYTA: kviečiame getCountryResId, nes getCountryData nebeegzistuoja
        val countryResId = BarcodeHelper.getCountryResId(barcode)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://world.openfoodfacts.org/api/v0/product/$barcode.json")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                withContext(Dispatchers.Main) {
                    if (json.has("status") && json.getInt("status") == 1) {
                        val p = json.getJSONObject("product")
                        _scanResult.value = ScanResult(
                            barcode = barcode,
                            countryResId = countryResId, // Naudojame naują kintamąjį
                            isLt = isLt,
                            productName = p.optString("product_name_lt").ifEmpty { p.optString("product_name", "---") },
                            brand = p.optString("brands", "---"),
                            nutriScore = p.optString("nutriscore_grade", "---").uppercase(),
                            allergens = p.optString("allergens_from_ingredients", "").replace("en:", "").replace("lt:", ""),
                            additives = p.optString("additives_tags", "").replace("en:", "").replace("lt:", ""),
                            ingredients = p.optString("ingredients_text_lt").ifEmpty { p.optString("ingredients_text", "---") },
                            quantity = p.optString("quantity", "")
                        )
                    } else {
                        _scanResult.value = ScanResult(
                            barcode = barcode,
                            countryResId = countryResId,
                            isLt = isLt,
                            isNotFound = true
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _scanResult.value = ScanResult(error = e.message)
                }
            }
        }
    }
}