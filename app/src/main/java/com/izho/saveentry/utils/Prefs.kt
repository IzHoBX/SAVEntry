package com.izho.saveentry.utils

import android.content.Context

const val TUTORIAL = "tutorial"
const val HAS_SEEN_TUTORIAL = "has_seen_tutorial"

class Prefs {
    companion object {
        fun hasSeenTutorial(context:Context) : Boolean {
            val sp = context.getSharedPreferences(TUTORIAL, Context.MODE_PRIVATE)
            return sp.getBoolean(HAS_SEEN_TUTORIAL, false)
        }

        fun setHasSeenTutorial(context: Context, newVal:Boolean) {
            val sp = context.getSharedPreferences(TUTORIAL, Context.MODE_PRIVATE)
            sp.edit().putBoolean(HAS_SEEN_TUTORIAL, newVal).apply()
        }
    }
}