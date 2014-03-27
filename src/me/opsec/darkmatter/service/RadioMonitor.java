package me.opsec.darkmatter.service;

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

public class RadioMonitor extends Service {

    private WifiManager mWifiManager;

    private PhoneStateListener mPhoneListener = new PhoneStateListener() {

        public void onServiceStateChanged(ServiceState serviceState) {
            if (serviceState.getState() == ServiceState.STATE_OUT_OF_SERVICE) {
                mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                if (mWifiManager.isWifiEnabled() == true) {
                    registerReceiver(mWifiReceiver, new IntentFilter(
                            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    mWifiManager.startScan();
                } else {
                    // No wifi available
                    sendRadioLostIntent();
                }
            }
        }
    };

    public BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            List<ScanResult> beacons = mWifiManager.getScanResults();
            if (beacons.size() == 0) {
                sendRadioLostIntent();
            }

            unregisterReceiver(this);
        }
    };

    private void sendRadioLostIntent() {
        Intent intent = new Intent(RadioMonitor.this, DarkService.class);
        intent.setAction(DarkService.ACTION_RADIO_LOST);
        startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TelephonyManager telephony = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephony.listen(mPhoneListener, PhoneStateListener.LISTEN_SERVICE_STATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not used
    }
}
