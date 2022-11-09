package com.testarossa.template.library.android.utils

import android.content.Context
import android.net.Uri
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.lang.reflect.Array

private const val PRIMARY_VOLUME_NAME = "primary"

object SAFUtils {
    fun copyFolder(copy: File, directory: String, context: Context) {
        if (copy.isDirectory) {
            copy.listFiles()?.forEach {
                if (it.isDirectory) {
                    val dir = getDocumentFileIfAllowedToWrite(File(directory), context)
                    val folder = dir?.findFile(copy.name)
                    if (folder == null) {
                        dir?.createDirectory(copy.name)
                    }
                    val path = directory + "/" + it.name
                    copyFolder(it, path, context)
                } else {
                    copy(it, directory, context)
                }
            }
        } else {
            copy(copy, directory, context)
        }
    }

    fun copy(copy: File, directory: String, context: Context) {
        var inStream: FileInputStream? = null
        var outStream: OutputStream? = null

        val dir = getDocumentFileIfAllowedToWrite(File(directory), context)
        val mime = mime(copy.toURI().toString())
        val copy1 = dir?.createFile(mime, copy.name)
        try {
            if (copy1 != null) {
                inStream = FileInputStream(copy)
                outStream = context.contentResolver.openOutputStream(copy1.uri)
                val buffer = ByteArray(16384)
                var bytesRead = inStream.read(buffer)
                while (bytesRead != -1) {
                    outStream?.write(buffer, 0, bytesRead)
                    bytesRead = inStream.read(buffer)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inStream?.close()
                outStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getDocumentFileIfAllowedToWrite(file: File, context: Context): DocumentFile? {
        val permissionUris = context.contentResolver.persistedUriPermissions
        for (permissionUri in permissionUris) {
            val treeUri = permissionUri.uri
            val rootDocFile = DocumentFile.fromTreeUri(context, treeUri)
            val rootDocFilePath = getFullPathFromTreeUri(treeUri, context)
            if (rootDocFilePath != null && file.absolutePath.startsWith(rootDocFilePath)) {
                val pathInRootDocParts = ArrayList<String>()
                var fileCheck = file
                while (rootDocFilePath != fileCheck.absolutePath) {
                    pathInRootDocParts.add(fileCheck.name)
                    if (fileCheck.parentFile != null) {
                        fileCheck = fileCheck.parentFile!!
                    } else {
                        break
                    }
                }
                var docFile: DocumentFile? = null
                if (pathInRootDocParts.size == 0) {
                    docFile = DocumentFile.fromTreeUri(context, rootDocFile!!.uri)
                } else {
                    for (i in pathInRootDocParts.size - 1 downTo 0) {
                        docFile = if (docFile == null) {
                            rootDocFile?.findFile(pathInRootDocParts[i])
                        } else {
                            docFile.findFile(pathInRootDocParts[i])
                        }
                    }
                }
                return if (docFile != null && docFile.canWrite()) {
                    docFile
                } else {
                    null
                }
            }
        }
        return null
    }

    fun mime(uri: String): String {
        var type = "*/*"
        try {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
            }
        } catch (e: Exception) {
            type = "*/*"
        }
        return type
    }

    fun getFullPathFromTreeUri(treeUri: Uri?, volumeBasePath: String?): String? {
        if (treeUri == null) return null
        if (volumeBasePath == null) {
            return File.separator
        }

        var volumePath = volumeBasePath
        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length - 1)
        }

        var documentPath: String = getDocumentPathFromTreeUri(treeUri)
        if (documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0, documentPath.length - 1)
        }

        return if (documentPath.isNotEmpty()) {
            if (documentPath.startsWith(File.separator)) {
                volumePath + documentPath
            } else {
                volumePath + File.separator + documentPath
            }
        } else {
            volumePath
        }
    }

    private fun getDocumentPathFromTreeUri(treeUri: Uri): String {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val split = docId.split(":")
        return if (split.size >= 2) {
            split[1]
        } else {
            File.separator
        }
    }

    fun getFullPathFromTreeUri(treeUri: Uri, context: Context): String? {
        return getFullPathFromTreeUri(
            treeUri,
            getVolumePath(getVolumeIdFromTreeUri(treeUri), context)
        )
    }

    private fun getVolumeIdFromTreeUri(treeUri: Uri): String? {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val split = docId.split(":")
        return if (split.isNotEmpty()) {
            split[0]
        } else {
            null
        }
    }

    private fun getVolumePath(volumeId: String?, context: Context): String? {
        try {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = storageManager.javaClass.getMethod("getVolumeList")
            val getUuid = storageVolumeClazz.getMethod("getUuid")
            val getPath = storageVolumeClazz.getMethod("getPath")
            val isPrimary = storageVolumeClazz.getMethod("isPrimary")
            val result = getVolumeList.invoke((storageManager))
            val length: Int
            if (result != null) {
                length = Array.getLength(result)
                for (i in 0 until length) {
                    val storageVolumeElement = Array.get(result, i)
                    val uuid = getUuid.invoke(storageVolumeElement) as String?
                    val primary = isPrimary.invoke(storageVolumeElement) as Boolean

                    // primary volume?
                    if (primary && PRIMARY_VOLUME_NAME == volumeId) {
                        return (getPath.invoke(storageVolumeElement) as String)
                    }

                    // other volumes?
                    if (uuid != null) {
                        if (uuid == volumeId) {
                            return (getPath.invoke(storageVolumeElement) as String)
                        }
                    }
                }
            }
            // not found
            return null
        } catch (e: Exception) {
            return null
        }
    }
}
