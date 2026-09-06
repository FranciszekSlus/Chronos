package com.tasker.chronos.utils

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Eksport / import danych aplikacji jako ZIP:
 * - pliki DataStore (*.preferences_pb)
 * - notes.json, shopping.json
 */
object DataBackupManager {

    private val DATASTORE_NAMES = listOf(
        "tasks",
        "habits",
        "goals",
        "custom_events",
        "day_schedules",
        "app_settings",
        "user_profile"
    )

    private val JSON_FILES = listOf(
        "notes.json",
        "shopping.json"
    )

    fun suggestedFileName(): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
        return "chronos-backup-$stamp.zip"
    }

    fun exportToUri(context: Context, uri: Uri): Result<Unit> = runCatching {
        val app = context.applicationContext
        val datastoreDir = File(app.filesDir, "datastore")

        app.contentResolver.openOutputStream(uri)?.use { out ->
            ZipOutputStream(BufferedOutputStream(out)).use { zip ->
                val manifest = JSONObject()
                    .put("app", "chronos")
                    .put("version", 1)
                    .put("exportedAt", System.currentTimeMillis())
                    .toString()
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(manifest.toByteArray(Charsets.UTF_8))
                zip.closeEntry()

                DATASTORE_NAMES.forEach { name ->
                    val file = File(datastoreDir, "$name.preferences_pb")
                    if (file.exists() && file.length() > 0L) {
                        addFileToZip(zip, file, "datastore/${file.name}")
                    }
                }

                JSON_FILES.forEach { name ->
                    val file = File(app.filesDir, name)
                    if (file.exists() && file.length() > 0L) {
                        addFileToZip(zip, file, name)
                    }
                }
            }
        } ?: error("Nie udało się otworzyć pliku do zapisu")
    }

    fun importFromUri(context: Context, uri: Uri): Result<Unit> = runCatching {
        val app = context.applicationContext
        val datastoreDir = File(app.filesDir, "datastore").apply { mkdirs() }
        val allowedDatastore = DATASTORE_NAMES.map { "$it.preferences_pb" }.toSet()
        val allowedJson = JSON_FILES.toSet()

        app.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(BufferedInputStream(input)).use { zip ->
                var entry = zip.nextEntry
                var imported = 0
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val name = entry.name.removePrefix("./").replace('\\', '/')
                        when {
                            name.startsWith("datastore/") -> {
                                val fileName = name.substringAfterLast('/')
                                if (fileName in allowedDatastore) {
                                    writeEntry(zip, File(datastoreDir, fileName))
                                    imported++
                                }
                            }
                            name in allowedJson -> {
                                writeEntry(zip, File(app.filesDir, name))
                                imported++
                            }
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
                if (imported == 0) error("Archiwum nie zawiera danych Chronos")
            }
        } ?: error("Nie udało się otworzyć pliku ZIP")
    }

    private fun addFileToZip(zip: ZipOutputStream, file: File, entryName: String) {
        zip.putNextEntry(ZipEntry(entryName))
        FileInputStream(file).use { it.copyTo(zip) }
        zip.closeEntry()
    }

    private fun writeEntry(zip: ZipInputStream, target: File) {
        target.parentFile?.mkdirs()
        FileOutputStream(target).use { out -> zip.copyTo(out) }
    }
}
