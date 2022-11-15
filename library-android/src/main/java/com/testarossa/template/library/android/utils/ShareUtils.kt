package com.testarossa.template.library.android.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.ShareCompat
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
        val fileUri =
            if (isBuildLargerThan(Build.VERSION_CODES.N)) FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                file
            ) else Uri.parse("file://" + file.absolutePath)

        ShareCompat.IntentBuilder(context)
            .setType(getTypeFromFile(file.absolutePath) ?: "*/*")
            .addStream(fileUri)
            .setChooserTitle("Share via")
            .startChooser()

        // or use :
//        val intentShareFile = Intent(Intent.ACTION_SEND)
//        intentShareFile.type = getTypeFromFile(file.absolutePath) ?: "*/*"
//        intentShareFile.putExtra(Intent.EXTRA_STREAM, fileUri)
//        intentShareFile.flags =
//            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//
//        val chooser = Intent.createChooser(intentShareFile, "Share via")
//        val resInfoList = context.packageManager.queryIntentActivities(chooser, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
//        resInfoList.forEach { info ->
//            val packageName = info.activityInfo.packageName
//            context.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//        context.startActivity(chooser)
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
    listUri: ArrayList<Uri>,
    mimeType: String = "*/*"
) {
    try {
        val shareIntent = ShareCompat.IntentBuilder(context)
            .setType(mimeType)
        listUri.forEach { uri -> shareIntent.addStream(uri) }
        val intent = shareIntent.intent
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share via"))
        /* or use:
        val intent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putExtra(Intent.EXTRA_SUBJECT, "Here are some files.")
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, listUri)
        }
        context.startActivity(intent)*/
    } catch (e: Exception) {
        e.printStackTrace()
    }
}