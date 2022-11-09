package com.testarossa.template.library.android.utils

import java.io.File
import java.util.LinkedList

fun folderSize(file: File?): Long {
    if (file == null || !file.exists())
        return 0
    //
    if (!file.isDirectory && !file.isHidden)
        return file.length()
    val dirs = LinkedList<File>()
    dirs.add(file)
    var result = 0L
    while (!dirs.isEmpty()) {
        val dir = dirs.removeAt(0)
        if (!dir.exists())
            continue
        val listFiles = dir.listFiles()
        if (listFiles.isNullOrEmpty()) {
            continue
        }
        for (child in listFiles) {
            if (!child.isHidden) {
                result += child.length()
                if (child.isDirectory) {
                    dirs.add(child)
                }
            }
        }
    }
    return result
}
