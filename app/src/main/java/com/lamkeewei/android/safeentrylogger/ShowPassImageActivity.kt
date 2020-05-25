package com.lamkeewei.android.safeentrylogger

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.lamkeewei.android.safeentrylogger.databinding.ActivityShowPassImageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ShowPassImageActivity : AppCompatActivity() {
    private val activityScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityShowPassImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chipClose.setOnClickListener {
            finish()
        }

        val passImagePath = intent.extras?.getString("passImagePath", null)
        passImagePath?.let {
            activityScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    val file = File(applicationContext.filesDir, passImagePath)
                    BitmapFactory.decodeFile(file.absolutePath)
                }

                binding.chipClose.text = "Close Check-In Pass"
                binding.imageCheckinPass.setImageBitmap(bitmap)
                binding.progressBarLoadPassImage.visibility = View.GONE
            }
        }
    }
}
