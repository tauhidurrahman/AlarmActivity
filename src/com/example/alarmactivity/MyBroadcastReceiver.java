/*This is started by "android.intent.action.BOOT_COMPLETED" and starts as soon as android completes boot*/

package com.example.alarmactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class MyBroadcastReceiver extends BroadcastReceiver{
	SharedPreferences sharedPref;
	int DefaultSampPeriodMin=10;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		sharedPref = context.getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
		boolean isRunning = sharedPref.getBoolean(context.getString(R.string.Flag_isRunning), false);
		int sampPeriodMin = sharedPref.getInt(context.getString(R.string.SamplingPeriod), DefaultSampPeriodMin);
        if(isRunning==true){
        	Intent intentServiceLocation = new Intent(context, ServiceLocation.class);
        	intentServiceLocation.putExtra("From", 1);//It carries signal as if starting from MainActivity
        	intentServiceLocation.putExtra("Delay", sampPeriodMin);
    		context.startService(intentServiceLocation);
        }
		
		Log.i("MainActivity", "onReceive of MYBroadcastReceiver!!");
	}

}
