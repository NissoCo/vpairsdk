package com.walabot.home.ble.pairing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build

class WifiNetworkMonitor(var context: Context) {

    interface Scan {
        fun onNetworkStateChange(info: WifiInfo?)
    }

    private var receiver: WifiReceiver? = null

    var scanEvents: Scan? = null

    fun startScanWifi() {
        val filter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        if (Build.VERSION.SDK_INT >= 28) {
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        receiver = WifiReceiver()
        context.registerReceiver(receiver, filter)
    }

    fun stopScan() {
        receiver?.let {
            context.unregisterReceiver(it)
        }
    }

    inner class WifiReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            val wifiManager =
                (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            when (action) {
                WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                    val wifiInfo: WifiInfo? = if (intent.hasExtra(WifiManager.EXTRA_WIFI_INFO)) {
                        intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO)
                    } else {
                        wifiManager.connectionInfo
                    }
                    scanEvents?.onNetworkStateChange(wifiInfo)
                }
                LocationManager.PROVIDERS_CHANGED_ACTION ->
                    scanEvents?.onNetworkStateChange(wifiManager.connectionInfo)
            }
        }
    }

}