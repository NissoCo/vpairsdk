package com.walabot.home.ble.sdk

import com.walabot.home.ble.Message.CLOUD_TYPE

data class WifiModel(
    var item: EspWifiItem? = null,
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
    var clientId: String = "unused"
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
    }
}