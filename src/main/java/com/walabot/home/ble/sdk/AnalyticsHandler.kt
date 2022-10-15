package com.walabot.home.ble.sdk

enum class AnalyticsComponents(val value: String) {
    Action("action"),
    Category("category"),
    Label("label"),
    Event("event")
}

interface AnalyticsHandler {
    fun log(components: ArrayList<AnalyticsComponents>)
}