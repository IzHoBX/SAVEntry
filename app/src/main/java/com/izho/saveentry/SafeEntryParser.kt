package com.izho.saveentry

class SafeEntryParser {

    companion object {
        const val QR_URL_PREFIX = "https://temperaturepass.ndi-api.gov.sg/login/"

        fun getLocationId(url:String) : String {
            return url.substring(SafeEntryParser.QR_URL_PREFIX.length)
        }
    }
}