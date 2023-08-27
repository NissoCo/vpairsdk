package com.walabot.home.ble.sdk

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

val dev = "https://us-central1-walabothome-app-cloud.cloudfunctions.net/"
val prod = "https://us-central1-walabot-home.cloudfunctions.net/"
val vayyarCare = "https://us-central1-vayyar-care.cloudfunctions.net/"
val version = "3"

class Connection {

    @OptIn(DelicateCoroutinesApi::class)
    fun pairing(host: String, code: String, token: String, callback: (Result<Map<String, Any>>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL("${host}/pairing/$code")
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.requestMethod = "PUT"
            httpURLConnection.setRequestProperty(
                "Content-Type",
                "application/json"
            ) // The format of the content we're sending to the server
            httpURLConnection.setRequestProperty(
                "Accept",
                "application/json"
            ) // The format of response we want to get from the server
            httpURLConnection.setRequestProperty(
                "Authorization",
                "Bearer $token"
            )
            httpURLConnection.setRequestProperty(
                "Api-Version",
                version
            )
            httpURLConnection.doInput = true
            httpURLConnection.doOutput = true


            // Check if the connection is successful
            val responseCode = httpURLConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = httpURLConnection.inputStream.bufferedReader()
                    .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.IO) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response))
                    val map = Gson().fromJson<Map<String, Any>>(prettyJson, Map::class.java)
                    Log.d("Pretty Printed JSON :", prettyJson)
                    withContext(Dispatchers.IO) {
                        callback(Result.success(map))
                    }
                }
            } else {
                Log.e("HTTPURLCONNECTION_ERROR", responseCode.toString())
                withContext(Dispatchers.IO) {
                    callback(Result.failure(Throwable("Connection Error")))
                }
            }
        }
    }
}