package com.example.vpairsdk_flutter.ble.sdk

import com.example.vpairsdk_flutter.ble.Message.CLOUD_TYPE
import org.json.JSONException
import org.json.JSONObject

data class WifiModel(
    var ssid: String? = null,
    var bssid: String? = null,
    var password: String? = null
    )

data class Cloud(
    var registryId: String = "walabot_home_gen2",
    var cloudRegion: String = "us-central1",
    var projectName: String = "",
    var cloudType: CLOUD_TYPE = CLOUD_TYPE.GOOLE_CLOUD
)

data class MQTT(
    var hostUrl: String = "mqtts://mqtt.googleapis.com",
    var port: Int = 443,
    var userName: String = "unused",
    var password: String = "unused",
    var clientId: String = "unused",
    var ntpUrl: String = "pool.ntp.org"
)

class Config {
    lateinit var apiURL: String
    var userId: String? = null
    var accessToken: String? = null

    val wifi: WifiModel by lazy {
        WifiModel()
    }

    val cloud: Cloud by lazy {
        Cloud()
    }

    val mqtt: MQTT by lazy {
        MQTT()
    }

    val updateCloud: Boolean
    get() {
        return userId != null
    }


    companion object {
        val dev: Config
        get() {
            return Config().apply {
                apiURL = "https://dev.vayyarhomeapisdev.com"
                cloud.projectName = "walabothome-app-cloud"
            }
        }

        val prod: Config
        get() {
            return Config().apply {
                apiURL = "https://home.vayyarhomeapisdev.com"
                cloud.projectName = "walabot-home"
            }
        }

        fun custom(config: String): Config? {
            try {
                val json = JSONObject(config)
                val cnfg = Config()
                cnfg.userId = json.optString("userId")
                cnfg.apiURL = json.optString("apiURL")
                cnfg.accessToken = json.optString("accessToken")


                val cloudData = json.optJSONObject("cloud")
                cnfg.cloud.registryId = cloudData?.optString("registryId") ?: ""
                cnfg.cloud.cloudRegion = cloudData?.optString("cloudRegion") ?: ""
                cnfg.cloud.projectName = cloudData?.optString("projectName") ?: ""
                cloudData?.optInt("cloudType")?.let {
                    cnfg.cloud.cloudType = CLOUD_TYPE.forNumber(it)
                }

                val mqtt = json.optJSONObject("mqtt")
                cnfg.mqtt.hostUrl = mqtt?.optString("hostUrl") ?: ""
                cnfg.mqtt.port = mqtt?.optInt("port") ?: 443
                cnfg.mqtt.userName = mqtt?.optString("username") ?: ""
                cnfg.mqtt.password = mqtt?.optString("password") ?: ""
                cnfg.mqtt.clientId = mqtt?.optString("clientId")?: ""
                cnfg.mqtt.ntpUrl = mqtt?.optString("ntpUrl") ?: ""

                val wifiData = json.optJSONObject("wifi")
                cnfg.wifi.ssid = wifiData?.optString("ssid")
                cnfg.wifi.password = wifiData?.optString("password" )

                return cnfg
            } catch (e: JSONException) {
                e.printStackTrace()
                return null
            }
        }


    }
}