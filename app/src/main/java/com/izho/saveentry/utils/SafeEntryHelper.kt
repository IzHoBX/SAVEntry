package com.izho.saveentry.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.izho.saveentry.LiveBarcodeScanningActivity

class SafeEntryHelper {

    companion object {

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
           return RemoteConfigManager.getListStringData(RemoteConfigManager.QR_URL_PREFIX)
        }

        fun getQRCodeHost() : List<String> {
            return RemoteConfigManager.getListStringData(RemoteConfigManager.QR_CODE_HOST)
        }

        fun getBackendHost() : List<String> {
            return RemoteConfigManager.getListStringData(RemoteConfigManager.BACKEND_HOST)
        }

        fun getBackendBuildingPath() : List<String> {
            return RemoteConfigManager.getListStringData(RemoteConfigManager.BACKEND_BUILDING_PATH)
        }

        fun getCheckoutPageIconPath() : List<String> {
            return RemoteConfigManager.getListStringData(RemoteConfigManager.CHECKOUT_PAGE_ICON_PATH)
        }

        fun update() {
            RemoteConfigManager.update()
        }

        fun getCheckinScript() : String {
            return RemoteConfigManager.getStringData(RemoteConfigManager.CHECK_IN_SCRIPT)
        }

        fun getCheckOutScript() : String {
            return RemoteConfigManager.getStringData(RemoteConfigManager.CHECK_OUT_SCRIPT)
        }

        fun forceUpdate(run:Runnable) {
            RemoteConfigManager.forceUpdate(run)
        }

    }
}