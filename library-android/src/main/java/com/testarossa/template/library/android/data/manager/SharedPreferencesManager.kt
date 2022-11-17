package com.testarossa.template.library.android.data.manager

import android.content.Context
import android.content.SharedPreferences

/**
 * pref: SharedPreferences
 * pref = pref(requireContext(), COMPASS)
 * pref.get..
 * pref.edit {
 *      this.put....
 * }
 */


fun pref(context: Context, name: String): SharedPreferences =
    context.getSharedPreferences(name, Context.MODE_PRIVATE)

inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
    val editor = this.edit()
    operation(editor)
    editor.apply()
}

