/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izho.saveentry.settings

import android.hardware.Camera
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.izho.saveentry.utils.Utils
import com.izho.saveentry.camera.CameraSource
import com.izho.saveentry.R
import com.izho.saveentry.utils.SafeEntryHelper
import java.util.HashMap

/** Configures App settings.  */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val tileBehaviorPreference =
            findPreference<androidx.preference.ListPreference>(getString(R.string.settings_tile_behavior))!!
        val optionsKeyToReadable = HashMap<String, String>()
        optionsKeyToReadable.put(getString(R.string.tile_behavior_scanner), "Always Open Scanner")
        optionsKeyToReadable.put(getString(R.string.tile_behavior_checkout), "Switch between Check in and out")
        tileBehaviorPreference.entries = optionsKeyToReadable.values.toTypedArray()
        tileBehaviorPreference.entryValues = optionsKeyToReadable.keys.toTypedArray()
        tileBehaviorPreference.summary = tileBehaviorPreference.entry
        tileBehaviorPreference.setOnPreferenceChangeListener { _, newValue ->
            val newBehaviorKey = newValue as String
            val context = activity ?: return@setOnPreferenceChangeListener false
            tileBehaviorPreference.summary = optionsKeyToReadable[newBehaviorKey]
            PreferenceUtils.saveStringPreference(
                context,
                R.string.settings_tile_behavior,
                newBehaviorKey)
            true
        }
        //sets default
        val currContext = context
        if(currContext != null) {
            tileBehaviorPreference.summary = optionsKeyToReadable[PreferenceUtils.getStringPref(currContext, R.string.settings_tile_behavior, currContext.getString(R.string.tile_behavior_scanner))]
        }

        findPreference<Preference>(getString(R.string.reset_checkin_information))?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            WebStorage.getInstance().deleteAllData();
            Toast.makeText(context, "Check in information cleared", Toast.LENGTH_SHORT).show()
            true
        }

    }
}
