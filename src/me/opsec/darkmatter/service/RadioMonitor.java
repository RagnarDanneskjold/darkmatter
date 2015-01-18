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
            checkSIMStateChanged();

            if (serviceState.getState() == ServiceState.STATE_OUT_OF_SERVICE) {
                mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                if (mWifiManager.isWifiEnabled()) {
                    registerReceiver(mWifiReceiver, new IntentFilter(
                            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    mWifiManager.startScan();
                } else {
                    // No wifi available
                    sendRadioLostIntent();
                }
            }
        }

        private void checkSIMStateChanged() {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

            if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
            	return;
            }
            if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
                sendSIMRemovedIntent();
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

    private void sendSIMRemovedIntent() {
        Intent intent = new Intent(RadioMonitor.this, DarkService.class);
        intent.setAction(DarkService.ACTION_SIM_REMOVED);
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
