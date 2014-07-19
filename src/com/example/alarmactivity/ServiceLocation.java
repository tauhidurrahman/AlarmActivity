package com.example.alarmactivity;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

public class ServiceLocation extends Service{
    private LocationManager locMan;
    private Handler handler = new Handler();
    private int delayMinute;
    private int intentSource;
    final static int myID = 1234;
    SharedPreferences sharedPref;

    public static Location curLocation;

    LocationListener gpsListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        	Boolean locationChanged=false;
        	
            if (curLocation == null) {
                locationChanged = true;
            }
            else if (curLocation.getTime() > location.getTime()){
                locationChanged = false;
                return;
            }
            else if (curLocation.getTime() < location.getTime()){
                locationChanged = true;
            }
            
            if (locationChanged==true){

                curLocation = location;
            	//Log.i("MainActivity", "onLocationChanged in ServiceLocation " + curLocation.getLatitude() + " " + curLocation.getProvider());
            	//Toast.makeText(getBaseContext(), "Location Found " + curLocation.getLatitude(), Toast.LENGTH_LONG).show();
                //locMan.removeUpdates(gpsListener);
            }
        }
        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status,Bundle extras) {
            if (status == 0)// UnAvailable
            {
            } else if (status == 1)// Trying to Connect
            {
            } else if (status == 2) {// Available
            }
        }

    };
    
    private final IBinder mBinder = new LocalBinder();// Binder given to clients which is a interface for client to bind
    
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	ServiceLocation getService() {
            // Return this instance of LocalService so clients can call public methods
            return ServiceLocation.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.i("MainActivity", "creating ServiceLocation");
        curLocation = null;
        
    }
    
    PowerManager mgr;
    WakeLock wakeLock;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.i("MainActivity", "onStartCommand ServiceLocation");
    	sharedPref = getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
    	
    	//Here I am starting the wake lock thingy
        mgr = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();
    	
    	// This part is for starting the service in foreground with a notification//
    	//The intent to launch when the user clicks the expanded notification
    	Intent intentExpandNotification = new Intent(this, MainActivity.class);
    	intentExpandNotification.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);
    	
    	//THis Notification constructor has deprecated, but good to support old phones
        Notification notification = new Notification(R.drawable.ic_action_search, getText(R.string.ticker_text), System.currentTimeMillis());
        
        //This method is deprecated. Use Notification.Builder instead.
        notification.setLatestEventInfo(this, "AnthropoSophia", "Crowdsourced Mobile Data Collection", pendIntent);
        
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(myID, notification);
        //Finished setting up notification and startForeground
    	
    	delayMinute = intent.getExtras().getInt("Delay");
  	    intentSource = intent.getExtras().getInt("From");
  	    Log.i("MainActivity", "Starting ServiceLocation from " + intentSource);
  	  
  	    if(intentSource==1){
  	  	    nthTryInOnePeriod=-1;
  		    handler.removeCallbacks(GpsFinderStart);
  		    handler.removeCallbacks(GpsFinderStop);
  	        handler.postDelayed(GpsFinderStart,1);
  	    }else{
  		  handler.removeCallbacks(GpsFinderManualCapture);
  		  handler.postDelayed(GpsFinderManualCapture,1);
  	    }
    	
    	return super.onStartCommand(intent, flags, startId);
    }
    
   private int nthTryInOnePeriod;
   private int NTryInOnePeriod=4;
   private boolean locCapturedInThisPeriod;
   private final int LocSensorEnableTimeInSec=20;
   private long startTimeOfThisPeriod;
   
   public Runnable GpsFinderStart = new Runnable(){
	    public void run(){
	    	handler.postDelayed(GpsFinderStart,delayMinute*60*1000/NTryInOnePeriod);
	    	nthTryInOnePeriod=(nthTryInOnePeriod+1) % NTryInOnePeriod; 
	    	if(nthTryInOnePeriod==0){
	    		locCapturedInThisPeriod=false;
	    		startTimeOfThisPeriod=System.currentTimeMillis();
	    	}
	    	switch (nthTryInOnePeriod) {
	            case 0: 
	            		handler.postDelayed(GpsFinderStop,LocSensorEnableTimeInSec*1000); 
	            		getBestLocation();
	            		Log.i("MainActivity", "GpsFinder Start first try");
	                    break;
	            case 1:  
	            		 if(locCapturedInThisPeriod==false){
            				 handler.postDelayed(GpsFinderStop,LocSensorEnableTimeInSec*1000);
	            			 getBestLocation();
	            			 Log.i("MainActivity", "GpsFinder Start Second try");
	            			 
	            		 }
	                     break;
	            case 2:  
			             if(locCapturedInThisPeriod==false){
		            		 handler.postDelayed(GpsFinderStop,LocSensorEnableTimeInSec*1000);
		           			 getBestLocation();
		           			 Log.i("MainActivity", "GpsFinder Start third try");
		           		 }
	                     break;
	            case 3:  
			             if(locCapturedInThisPeriod==false){
		            		 handler.postDelayed(GpsFinderStop,LocSensorEnableTimeInSec*1000);
		           			 getBestLocation();
		           			 Log.i("MainActivity", "GpsFinder Start fourth try");
		           		 }
	                     break;
	        }
	        
  	}
  };

	 public Runnable GpsFinderStop = new Runnable(){
		    public void run(){
		    	Log.i("MainActivity", "GpsFinderStop ServiceLocation");
		    	//Location tempLoc = getBestLocation();
		    	Location tempLoc = getCurrentLocation();
		    	locMan.removeUpdates(gpsListener);
		        if(tempLoc!=null){
		        	//if tempLoc is latest, then replace it
		        	if(tempLoc.getTime()>startTimeOfThisPeriod){
		        		curLocation = tempLoc;
		        		locCapturedInThisPeriod=true;
		        		Log.i("MainActivity", "onLocationChanged in ServiceLocation " + curLocation.getLatitude() + " " + curLocation.getProvider());
		        		
		        	}
		        }
		        tempLoc = null;
		        
		        Date date = new Date(startTimeOfThisPeriod);
		        DateFormat df_Hour = new SimpleDateFormat("HH");//24hrs format
		        int currentHour = Integer.parseInt(df_Hour.format(date));// from 0 to 23
		        Log.i("MainActivity", "Hour in 24 format:"+ df_Hour.format(date));
		        boolean isNight = currentHour>=0 && currentHour<=6;
		        
		        // if location is captured in this period, and no ongoing call and not night
		        if(locCapturedInThisPeriod==true && sharedPref.getBoolean("isPhoneIdle", true)==true &&  isNight==false){
		        	//if intent starting only from MainActivity then pass intent to AudioRecord else avoid this step
			        	//intentSource==1
				        Intent intentServiceAudioCapture = new Intent(getApplicationContext(), ServiceAudioRecord.class);//ServiceAudioCapture
				        intentServiceAudioCapture.putExtra("Lat", ""+curLocation.getLatitude());
				        intentServiceAudioCapture.putExtra("Lon", ""+curLocation.getLongitude());
				        intentServiceAudioCapture.putExtra("Alt", ""+curLocation.getAltitude());
				        intentServiceAudioCapture.putExtra("Bea", ""+curLocation.getBearing());
				        intentServiceAudioCapture.putExtra("Pr", ""+curLocation.getProvider());
				        intentServiceAudioCapture.putExtra("Sp", ""+curLocation.getSpeed());
				        intentServiceAudioCapture.putExtra("T", ""+curLocation.getTime());
				        intentServiceAudioCapture.putExtra("intSrc", ""+intentSource);
				        getApplicationContext().startService(intentServiceAudioCapture);
		        }
		    }
	};
 
   @Override
   public IBinder onBind(Intent arg0) {
	   Log.i("MainActivity", "onBind ServiceLocation");
	   handler.removeCallbacks(GpsFinderManualCapture);
	   handler.postDelayed(GpsFinderManualCapture,1);
       return mBinder;
   }
   
   public Runnable GpsFinderManualCapture = new Runnable(){
	    public void run(){
	        getBestLocation();
    	}
   };
   
   
    @Override
	public boolean onUnbind(Intent intent) {
    	Log.i("MainActivity", "onUnBind ServiceLocation");
    	locMan.removeUpdates(gpsListener);
		//return super.onUnbind(intent);
    	//returning true because of Rebinding
    	return true;
	}
    
    

    @Override
	public void onRebind(Intent intent) {
    	Log.i("MainActivity", "onReBind ServiceLocation");
    	handler.removeCallbacks(GpsFinderManualCapture);
 	    handler.postDelayed(GpsFinderManualCapture,1);
		super.onRebind(intent);
	}



	@Override
   public void onDestroy() {
       handler.removeCallbacks(GpsFinderManualCapture);
       handler.removeCallbacks(GpsFinderStart);
       handler.removeCallbacks(GpsFinderStop);
       handler = null;
       locMan.removeUpdates(gpsListener);
       locMan=null;
       curLocation=null;
       Log.i("MainActivity", "Destroying ServiceLocation");
       stopService(new Intent(getBaseContext(),ServiceAudioRecord.class));//ServiceAudioCapture
       
       //releasing Wakelock
       wakeLock.release();
   }

     Location getBestLocation() {
        Location gpslocation = null;
        Location networkLocation = null;
        
        if(locMan==null){
          locMan = (LocationManager) getApplicationContext() .getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            if(locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 0, gpsListener);// here you can set the 2nd argument time interval also that after how much time it will get the gps location
                gpslocation = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }else{
            	Toast.makeText(getBaseContext(),"Please enable GPS Location Services!",Toast.LENGTH_SHORT).show();
            }
            
            if(locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0, 0, gpsListener);
                networkLocation = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }else{
            	Toast.makeText(getBaseContext(),"Please enable Network Location Services!",Toast.LENGTH_SHORT).show();
            }
        } catch (IllegalArgumentException e) {
            Log.e("error", e.toString());
        }
        if(gpslocation==null && networkLocation==null)
            return null;

        if(gpslocation!=null && networkLocation!=null){
            if(gpslocation.getTime() < networkLocation.getTime()){
                gpslocation = null;
                return networkLocation;
            }else{
                networkLocation = null;
                return gpslocation;
            }
        }
        if (gpslocation == null) {
            return networkLocation;
        }
        if (networkLocation == null) {
            return gpslocation;
        }
        return null;
    }
    
    Location getCurrentLocation(){
    	return curLocation;
    }
}