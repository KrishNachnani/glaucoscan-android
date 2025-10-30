package com.glaucoma.ai.base.local

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class SharedPrefManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    object KEY {
        const val IS_FIRST = "is_first"
    }

    fun savePopupStatus(isFirst: Int) {
        sharedPreferences.edit {
            putInt(KEY.IS_FIRST, isFirst)
        }
    }

    fun getPopupStatus(): Int {
        return sharedPreferences.getInt(KEY.IS_FIRST, 0)
    }

    fun clear() {
        sharedPreferences.edit { clear() }
    }
}