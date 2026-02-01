package com.petrasmo.baris.util

import com.petrasmo.baris.R

object BarcodeHelper {
    fun getCountryResId(barcode: String): Int {
        if (barcode.length < 3) return R.string.country_other
        val prefix3 = barcode.take(3).toIntOrNull() ?: return R.string.country_other

        return when (prefix3) {
            in 0..139 -> R.string.country_usa_canada
            in 300..379 -> R.string.country_france
            380 -> R.string.country_bulgaria
            383 -> R.string.country_slovenia
            385 -> R.string.country_croatia
            387 -> R.string.country_bosnia
            in 400..440 -> R.string.country_germany
            in 450..459, in 490..499 -> R.string.country_japan
            in 460..469 -> R.string.country_russia
            470 -> R.string.country_kyrgyzstan
            471 -> R.string.country_taiwan
            474 -> R.string.country_estonia
            475 -> R.string.country_latvia
            476 -> R.string.country_azerbaijan
            477 -> R.string.country_lithuania
            478 -> R.string.country_uzbekistan
            479 -> R.string.country_sri_lanka
            480 -> R.string.country_philippines
            481 -> R.string.country_belarus
            482 -> R.string.country_ukraine
            484 -> R.string.country_moldova
            485 -> R.string.country_armenia
            486 -> R.string.country_georgia
            487 -> R.string.country_kazakhstan
            489 -> R.string.country_hong_kong
            in 500..509 -> R.string.country_uk
            in 520..521 -> R.string.country_greece
            528 -> R.string.country_lebanon
            529 -> R.string.country_cyprus
            530 -> R.string.country_albania
            531 -> R.string.country_macedonia
            535 -> R.string.country_malta
            539 -> R.string.country_ireland
            in 540..549 -> R.string.country_belgium_lux
            560 -> R.string.country_portugal
            569 -> R.string.country_iceland
            in 570..579 -> R.string.country_denmark
            590 -> R.string.country_poland
            594 -> R.string.country_romania
            599 -> R.string.country_hungary
            in 600..601 -> R.string.country_south_africa
            603 -> R.string.country_ghana
            611 -> R.string.country_morocco
            613 -> R.string.country_algeria
            619 -> R.string.country_tunisia
            622 -> R.string.country_egypt
            625 -> R.string.country_jordan
            626 -> R.string.country_iran
            628 -> R.string.country_saudi_arabia
            629 -> R.string.country_uae
            in 640..649 -> R.string.country_finland
            in 690..699 -> R.string.country_china
            in 700..709 -> R.string.country_norway
            729 -> R.string.country_israel
            in 730..739 -> R.string.country_sweden
            750 -> R.string.country_mexico
            in 760..769 -> R.string.country_switzerland
            in 770..771 -> R.string.country_colombia
            779 -> R.string.country_argentina
            780 -> R.string.country_chile
            in 789..790 -> R.string.country_brazil
            in 800..839 -> R.string.country_italy
            in 840..849 -> R.string.country_spain
            858 -> R.string.country_slovakia
            859 -> R.string.country_czech
            860 -> R.string.country_serbia
            in 868..869 -> R.string.country_turkey
            in 870..879 -> R.string.country_netherlands
            880 -> R.string.country_south_korea
            885 -> R.string.country_thailand
            888 -> R.string.country_singapore
            890 -> R.string.country_india
            893 -> R.string.country_vietnam
            899 -> R.string.country_indonesia
            in 900..919 -> R.string.country_austria
            in 930..939 -> R.string.country_australia
            in 940..949 -> R.string.country_new_zealand
            955 -> R.string.country_malaysia
            else -> R.string.country_other
        }
    }

    fun isLithuanian(barcode: String): Boolean = barcode.startsWith("477")
}