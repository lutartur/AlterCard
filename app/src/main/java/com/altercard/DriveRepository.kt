package com.altercard

import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "DriveRepository"
private const val FILE_NAME = "cards.json"
private const val APP_DATA_FOLDER = "appDataFolder"
private const val MIME_JSON = "application/json"

private data class DrivePayload(
    @SerializedName("cards") val cards: List<Card>,
    @SerializedName("uploadedAt") val uploadedAt: Long
)

class DriveRepository(private val credential: GoogleAccountCredential) {

    private val gson = Gson()

    private fun buildDriveService(): Drive =
        Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("AlterCard")
            .build()

    private suspend fun findFileId(service: Drive): String? = withContext(Dispatchers.IO) {
        val result = service.files().list()
            .setSpaces(APP_DATA_FOLDER)
            .setQ("name = '$FILE_NAME'")
            .setFields("files(id)")
            .execute()
        result.files?.firstOrNull()?.id
    }

    suspend fun upload(cards: List<Card>) = withContext(Dispatchers.IO) {
        val service = buildDriveService()
        val payload = DrivePayload(cards = cards, uploadedAt = System.currentTimeMillis())
        val json = gson.toJson(payload)
        val content = ByteArrayContent.fromString(MIME_JSON, json)

        val existingId = findFileId(service)
        if (existingId == null) {
            val metadata = File().apply {
                name = FILE_NAME
                parents = listOf(APP_DATA_FOLDER)
            }
            service.files().create(metadata, content).execute()
            Log.d(TAG, "Created $FILE_NAME on Drive")
        } else {
            service.files().update(existingId, null, content).execute()
            Log.d(TAG, "Updated $FILE_NAME on Drive")
        }
    }

    suspend fun download(): List<Card>? = withContext(Dispatchers.IO) {
        val service = buildDriveService()
        val fileId = findFileId(service) ?: return@withContext null
        val stream = service.files().get(fileId).executeMediaAsInputStream()
        val json = stream.bufferedReader().readText()
        val type = object : TypeToken<DrivePayload>() {}.type
        val payload: DrivePayload = gson.fromJson(json, type)
        Log.d(TAG, "Downloaded ${payload.cards.size} cards from Drive")
        payload.cards
    }
}
