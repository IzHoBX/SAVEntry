package com.izho.saveentry.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.lifecycle.*
import com.izho.saveentry.data.Location
import com.izho.saveentry.data.Visit
import com.izho.saveentry.data.VisitWithLocation
import com.izho.saveentry.data.getAppDatabase
import com.izho.saveentry.SAVEntryApplication
import com.izho.saveentry.utils.SafeEntryHelper
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.*

class CheckInOrOutViewModel(app: Application,
                            private val url: String,
                            private val action: String? = null,
                            private var visitId: Long? = null
) : AndroidViewModel(app) {

    private val database = getAppDatabase(app.applicationContext)

    private val moshi = Moshi.Builder().build()
    private val buildingInfoAdapter = moshi.adapter(BuildingInfo::class.java)

    private val httpClient by lazy { OkHttpClient() }

    private val locationId: String = url.split("/").last()

    private val _currentLocation = MutableLiveData<Location>().apply {
        value = null
    }
    val currentLocation: LiveData<Location> get() = _currentLocation

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage get() = _errorMessage

    private val _createdVisit = MutableLiveData<VisitWithLocation>().apply {
        value = null
    }

    val createdVisit: LiveData<VisitWithLocation> get() = _createdVisit

    val _action = MutableLiveData<String>().apply {
        value = action
    }

    val _networkExecutionError = MutableLiveData<Boolean>().apply {
        value = false
    }

    val webViewClient by lazy {
        object: WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val requestHost = request?.url?.host

                // Intercept calls to backend so that we can pick out the details about the place we
                // are signing in from.
                if (currentLocation.value == null && requestHost != null
                    && requestHost in SafeEntryHelper.getBackendHost() && request.url.path in SafeEntryHelper.getBackendBuildingPath()
                ) {
                    // Copy over all the headers
                    var req = Request.Builder().url(request.url.toString())
                    for ((k, v) in request.requestHeaders) {
                        req = req.addHeader(k, v)
                    }

                    // Retrieve the cookies for the request from the WebView
                    val cookieManager = CookieManager.getInstance()
                    val cookies = cookieManager.getCookie(request.url.toString())
                    if (cookies != null) {
                        req = req.addHeader("Cookie", cookies)
                    }

                    try {
                        val response = this@CheckInOrOutViewModel
                            .httpClient.newCall(req.build()).execute()

                        val jsonData = response.body?.charStream()?.readText()
                        Log.i(TAG, "$jsonData")
                        jsonData?.let {
                            val info = buildingInfoAdapter.fromJson(jsonData)
                            info?.let {
                                _currentLocation.postValue(
                                    Location(locationId, info.entityName, info.venueName, url))
                            }
                        }
                    } catch (e:Throwable) {
                        //this is just in-case. By right if our fetch encounter error, webview will also show error and user will take action from there
                        _currentLocation.postValue(
                            Location(locationId, "UKNOWN", SafeEntryHelper.getLocationId(url), url))
                        _networkExecutionError.postValue(true)
                    }
                } else if (_action.value == "checkIn" && request?.url?.path in SafeEntryHelper.getCheckoutPageIconPath()) {
                    if(view!=null) {
                        _action.postValue("checkOut")
                        checkOutOfLocation(view)
                    }
                }

                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Check that it has navigated to the qrScan page
                if (url != null && _action.value != null && (url.contains("qrScan") || url.contains("tenant"))) {
                    when(_action.value) {
                        "checkIn" -> {
                            val clickCheckIn = """
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
                                            alert('${MessageType.NO_DETAILS.name}');
                                        } else {
                                            submitBtn.click()
                                        }
                                    });
                                });

                                waitElement('.safe-entry-card', 0, card => {
                                    alert('${MessageType.CHECK_IN_COMPLETED.name}');
                                });
                            """

                            view?.evaluateJavascript(clickCheckIn) {}
                        }

                        "checkOut" -> {
                            val clickCheckOut = """
                                const waitElement = (selector, idx, cb) => {
                                    const waitId = setInterval(() => {
                                        const el = $(selector)
                                        if (el.length) {
                                            clearInterval(waitId);
                                            cb(el[idx]);
                                        }
                                    }, 100);
                                }

                                waitElement('.safeentry-check-btn', 1, checkInBtn => {
                                    checkInBtn.click();

                                    waitElement('.submit-button button', 0, (submitBtn) => {
                                        if (submitBtn.disabled) {
                                            alert('${MessageType.NO_DETAILS.name}');
                                        } else {
                                            submitBtn.click()
                                        }
                                    });
                                });

                                waitElement('.safe-entry-card', 0, card => {
                                    alert('${MessageType.CHECK_OUT_COMPLETED.name}');
                                });
                            """

                            view?.evaluateJavascript(clickCheckOut) {}
                        }
                    }
                }
            }
        }
    }

    val webChromeClient by lazy {
        object: WebChromeClient() {
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                if (view != null) {
                    message?.let {
                        when(MessageType.valueOf(it)) {
                            MessageType.CHECK_IN_COMPLETED -> checkInToLocation(view)
                            MessageType.CHECK_OUT_COMPLETED -> checkOutOfLocation(view)
                            MessageType.NO_DETAILS -> {
                                errorMessage.value = "Please fill in details for the first time."
                                Log.i(TAG, "Unable to automatically " +
                                        "check in/out as details are not provided.")
                            }
                        }
                    }
                }

                result?.confirm()
                return true
            }
        }
    }

    fun completeEventConfirm() {
        _createdVisit.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun checkInToLocation(view: View, location:Location?=currentLocation.value, isOfflineCheckIn:Boolean=false) {
        viewModelScope.launch {
            if (location != null) {
                database.dao.insertLocation(location)

                val passImage = captureSnapshot(view)
                val visitId = database.dao.insertVisit(
                    Visit(locationId = location.locationId, passImagePath = passImage, isOfflineCheckIn = isOfflineCheckIn))
                this@CheckInOrOutViewModel.visitId = visitId
                val visit = database.dao.getVisitWithLocationById(visitId)

                Log.i(TAG, "Completed checking in for $locationId")
                _createdVisit.value = visit
            } else {
                _errorMessage.value = "Unable to save check-in. Try again."
            }
        }
    }

    @SuppressLint("Parameter never used")
    fun checkOutOfLocation(view: View) {
        val localVistId = visitId
        localVistId?.let {
            viewModelScope.launch {
                val data = database.dao.getVisitWithLocationById(localVistId)

                // Delete pass image file
                data.visit.passImagePath?.let {
                    val app = getApplication<SAVEntryApplication>()
                    val file = File(app.applicationContext.filesDir, it)
                    if (file.exists()) {
                        file.delete()
                    }
                }

                // Update the check out time to current time.
                data.visit.checkOutAt = System.currentTimeMillis()
                database.dao.updateVisit(data.visit)

                Log.i(TAG, "Completed checking out for ${data.location.locationId}")
                _createdVisit.value = data
            }
        }
    }

    private suspend fun captureSnapshot(view: View): String? {
        return withContext(Dispatchers.IO) {
            val bitmap = Bitmap.createBitmap(
                view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            val application = getApplication<SAVEntryApplication>()

            val filename = "${UUID.randomUUID()}.jpg"
            application.applicationContext
                .openFileOutput(filename, Context.MODE_PRIVATE).use {
                    // Write the file as a jpeg
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
                }
            bitmap.recycle()

            filename
        }
    }

    @JsonClass(generateAdapter = true)
    data class BuildingInfo(val entityName: String, val venueName: String)

    enum class MessageType() {
        CHECK_OUT_COMPLETED,
        CHECK_IN_COMPLETED,
        NO_DETAILS
    }

    class Factory(private val app: Application,
                  private val url: String,
                  private val action: String?,
                  private val viewId: Long?
    ): ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CheckInOrOutViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CheckInOrOutViewModel(app, url, action, viewId) as T
            }

            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }

    companion object {
        private const val TAG = "CheckInOrOutViewModel"
    }
}
