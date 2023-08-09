package com.walabot.home.ble.sdk

import com.walabot.home.ble.Message.CLOUD_TYPE
import org.json.JSONException
import org.json.JSONObject

data class WifiModel(
    var ssid: String? = null,
    var bssid: String? = null,
    var pwd: String? = null
    )

data class Cloud(
    var registryId: String = "walabot_home_gen2",
    var region: String = "us-central1",
    var name: String = "",
    var type: CLOUD_TYPE = CLOUD_TYPE.GOOLE_CLOUD
)

data class MQTT(
    var hostUrl: String = "mqtts://mqtt.googleapis.com",
    var port: Int = 443,
    var userName: String = "unused",
    var pwd: String = "unused",
    var clientId: String = "unused",
    var ntpUrl: String = "pool.ntp.org"
)

class Config {
    lateinit var url: String
    var userId: String? = null
    var token: String? = null

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
                url = "https://dev.vayyarhomeapisdev.com"
                cloud.name = "walabothome-app-cloud"
            }
        }

        val prod: Config
        get() {
            return Config().apply {
                url = "https://home.vayyarhomeapisdev.com"
                cloud.name = "walabot-home"
            }
        }

        fun custom(config: String): Config? {
            try {
                val json = JSONObject(config)
                val cnfg = Config()
                cnfg.url = json.optString("url")
                cnfg.token = json.optString("token")


                val cloudData = json.optJSONObject("cloud")
                cnfg.cloud.registryId = cloudData?.optString("reigstryId") ?: ""
                cnfg.cloud.region = cloudData?.optString("region") ?: ""
                cnfg.cloud.name = cloudData?.optString("name") ?: ""
                cloudData?.optInt("type")?.let {
                    cnfg.cloud.type = CLOUD_TYPE.forNumber(it)
                }

                val mqtt = json.optJSONObject("mqtt")
                cnfg.mqtt.hostUrl = mqtt?.optString("hostUrl") ?: ""
                cnfg.mqtt.port = mqtt?.optInt("port") ?: 443
                cnfg.mqtt.userName = mqtt?.optString("userName") ?: ""
                cnfg.mqtt.pwd = mqtt?.optString("pwd") ?: ""
                cnfg.mqtt.clientId = mqtt?.optString("clientId")?: ""
                cnfg.mqtt.ntpUrl = mqtt?.optString("ntpUrl") ?: ""

                val wifiData = json.optJSONObject("wifi")
                cnfg.wifi.ssid = wifiData?.optString("ssid")
                cnfg.wifi.pwd = wifiData?.optString("pwd" )

                return cnfg
            } catch (e: JSONException) {
                e.printStackTrace()
                return null
            }
        }


    }
}