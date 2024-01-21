package h.lillie.weartube.presentation

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class Extractor {
    suspend fun playerRequest(context: Context, videoID: String) : String = withContext(Dispatchers.IO) {
        val body: RequestBody = """{
            "context": {
                "client": {
                    "hl": "en",
                    "gl": "${context.resources.configuration.locales.get(0).country}",
                    "clientName": "IOS",
                    "clientVersion": "17.33.2",
                    "deviceModel": "iPhone14,3"
                }
            },
            "contentCheckOk": true,
            "racyCheckOk": true,
            "videoId": "$videoID"
        }""".trimIndent().toRequestBody()

        val client: OkHttpClient = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .method("POST", body)
            .header("User-Agent", "com.google.ios.youtube/17.33.2 (iPhone14,3; U; CPU iOS 15_6 like Mac OS X)")
            .url("https://www.youtube.com/youtubei/v1/player?key=AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc&prettyPrint=false")
            .build()

        return@withContext client.newCall(request).execute().body?.string()!!
    }

    suspend fun nextRequest(context: Context, videoID: String) : String = withContext(Dispatchers.IO) {
        val body: RequestBody = """{
            "context": {
                "client": {
                    "hl": "en",
                    "gl": "${context.resources.configuration.locales.get(0).country}",
                    "clientName": "IOS",
                    "clientVersion": "17.33.2",
                    "deviceModel": "iPhone14,3"
                }
            },
            "contentCheckOk": true,
            "racyCheckOk": true,
            "videoId": "$videoID"
        }""".trimIndent().toRequestBody()

        val client: OkHttpClient = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .method("POST", body)
            .header("User-Agent", "com.google.ios.youtube/17.33.2 (iPhone14,3; U; CPU iOS 15_6 like Mac OS X)")
            .url("https://www.youtube.com/youtubei/v1/next?key=AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc&prettyPrint=false")
            .build()

        return@withContext client.newCall(request).execute().body?.string()!!
    }

    suspend fun browseRequest(context: Context, browseID: String) : String = withContext(Dispatchers.IO) {
        val body: RequestBody = """{
            "context": {
                "client": {
                    "hl": "en",
                    "gl": "${context.resources.configuration.locales.get(0).country}",
                    "clientName": "IOS",
                    "clientVersion": "17.33.2",
                    "deviceModel": "iPhone14,3"
                }
            },
            "contentCheckOk": true,
            "racyCheckOk": true,
            "browseId": "$browseID"
        }""".trimIndent().toRequestBody()

        val client: OkHttpClient = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .method("POST", body)
            .header("User-Agent", "com.google.ios.youtube/17.33.2 (iPhone14,3; U; CPU iOS 15_6 like Mac OS X)")
            .url("https://www.youtube.com/youtubei/v1/browse?key=AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc&prettyPrint=false")
            .build()

        return@withContext client.newCall(request).execute().body?.string()!!
    }

    suspend fun searchRequest(context: Context, query: String) : String = withContext(Dispatchers.IO) {
        val body: RequestBody = """{
            "context": {
                "client": {
                    "hl": "en",
                    "gl": "${context.resources.configuration.locales.get(0).country}",
                    "clientName": "WEB",
                    "clientVersion": "2.20220801.00.00"
                }
            },
            "contentCheckOk": true,
            "racyCheckOk": true,
            "query": "$query"
        }""".trimIndent().toRequestBody()

        val client: OkHttpClient = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .method("POST", body)
            .url("https://www.youtube.com/youtubei/v1/search?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8&prettyPrint=false")
            .build()

        return@withContext client.newCall(request).execute().body?.string()!!
    }
}