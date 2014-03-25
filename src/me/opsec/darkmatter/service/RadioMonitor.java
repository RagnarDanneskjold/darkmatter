package me.opsec.darkmatter.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

// TODO: Not sure if this should be a service, but might be the only way to make it work.
public class RadioMonitor extends Service {
    /*
     * TODO: add support for testing whether WiFi is also unavailable.
     * 
     * http://developer.android.com/reference/android/net/wifi/WifiManager.html#getScanResults()
     * 
     * only available after: http://developer.android.com/reference/android/net/wifi/WifiManager.html#SCAN_RESULTS_AVAILABLE_ACTION
     * 
     * 
     * if (serviceState == OUT_OF_SERVICE) {
     *  if (wifiManager.isWifiEnabled() == true) {
     *      wifiManager.startScan();
     *      beacons = wifiManager.getScanResults();
     *      if (beacons.length() == 0) {
     *          send_security_event( RADIO_LOST );
     *      }
     *  } else { // no wifi available
     *  send_security_event( RADIO_LOST );
     * } 
     */

    private class PhoneListener extends PhoneStateListener {

        public void onServiceStateChanged(ServiceState serviceState) {
            if (serviceState.getState() == ServiceState.STATE_OUT_OF_SERVICE) {
                Intent intent = new Intent(RadioMonitor.this, DarkService.class);
                intent.setAction(DarkService.ACTION_RADIO_LOST);
                startService(intent);
            }
        }
    }

    public void init() {
        TelephonyManager telephony = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephony.listen(new PhoneListener(), PhoneStateListener.LISTEN_SERVICE_STATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not used
    }
}
