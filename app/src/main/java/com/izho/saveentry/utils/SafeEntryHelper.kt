package com.izho.saveentry.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.izho.saveentry.LiveBarcodeScanningActivity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class SafeEntryHelper {

    companion object {
        val defaults = HashMap<String, String>()
        private const val QR_CODE_HOST = "qr_code_host"
        private const val QR_URL_PREFIX = "qr_url_prefix"
        private const val BACKEND_HOST = "backend_host"
        private const val BACKEND_BUILDING_PATH = "backend_building_path"
        private const val CHECKOUT_PAGE_ICON_PATH = "checkout_page_icon_path"
        //https://www.safeentry-qr.gov.sg/login/PROD-201012168M-273456-LEARNERSLODGEBUKITTIMAHPT-SE

        val adapter = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build().adapter(List::class.java)

        val remoteConfig = Firebase.remoteConfig

        init {
            defaults.put(QR_CODE_HOST, "[\n" +
                    "            \"temperaturepass.ndi-api.gov.sg\",\n" +
                    "            \"www.safeentry-qr.gov.sg\"\n" +
                    "            ]")
            defaults.put(QR_URL_PREFIX, "[\n" +
                    "            \"https://temperaturepass.ndi-api.gov.sg/login/\",\n" +
                    "            \"https://www.safeentry-qr.gov.sg/login/\"\n" +
                    "            ]")
            defaults.put(BACKEND_HOST, "[\n" +
                    "            \"backend.temperaturepass.ndi-api.gov.sg\",\n" +
                    "            \"backend.safeentry-qr.gov.sg\"\n" +
                    "            ]")
            defaults.put(BACKEND_BUILDING_PATH, "[\n" +
                    "            \"/api/v1/building\",\n" +
                    "            \"/api/v2/building\"\n" +
                    "            ]")
            defaults.put(CHECKOUT_PAGE_ICON_PATH, "[\n" +
                    "            \"/assets/images/successpage-tickblue-icon.svg\"\n" +
                    "            ]")
            remoteConfig.setDefaultsAsync(defaults as Map<String, Any>)
            remoteConfig.fetchAndActivate()
        }

        fun getLocationId(url:String) : String {
            var prefixChosen = ""
            for (prefix in getQRUrlPrefix()) {
                if (prefix in url) {
                    prefixChosen = prefix
                    break
                }
            }
            if (prefixChosen == "") {
                FirebaseCrashlytics.getInstance().recordException(
                    LiveBarcodeScanningActivity.NotSafeEntryQRException("$url prefix")
                )
                return ""
            }
            return url.substring(prefixChosen.length)
        }

        fun getQRUrlPrefix() : List<String> {
           return getData(QR_URL_PREFIX)
        }

        fun getQRCodeHost() : List<String> {
            return getData(QR_CODE_HOST)
        }

        fun getBackendHost() : List<String> {
            return getData(BACKEND_HOST)
        }

        fun getBackendBuildingPath() : List<String> {
            return getData(BACKEND_BUILDING_PATH)
        }

        fun getCheckoutPageIconPath() : List<String> {
            return getData(CHECKOUT_PAGE_ICON_PATH)
        }

        private fun getData(key:String) : List<String> {
            try {
                val raw = remoteConfig.getString(key)
                return adapter.fromJson(raw) as List<String>
            } catch (e:Throwable) {
                return adapter.fromJson(defaults.get(key)) as List<String>
            }
        }

        fun update() {
            try {
                remoteConfig.fetchAndActivate()
            } catch (e:FirebaseRemoteConfigFetchThrottledException) {

            }
        }

        fun forceUpdate(run:Runnable) {
            remoteConfig.fetch(0).addOnCompleteListener {
                remoteConfig.activate().addOnCompleteListener {
                    run.run()
                }
            }
        }

    }
}