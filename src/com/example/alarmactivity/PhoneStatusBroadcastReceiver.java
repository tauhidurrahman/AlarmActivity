package com.example.alarmactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStatusBroadcastReceiver extends BroadcastReceiver {
	SharedPreferences sharedPref;
	boolean isPhoneIdle;
	@Override
	public void onReceive(Context context, Intent intent) {
		sharedPref = context.getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
		
		MyPhoneStateListener phoneListener=new MyPhoneStateListener();
        TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        
        telephony.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
		
        
	}
	
	public class MyPhoneStateListener extends PhoneStateListener{
		public void onCallStateChanged(int state,String incomingNumber){
			
			switch(state)
	     	{
	           case TelephonyManager.CALL_STATE_IDLE:
	                Log.i("MainActivity", "IDLE");
	                isPhoneIdle=true;
	                break;
	           case TelephonyManager.CALL_STATE_OFFHOOK:
	                Log.i("MainActivity", "OFFHOOK");
	                isPhoneIdle=false;
	                break;
	           case TelephonyManager.CALL_STATE_RINGING:
	        	   isPhoneIdle=false;
	        	   break;
	     	}
			Log.i("MainActivity", ""+isPhoneIdle);
			//save reward in shared preferences
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("isPhoneIdle", isPhoneIdle);
            editor.commit();
			
		}
	}

}
