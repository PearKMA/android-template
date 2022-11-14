@file:JvmName("StringUtils")

package com.testarossa.template.library.android.utils

import java.text.DecimalFormat
import java.util.regex.Pattern
import kotlin.math.log10
import kotlin.math.pow

/**
 * Kiểm tra tên file
 * @param nameFile chuỗi cần kiểm tra
 * @return  true: tên hợp lệ/ false: tên ko hợp lệ
 */
fun isFileNameValid(nameFile: String, extension: String): Boolean {
    val regex = "(((^[^.\\\\/:*?\"<>|])([^\\\\/:*?\"<>|]*))(\\.(?i)($extension))$)"
    val pattern = Pattern.compile(regex)
    val optimalString = optimalString(nameFile)
    val matcher = pattern.matcher(optimalString)
    return matcher.matches()
}

fun String?.isEmail(): Boolean {
    return this?.let {
        Pattern.matches(this, "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*\$")
    } ?: let {
        false
    }
}

fun isMatchedRegex(string: String, regex: String): Boolean {
    val pattern = Pattern.compile(regex)
    val optimalString = optimalString(string)
    val matcher = pattern.matcher(optimalString)
    return matcher.matches()
}


/**
 * Tối ưu chuỗi: loại bỏ các khoảng trắng do tab hoặc new line..
 * @param name  tên cần tối ưu
 * @return  text đã được tối ưu
 */
fun optimalString(name: String): String {
    val strings = name.split(" ")
    var optimalString = ""
    for (str in strings) {
        if (str.trim().isNotEmpty()) {
            optimalString += "$str "
        }
    }
    return optimalString.trim()
}

fun String.hexDecodeString(): String {
    val output = StringBuilder()

    for (i in this.indices step 2) {
        val str = this.substring(i, i + 2)
        output.append(str.toInt(16).toChar())
    }
    return output.toString()
}

fun Long.readableFormat(
    defaultValue: String = "0",
    pattern: String = "#,##0.##",
    units: Array<String>,
    divisionUnit: Double
): String {
    if (this <= 0) return defaultValue
    val digitGroups = (log10(this.toDouble()) / log10(divisionUnit)).toInt()
    return DecimalFormat(pattern).format(
        this / divisionUnit.pow(digitGroups.toDouble())
    ).toString() + "" + units[digitGroups]
}
