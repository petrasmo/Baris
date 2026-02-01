package com.example.baris.ui.model

/**
 * Modelis, saugantis nuskaitytos prekės duomenis.
 * countryResId saugo nuorodą į strings.xml resursą, kad pavadinimas būtų verčiamas automatiškai.
 */
data class ScanResult(
    val barcode: String = "",
    val countryResId: Int = 0, // Pakeista iš String į Int, kad dingtų "Unresolved reference" klaida
    val isLt: Boolean = false,
    val productName: String = "",
    val brand: String = "",
    val nutriScore: String = "",
    val allergens: String = "",
    val additives: String = "",
    val ingredients: String = "",
    val quantity: String = "",
    val isNotFound: Boolean = false,
    val error: String? = null
)