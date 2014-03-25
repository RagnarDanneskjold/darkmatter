package me.opsec.darkmatter.service;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.content.Intent;
import android.telephony.ServiceState;

public class RadioMonitor {
	/*
	 * TODO: add support for testing whether WiFi is also unavailable. 
	 * 
	 * http://developer.android.com/reference/android/net/wifi/WifiManager.html#getScanResults()
	 * 
	 * only available after: http://developer.android.com/reference/android/net/wifi/WifiManager.html#SCAN_RESULTS_AVAILABLE_ACTION
	 * 
	 * 
	 * if (serviceState == OUT_OF_SERVICE) {
	 * 	if (wifiManager.isWifiEnabled() == true) {
	 * 		wifiManager.startScan();
	 * 		beacons = wifiManager.getScanResults();
	 * 		if (beacons.length() == 0) {
	 * 			send_security_event( RADIO_LOST );
	 * 		}
	 * 	} else { // no wifi available
	 * 	send_security_event( RADIO_LOST );
	 * } 
	 */
	
	private class PhoneListener implements public PhoneStateListener {
		
		public void onServiceStateChanged (ServiceState serviceState) {
			if (serviceState.getState() == ServiceState.STATE_OUT_OF_SERVICE) {
				intent = new Intent(context, DarkService.class);
		        intent.setAction(DarkService.ACTION_RADIO_LOST);
		        context.startService(intent);
			}
		}
	}
	
	public void init() {
		TelephonyManager telephony = Context.getSystemService(Context.TELEPHONY_SERVICE);
		
		telephony.listen(new PhoneListener(), PhoneStateListener.LISTEN_SERVICE_STATE);
	}
}
