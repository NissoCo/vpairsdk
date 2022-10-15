package com.walabot.home.ble.pairing

data class ConfigParams(var cloudBaseUrl: String? = null,
                        var cloudRegion: String? = null,
                        var cloudProjectId: String? = null,
                        var mqttUrl: String? = null,
                        var mqttPort: Int? = null)

data class Gen2CloudOptions(var params: ConfigParams? = null,
                            val mqttUserName: String = "unused",
                            val mqttPassword: String = "unused",
                            val mqttClientId: String = "unused",
                            val mqttRegistryId: String = "walabot_home_gen2",
                            val ntpUrl: String = "pool.ntp.org")



