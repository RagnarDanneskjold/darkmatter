package me.opsec.darkmatter.service;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.content.Intent;
import android.telephony.ServiceState;

public class RadioMonitor {
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
