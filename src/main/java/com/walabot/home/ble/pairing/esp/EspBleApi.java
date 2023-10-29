package com.walabot.home.ble.pairing.esp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.GeneratedMessageV3;
import com.walabot.home.ble.BleDevice;
import com.walabot.home.ble.Message;
import com.walabot.home.ble.Result;
import com.walabot.home.ble.WHBle;
import com.walabot.home.ble.WHConnectionCallback;
import com.walabot.home.ble.WHDataCallback;
import com.walabot.home.ble.pairing.WifiNetworkMonitor;
import com.walabot.home.ble.sdk.AnalyticsHandler;
import com.walabot.home.ble.sdk.Config;
import com.walabot.home.ble.sdk.EspPairingEvent;
import com.walabot.home.ble.sdk.EspWifiItem;
import com.walabot.home.ble.sdk.EspWifiItemImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class EspBleApi implements EspApi {

    public interface OnResult {
        void onResult(Result<EspPairingEvent> result, EspBleApi espBleApi);
    }
    private WalabotDeviceDesc walabotDescription;
    public Config config;
    public OnResult callback;
    private Map<String, Object> devInfo;
    public String deviceId;
    private AnalyticsHandler analyticsHandler;

    public Map<String, Object> getDevInfo() {
        if (devInfo == null && deviceId != null) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("devID", deviceId);
            return temp;
        }
        return devInfo;
    }

    public void setAnalyticsHandler(AnalyticsHandler analyticsHandler) {
        this.analyticsHandler = analyticsHandler;
    }

    public static class ESPBleAPIImpl implements WHConnectionCallback, WHDataCallback, AnalyticsHandler {
        interface OpCallback {
            void onData(OpResult result);
        }

        interface ConnectionCallback {
            void onConnectionResult(Result<BluetoothDevice> result);
        }
        private AnalyticsHandler analyticsHandler;
        private final WHBle _whBle;
        private final AtomicReference<OpCallback> _opCallback =
                new AtomicReference<>();
        private final AtomicReference<ConnectionCallback> _connectionCallback =
                new AtomicReference<>();

        ProtocolMediator messageImpl;

        private AnalyticsHandler getAnalyticsHandler() {
            if (analyticsHandler == null) {
                return this;
            }
            return analyticsHandler;
        }

        public ESPBleAPIImpl(Context context) {
            _whBle = new WHBle(context);
        }

        public void sendMessage(Message.ToDeviceMessageType type, @Nullable GeneratedMessageV3 payload, OpCallback cb) {
            _opCallback.set(cb);
            byte[] data = EspApi.Companion.generateOutGoingMessage(type, payload);
            boolean retVal = _whBle.sendData(data);
            if (!retVal) {
                cb.onData(new OpResult(ERR_WRITE_FAILED, null));
            }
        }

        public boolean isBleEnabled() {
            return _whBle.isAdapterOn();
        }

        public boolean isConnected() {
            return _whBle.isConnectionReady();
        }


        public void connect(BleDevice bleDevice, ConnectionCallback cb) {
            _connectionCallback.set(cb);
            _whBle.connectToDevice(bleDevice.getDevice(), ESPBleAPIImpl.this,
                    ESPBleAPIImpl.this,
                    10000);
        }

        public void disconnect() {
            BluetoothDevice device = _whBle.getSelectedDevice();
            if (device != null) {
                _whBle.disconnect(device, true);
            }
            _opCallback.set(null);
            _connectionCallback.set(null);
        }


        @Override
        public void log(@NonNull String message, @Nullable String deviceId) {
            Log.d("Analytics", message + "deviceId: " + deviceId);
        }


        //region WHConnectionCallback
        @Override
        public void onDeviceConnected(BluetoothDevice device, int version) {
            switch (version) {
                case 1:
                    messageImpl = new ProtobufMessagesV1();
                    break;
                case 3:
                    messageImpl = new ProtobufMessagesV3();
                    break;
                default:
                    messageImpl = null;
            }
        }

        @Override
        public final void onDeviceDisconnected(BluetoothDevice device) {
            ConnectionCallback cb = _connectionCallback.getAndSet(null);
            if (cb != null) {
                cb.onConnectionResult(new Result<>(device));
            }
            OpCallback opCallback = _opCallback.getAndSet(null);
            if (opCallback != null) {
                opCallback.onData(new OpResult(ERR_DISCONNECTED, null));
            }
            messageImpl = null;
        }

        @Override
        public void onDeviceConnectionFailed(BluetoothDevice device) {

        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {

        }
        //endregion

        //region WHDataCallback
        @Override
        public final void onReadSuccess(byte[] data) {
            getAnalyticsHandler().log("On read Characteristics", Objects.requireNonNull(_whBle.getConnectedDevice()).getAddress());
            OpCallback opCallback = _opCallback.getAndSet(null);
            if (opCallback != null) {
                opCallback.onData(new OpResult(0, data));
            }
        }

        @Override
        public final void onReadFailed() {
            getAnalyticsHandler().log("On read Characteristics failed", Objects.requireNonNull(_whBle.getConnectedDevice()).getAddress());
            OpCallback opCallback = _opCallback.getAndSet(null);
            if (opCallback != null) {
                opCallback.onData(new OpResult(ERR_READ_FAILED, null));
            }
        }

        @Override
        public void onWriteSuccess(byte[] value) {
            getAnalyticsHandler().log("On write Characteristics", Objects.requireNonNull(_whBle.getConnectedDevice()).getAddress());
        }

        @Override
        public final void onWriteFailed(byte[] value) {
            getAnalyticsHandler().log("On write Characteristics failed", Objects.requireNonNull(_whBle.getConnectedDevice()).getAddress());
            OpCallback opCallback = _opCallback.getAndSet(null);
            if (opCallback != null) {
                opCallback.onData(new OpResult(ERR_WRITE_FAILED, null));
            }
        }

        @Override
        public void onNotificationEnabled(boolean enabled) {
            BluetoothDevice device = _whBle.getConnectedDevice();
            ConnectionCallback cb = _connectionCallback.getAndSet(null);
            if (cb != null) {
                cb.onConnectionResult(new Result<>(device));
            }
        }
        //endregion
    }

    private final ESPBleAPIImpl _espBleImpl;
    private final Context _context;
    WifiNetworkMonitor monitor;

    public EspBleApi(Context context, Config config, OnResult callback) {
        _context = context;
        _espBleImpl = new ESPBleAPIImpl(context);
        _espBleImpl.analyticsHandler = analyticsHandler;
        this.config = config;
        this.callback = callback;
    }

    @Override
    public boolean isBleEnabled() {
        return _espBleImpl.isBleEnabled();
    }

    @Override
    public boolean isConnected() {
        return _espBleImpl.isConnected();
    }

    @Override
    public WalabotDeviceDesc getDeviceDescriptor() {
        return walabotDescription;
    }

    @Override
    public void connect(@Nullable EspAPICallback<WalabotDeviceDesc> cb) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void connect(@NonNull BleDevice bleDevice, EspAPICallback<WalabotDeviceDesc> cb) {
        _espBleImpl.connect(bleDevice, (result) ->
        {
            if (_espBleImpl.messageImpl == null) {
                cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_FIND_SERVICE, null));
                return;
            }

            if (result.isSuccessfull()) {
//                if (result.getResult() != null && result.getResult().getName() != null) {
//
//                }
                walabotDescription = new WalabotDeviceDesc(bleDevice.getDevice().getAddress(), "", result.getResult().getName());
                walabotDescription.setProtocolVersion(_espBleImpl._whBle.getProtocolVersion());
                fetchDevInfo(cb);
            } else {
                Log.i(EspBleApi.class.getName(), "connect result failed");
                cb.onFailure(result.getThrowable());
            }
        });
    }

    private void fetchDevInfo(EspAPICallback<WalabotDeviceDesc> cb) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                cb.onSuccess(walabotDescription);
            }
        }, 3000);
        _espBleImpl.sendMessage(Message.ToDeviceMessageType.GET_DEV_INFO, null, (dataResult) -> {
            if (dataResult.isSuccessful()) {
                devInfo = _espBleImpl.messageImpl.parseDevInfoResult(dataResult.getData());
                timer.cancel();
                cb.onSuccess(walabotDescription);
            }
        });
    }

    @Override
    public void sendWiFiScanRequest(EspAPICallback<List<EspWifiItem>> cb) {
        if (_espBleImpl.messageImpl == null || !_espBleImpl.isConnected()) {
            cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_FIND_SERVICE, null));
            return;
        }
        if (_espBleImpl.messageImpl instanceof ProtobufMessagesV1) {
            monitor = new WifiNetworkMonitor(_context);
            monitor.setScanEvents(info -> {
                ArrayList<EspWifiItem> list = new ArrayList<>();
                if (info != null) {
                    list.add(new EspWifiItemImpl(info.getSSID().replaceAll("\"", ""), info.getBSSID(), info.getRssi()));
                }
                cb.onSuccess(list);
            });
            monitor.startScanWifi();
        } else {
            _espBleImpl.sendMessage(Message.ToDeviceMessageType.DO_WIFI_SCAN, null, (dataResult) ->
            {
                if (dataResult == null || dataResult.getData() == null) {
                    EspPairingException e = new EspPairingException("error", 0, 0);
                    cb.onFailure(e);
                    return;
                }

                ProtocolMediator.WifiScanResult scanResult = _espBleImpl.messageImpl.parseWifiScanResult(dataResult.getData());
                if (scanResult.isSuccessful()) {
                    ArrayList<EspWifiItem> items = new ArrayList<>();
                    for (Message.AccessPoint accessPoint : Objects.requireNonNull(scanResult.getAccessPoints())) {
                        items.add(new EspWifiItemImpl(accessPoint.getSsid(), accessPoint.getBssid(), accessPoint.getRssi()));
                    }
                    cb.onSuccess(items);
                } else {
                    EspPairingException e = new EspPairingException(scanResult.toString(),
                            scanResult.getResult(),
                            scanResult.getEsp_error());
                    cb.onFailure(e);
                }

            });
        }

    }

    @Override
    public void sendWifiCredentials(byte[] ssid, byte[] bssid, byte[] password, EspAPICallback<WalabotDeviceDesc> cb) {
        if (_espBleImpl.messageImpl == null) {
            cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_FIND_SERVICE, null));
            return;
        }

        GeneratedMessageV3 message = _espBleImpl.messageImpl.wifiCredentials(ssid, bssid, password);
        _espBleImpl.sendMessage(Message.ToDeviceMessageType.CONNECT_WIFI, message, (dataResult) ->
        {
            if (dataResult.isSuccessful()) {
                ProtocolMediator.WifiResult wifiResult = _espBleImpl.messageImpl.parseWifiResult(dataResult.getData());
                if (wifiResult != null) {
                    if (wifiResult.isSuccessful() && wifiResult.getMac() != null && !wifiResult.getMac().isEmpty()) {
//                        String name = walabotDescription.getName();
//                        walabotDescription = new WalabotDeviceDesc(wifiResult.getMac(), wifiResult.getIp(), name);
                        cb.onSuccess(walabotDescription);
                    } else {
                        // we got a valid status, we parsed the payload, but the IP/MAC was
                        // broken, or we got invalid status (like network is local only)
                        EspPairingException e = new EspPairingException("Wrong Wifi Code", wifiResult.getResult(), 0);
                        cb.onFailure(e);
                    }
                }
            } else {
                EspPairingException e = new EspPairingException(EspPairingErrorType.CONNECT_FAILED, dataResult);
                cb.onFailure(e);
            }
        });
    }

    @Override
    public void sendCloudDetails(@NonNull Config config, EspAPICallback<Void> cb) {
        if (_espBleImpl.messageImpl == null) {
            cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_FIND_SERVICE, null));
            return;
        }

        GeneratedMessageV3 message = _espBleImpl.messageImpl.cloudDetails(config);
        _espBleImpl.sendMessage(Message.ToDeviceMessageType.CLOUD_CONNECT, message, (opResult) ->
        {
            ProtocolMediator.MessageResult r = _espBleImpl.messageImpl.parseResult(opResult.getData());
            if (r == null) {
                cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_PARSE_CONNECT_RESULT, opResult));
                return;
            }
            if (r.isSuccessful()) {
                cb.onSuccess(null);
            } else {
                EspPairingException e = new EspPairingException(opResult.toString(),
                        r.getResult(),
                        r.getEsp_error());
                cb.onFailure(e);
            }
        });
    }

    @Override
    public void pair(String hostAddress, String uid, EspAPICallback<EspPairingResponse> cb) {
        if (_espBleImpl.messageImpl == null) {
            cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_FIND_SERVICE, null));
            return;
        }

        GeneratedMessageV3 message = _espBleImpl.messageImpl.pair(uid);
        _espBleImpl.sendMessage(Message.ToDeviceMessageType.PAIR_TO_PHONE, message, result ->
        {
            ProtocolMediator.PairResult pairResult = _espBleImpl.messageImpl.parsePairResult(result.getData());
            if (pairResult != null) {
                cb.onSuccess(new EspPairingResponse(pairResult.getCode()));
            } else {
                cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_PARSE_PAIR_RESULT, result));
            }
        });
    }

    @Override
    public void notifyPairingComplete(String hostAddress, String uid, String code, EspAPICallback<Void> cb) {
        if (_espBleImpl.messageImpl == null) {
            cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_FIND_SERVICE, null));
            return;
        }

        GeneratedMessageV3 message = _espBleImpl.messageImpl.pairingComplete(uid, code);
        _espBleImpl.sendMessage(Message.ToDeviceMessageType.PAIR_TO_PHONE_COMPLETE, message, opResult ->
        {
            ProtocolMediator.MessageResult r = _espBleImpl.messageImpl.parseResult(opResult.getData());
            if (r == null) {
                cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_PARSE_CONNECT_RESULT, opResult));
                return;
            }
            if (r.isSuccessful()) {
                cb.onSuccess(null);
            } else {
                cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_SEND_CLOUD_DETAILS, opResult));
            }
        });
    }

    @Override
    public void checkOta(EspAPICallback<CheckOtaResult> cb) {
        if (_espBleImpl.messageImpl == null) {
            cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_FIND_SERVICE, null));
            return;
        }

        cb.onSuccess(new CheckOtaResult(false, 0, 0));
    }

    @Override
    public void performOta(int versionCode, EspAPICallback<Void> cb) {
        if (_espBleImpl.messageImpl == null) {
            cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_FIND_SERVICE, null));
            return;
        }

        cb.onSuccess(null);
    }

    @Override
    public void reboot(EspAPICallback<EspPairingEvent> cb) {
        if (devInfo != null) {
            cb.onSuccess(EspPairingEvent.CommittingProvision);
            commitProvision(cb);
        } else {
            if (_espBleImpl.messageImpl == null) {
                cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_FIND_SERVICE, null));
                return;
            }

            _espBleImpl.sendMessage(Message.ToDeviceMessageType.DO_REBOOT_OPERATIONAL, null, opResult ->
            {
                ProtocolMediator.MessageResult r = _espBleImpl.messageImpl.parseResult(opResult.getData());
                if (r == null) {
                    cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_PARSE_CONNECT_RESULT, opResult));
                    return;
                }
                if (r.isSuccessful()) {
                    cb.onSuccess(EspPairingEvent.Rebooting);
                } else {
                    cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_SEND_CLOUD_DETAILS, opResult));
                }
            });
        }
    }

    @Override
    public void rebootToFactory(EspAPICallback<Void> cb) {
        if (_espBleImpl.messageImpl == null) {
            cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_FIND_SERVICE, null));
            return;
        }

        // We should remove
        _espBleImpl.sendMessage(Message.ToDeviceMessageType.DO_REBOOT_FACTORY, null, opResult ->
        {
            ProtocolMediator.MessageResult r = _espBleImpl.messageImpl.parseResult(opResult.getData());
            if (r == null || r.isSuccessful()) {
                cb.onSuccess(null);
            } else {
                cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_SEND_CLOUD_DETAILS, opResult));
            }
        });
    }

    @Override
    public void commitProvision(EspAPICallback<EspPairingEvent> cb) {
        if (_espBleImpl.messageImpl == null) {
            cb.onFailure(new EspPairingException(EspPairingErrorType.FAILED_TO_FIND_SERVICE, null));
            return;
        }
        _espBleImpl.sendMessage(Message.ToDeviceMessageType.COMMIT_PROVISION, null, opResult ->
        {
            ProtocolMediator.MessageResult r = _espBleImpl.messageImpl.parseResult(opResult.getData());
            if (r == null || r.isSuccessful()) {
                cb.onSuccess(EspPairingEvent.ProvisionCommited);
            } else {
                cb.onFailure(new EspPairingException(EspPairingErrorType.COMMIT_PROVISION_FAILED, opResult));
            }
        });
    }

    @Override
    public void stop() {
        Log.i(EspBleApi.class.getName(), "disconnect");
        _espBleImpl.disconnect();

    }
}
