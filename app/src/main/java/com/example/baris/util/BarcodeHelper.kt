package com.example.baris.util

object BarcodeHelper {

    // Grąžina valstybės pavadinimą su vėliavėle

    fun getCountryData(barcode: String): String {
        if (barcode.length < 3) return "Neatpažinta"
        val prefix3 = barcode.take(3).toIntOrNull() ?: return "Neatpažinta"

        return when (prefix3) {
            in 0..139 -> "JAV ir Kanada 🇺🇸🇨🇦"
            in 300..379 -> "Prancūzija 🇫🇷"
            380 -> "Bulgarija 🇧🇬"
            383 -> "Slovėnija 🇸🇮"
            385 -> "Kroatija 🇭🇷"
            387 -> "Bosnija ir Herc. 🇧🇦"
            in 400..440 -> "Vokietija 🇩🇪"
            in 450..459, in 490..499 -> "Japonija 🇯🇵"
            in 460..469 -> "Rusija 🇷🇺"
            470 -> "Kirgizija 🇰🇬"
            471 -> "Taivanas 🇹🇼"
            474 -> "Estija 🇪🇪"
            475 -> "Latvija 🇱🇻"
            476 -> "Azerbaidžanas 🇦🇿"
            477 -> "Lietuva 🇱🇹"
            478 -> "Uzbekija 🇺🇿"
            479 -> "Šri Lanka 🇱🇰"
            480 -> "Filipinai 🇵🇭"
            481 -> "Baltarusija 🇧🇾"
            482 -> "Ukraina 🇺🇦"
            484 -> "Moldova 🇲🇩"
            485 -> "Armėnija 🇦🇲"
            486 -> "Gruzija 🇬🇪"
            487 -> "Kazachstanas 🇰🇿"
            489 -> "Honkongas 🇭🇰"
            in 500..509 -> "Jungtinė Karalystė 🇬🇧"
            in 520..521 -> "Graikija 🇬🇷"
            528 -> "Libanas 🇱🇧"
            529 -> "Kipras 🇨🇾"
            530 -> "Albanija 🇦🇱"
            531 -> "Makedonija 🇲🇰"
            535 -> "Malta 🇲🇹"
            539 -> "Airija 🇮🇪"
            in 540..549 -> "Belgija ir Liuksemburgas 🇧🇪🇱🇺"
            560 -> "Portugalija 🇵🇹"
            569 -> "Islandija 🇮🇸"
            in 570..579 -> "Danija 🇩🇰"
            590 -> "Lenkija 🇵🇱"
            594 -> "Rumunija 🇷🇴"
            599 -> "Vengrija 🇭🇺"
            in 600..601 -> "Pietų Afrika 🇿🇦"
            603 -> "Gana 🇬🇭"
            611 -> "Marokas 🇲🇦"
            613 -> "Alžyras 🇩🇿"
            619 -> "Tunisas 🇹🇳"
            622 -> "Egiptas 🇪🇬"
            625 -> "Jordanija 🇯🇴"
            626 -> "Iranas 🇮🇷"
            628 -> "Saudo Arabija 🇸🇦"
            629 -> "JAE 🇦🇪"
            in 640..649 -> "Suomija 🇫🇮"
            in 690..699 -> "Kinija 🇨🇳"
            in 700..709 -> "Norvegija 🇳🇴"
            729 -> "Izraelis 🇮🇱"
            in 730..739 -> "Švedija 🇸🇪"
            750 -> "Meksika 🇲🇽"
            in 760..769 -> "Šveicarija 🇨🇭"
            in 770..771 -> "Kolumbija 🇨🇴"
            779 -> "Argentina 🇦🇷"
            780 -> "Čilė 🇨🇱"
            in 789..790 -> "Brazilija 🇧🇷"
            in 800..839 -> "Italija 🇮🇹"
            in 840..849 -> "Ispanija 🇪🇸"
            858 -> "Slovakija 🇸🇰"
            859 -> "Čekija 🇨🇿"
            860 -> "Serbija 🇷🇸"
            in 868..869 -> "Turkija 🇹🇷"
            in 870..879 -> "Nyderlandai 🇳🇱"
            880 -> "Pietų Korėja 🇰🇷"
            885 -> "Tailandas 🇹🇭"
            888 -> "Singapūras 🇸🇬"
            890 -> "Indija 🇮🇳"
            893 -> "Vietnamas 🇻🇳"
            899 -> "Indonezija 🇮🇩"
            in 900..919 -> "Austrija 🇦🇹"
            in 930..939 -> "Australija 🇦🇺"
            in 940..949 -> "Naujoji Zelandija 🇳🇿"
            955 -> "Malaizija 🇲🇾"
            else -> "Užsienis (Kitos šalys)"
        }
    }

    /**
     * Tikrina, ar prekė yra lietuviška.
     */
    fun isLithuanian(barcode: String): Boolean {
        return barcode.startsWith("477")
    }


}