package com.izho.saveentry.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.izho.saveentry.LiveBarcodeScanningActivity
import com.izho.saveentry.viewmodel.CheckInOrOutViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class RemoteConfigManager {

    companion object {
        val defaults = HashMap<String, String>()
        const val QR_CODE_HOST = "qr_code_host"
        const val QR_URL_PREFIX = "qr_url_prefix"
        const val BACKEND_HOST = "backend_host"
        const val BACKEND_BUILDING_PATH = "backend_building_path"
        const val CHECKOUT_PAGE_ICON_PATH = "checkout_page_icon_path"
        const val CHECK_IN_SCRIPT = "check_in_script"
        const val CHECK_OUT_SCRIPT = "check_out_script"
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
            defaults.put(CHECK_IN_SCRIPT, """
                                console.log('Local version')
                                const waitElement = (selector, idx, cb) => {
                                    const waitId = setInterval(() => {
                                        const el = $(selector)
                                        if (el.length) {
                                            clearInterval(waitId);
                                            cb(el[idx]);
                                        }
                                    }, 100);
                                }

                                waitElement('.safeentry-check-btn', 0, checkInBtn => {
                                    checkInBtn.click();

                                    waitElement('.submit-button button', 0, (submitBtn) => {
                                        if (submitBtn.disabled) {
                                            alert('${CheckInOrOutViewModel.MessageType.NO_DETAILS.name}');
                                        } else {
                                            submitBtn.click()
                                        }
                                    });
                                });

                                waitElement('.safe-entry-card', 0, card => {
                                    alert('${CheckInOrOutViewModel.MessageType.CHECK_IN_COMPLETED.name}');
                                });
                            """)
            defaults.put(CHECK_OUT_SCRIPT, """
                                console.log('Local version')
                                const waitElement = (selector, idx, cb) => {
                                    const waitId = setInterval(() => {
                                        const el = $(selector);
                                        if (el.length) {
                                            clearInterval(waitId);
                                            cb(el[idx]);
                                        }
                                    }, 100);
                                };

                                waitElement('.safeentry-check-btn', 1, checkInBtn => {
                                    checkInBtn.click();

                                    waitElement('.submit-button button', 0, (submitBtn) => {
                                        if (submitBtn.disabled) {
                                            alert('${CheckInOrOutViewModel.MessageType.NO_DETAILS.name}');
                                        } else {
                                            submitBtn.click();
                                        }
                                    });
                                });

                                waitElement('.safe-entry-card', 0, card => {
                                    alert('${CheckInOrOutViewModel.MessageType.CHECK_OUT_COMPLETED.name}');
                                });
                            """)
            remoteConfig.setDefaultsAsync(defaults as Map<String, Any>)
            remoteConfig.fetchAndActivate()
        }

        fun getListStringData(key:String) : List<String> {
            try {
                val raw = remoteConfig.getString(key)
                return adapter.fromJson(raw) as List<String>
            } catch (e:Throwable) {
                return adapter.fromJson(defaults.get(key)) as List<String>
            }
        }

        fun getStringData(key:String) : String {
            return remoteConfig.getString(key)
        }

        fun update() {
            try {
                remoteConfig.fetchAndActivate()
            } catch (e: FirebaseRemoteConfigFetchThrottledException) {

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