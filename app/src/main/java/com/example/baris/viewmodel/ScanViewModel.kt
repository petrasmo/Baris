package com.example.baris.viewmodel

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.baris.ui.model.ScanResult
import com.example.baris.util.BarcodeHelper

class ScanViewModel : ViewModel() {

    // Tai yra „stebimas“ objektas. MainActivity jį stebės ir atnaujins vaizdą.
    private val _scanResult = MutableLiveData<ScanResult>()
    val scanResult: LiveData<ScanResult> = _scanResult

    fun processBarcode(barcode: String) {
        val country = BarcodeHelper.getCountryData(barcode)
        val isLt = BarcodeHelper.isLithuanian(barcode)

        val result = if (isLt) {
            ScanResult(
                countryName = "$country ($barcode)",
                statusText = "Valio! Tai lietuviška prekė 🇱🇹",
                statusColor = Color.parseColor("#2E7D32"), // holo_green_dark
                backgroundColor = "#E8F5E9"
            )
        } else {
            ScanResult(
                countryName = "$country ($barcode)",
                statusText = "Prekė nėra lietuviška",
                statusColor = Color.parseColor("#C62828"), // holo_red_dark
                backgroundColor = "#F5F5F5"
            )
        }

        _scanResult.value = result
    }


}