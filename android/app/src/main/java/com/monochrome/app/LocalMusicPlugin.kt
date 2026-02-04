package com.monochrome.app

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import java.io.File
import org.json.JSONArray
import org.json.JSONException

@CapacitorPlugin(name = "LocalMusic")
class LocalMusicPlugin : Plugin() {
    private val AUDIO_EXTENSIONS = setOf("flac", "mp3", "m4a", "wav", "ogg", "aac")
    private val REQUEST_CODE_DIRECTORY = 1001
    private var pendingCall: PluginCall? = null

    @PluginMethod
    fun scanMusicDirectory(call: PluginCall) {
        val musicDir =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "")

        if (!musicDir.exists()) {
            call.reject("Music directory not found")
            return
        }

        try {
            val files = mutableListOf<JSObject>()
            scanDirectory(musicDir, files)

            val result = JSObject()
            result.put("files", JSONArray(files))
            call.resolve(result)
        } catch (e: Exception) {
            Log.e("LocalMusic", "Error scanning directory", e)
            call.reject("Error scanning directory: ${e.message}")
        }
    }

    @PluginMethod
    fun pickMusicFolder(call: PluginCall) {
        pendingCall = call
        val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
        startActivityForResult(intent, REQUEST_CODE_DIRECTORY)
    }

    @PluginMethod
    fun readFileBytes(call: PluginCall) {
        val path = call.getString("path")
        if (path == null) {
            call.reject("Path is required")
            return
        }

        try {
            val file = File(path)
            if (!file.exists()) {
                call.reject("File not found: $path")
                return
            }

            val bytes = file.readBytes()
            // Convert to base64 for JSON transfer
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

            val result = JSObject()
            result.put("data", base64)
            result.put("size", bytes.size)
            call.resolve(result)
        } catch (e: Exception) {
            Log.e("LocalMusic", "Error reading file", e)
            call.reject("Error reading file: ${e.message}")
        }
    }

    private fun scanDirectory(dir: File, results: MutableList<JSObject>) {
        try {
            val files = dir.listFiles() ?: return

            for (file in files) {
                if (file.isDirectory && !file.name.startsWith(".")) {
                    // Recursively scan subdirectories
                    scanDirectory(file, results)
                } else if (file.isFile) {
                    val extension = file.extension.lowercase()
                    if (extension in AUDIO_EXTENSIONS) {
                        try {
                            val fileObj = JSObject()
                            fileObj.put("name", file.name)
                            fileObj.put("path", file.absolutePath)
                            fileObj.put("size", file.length())
                            fileObj.put("lastModified", file.lastModified())
                            results.add(fileObj)
                            Log.d("LocalMusic", "Found audio file: ${file.name}")
                        } catch (e: JSONException) {
                            Log.e("LocalMusic", "Error adding file to results", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LocalMusic", "Error scanning directory: ${dir.absolutePath}", e)
        }
    }

    override fun handleOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_DIRECTORY && data != null) {
            val treeUri = data.data
            if (treeUri != null) {
                try {
                    val path = getPathFromUri(treeUri)
                    if (path != null) {
                        val files = mutableListOf<JSObject>()
                        scanDirectory(File(path), files)

                        val result = JSObject()
                        result.put("files", JSONArray(files))
                        result.put("path", path)
                        pendingCall?.resolve(result)
                    } else {
                        pendingCall?.reject("Could not access selected folder")
                    }
                } catch (e: Exception) {
                    Log.e("LocalMusic", "Error processing folder selection", e)
                    pendingCall?.reject("Error: ${e.message}")
                }
            }
            pendingCall = null
        }
    }

    private fun getPathFromUri(uri: Uri): String? {
        return when {
            uri.scheme == "file" -> uri.path
            uri.scheme == "content" -> {
                // Try to get the path from document provider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":")
                if (split.size >= 2) {
                    if ("primary" == split[0]) {
                        "${Environment.getExternalStorageDirectory()}/${split[1]}"
                    } else {
                        "/storage/${split[0]}/${split[1]}"
                    }
                } else {
                    null
                }
            }
            else -> null
        }
    }
}
