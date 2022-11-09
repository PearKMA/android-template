package com.testarossa.template.library.android.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import androidx.core.content.FileProvider
import com.testarossa.template.library.android.utils.extension.isBuildLargerThan
import java.io.File

/**
 * Chia sẻ một file
 * @param context   context
 * @param file  file cần chia sẻ
 * @return  kết quả chia sẻ
 */
fun shareItem(context: Context?, file: File): Boolean {
    return if (context != null && file.exists()) {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val intentShareFile = Intent(Intent.ACTION_SEND)
        intentShareFile.type = getTypeFromFile(file.absolutePath) ?: "*/*"
        val fileUri =
            if (isBuildLargerThan(Build.VERSION_CODES.N)) FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                file
            ) else Uri.parse("file://" + file.absolutePath)
        intentShareFile.putExtra(Intent.EXTRA_STREAM, fileUri)
        intentShareFile.flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.startActivity(Intent.createChooser(intentShareFile, "Share via"))
        true
    } else {
        false
    }
}

/**
 * Chia sẻ nhiều file
 * @param context   context
 * @param listUri   danh sách file cần chia sẻ
 */
fun shareMultiples(
    context: Context,
    listUri: ArrayList<Uri>
) {
    try {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND_MULTIPLE
        intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.")
        intent.type = "*/*"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, listUri)
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
