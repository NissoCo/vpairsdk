package com.walabot.home.ble.sdk

import com.walabot.home.ble.pairing.PairingResponse
import java.net.HttpURLConnection
import java.net.URL

class Server {
    val baseUrlDev = "https://us-central1-walabothome-app-cloud.cloudfunctions.net/"
    val baseUrlProd = "https://us-central1-walabot-home.cloudfunctions.net/"

    fun cloudPairing(code: String, token: String, completion: (Result<Map<String, Any>>) -> Unit) {
        val url = URL("$baseUrlDev/pairing/$code")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.addRequestProperty("Content-Type", "application/json")
        connection.addRequestProperty("Authorization", "")
//        val req: RestRequest<PairingResponse> = RestRequest.createRequestBuilder<PairingResponse>(
//            PairingResponse::class.java
//        ).setBaseUrl(_baseUrl).setHttpMethod("PUT").setResUrl("pairing/$code").addHeader(
//            ServerAPI.HEADER_AUTH,
//            "Bearer $idToken"
//        ).addHeader(ServerAPI.HEADER_CONTENT_TYPE, "application/json").build()
    }
}