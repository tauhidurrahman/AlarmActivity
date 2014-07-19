package com.example.alarmactivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	AlarmManager alarmManager;
	int sampPeriodMin;
	boolean isRunning;
	private int MANUAL_CAPTURE_RQST_CODE = 99;
	int DefaultSampPeriodMin=10;
	TextView sampleCount;
	Button startnStop;
	SharedPreferences sharedPref;
	DatabaseHandler db;
	final int RESULT_SUCCESS = 1;
	final int RESULT_FAILURE = 0;
	TextView tvLoc1,tvLoc2,tvLoc3,tvLoc4;
	String loc_1,loc_2,loc_3,loc_4;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //opening shared preferences and sqlite handler 
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        db = new DatabaseHandler(this);
        
        //Configuring the startnStop Button
        startnStop = (Button) findViewById(R.id.startnStop);
        isRunning = sharedPref.getBoolean(getString(R.string.Flag_isRunning), false);
        if(isRunning==true){
        	startnStop.setText("Stop");
        	startnStop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_pause, 0, 0, 0);
        }else{
        	startnStop.setText("Start");
        	startnStop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_play_clip, 0, 0, 0);
        }
        
        sampPeriodMin = sharedPref.getInt(getString(R.string.SamplingPeriod), DefaultSampPeriodMin);
        
        //Configuring the sampleCount TextView
        sampleCount = (TextView) findViewById(R.id.sampleCount);
        String TotalRewardEarned = (int)(Float.parseFloat(sharedPref.getString("TotalRewardEarned", "0"))*100)+"";
        sampleCount.setText(TotalRewardEarned);
        
        tvLoc1 = (TextView) findViewById(R.id.collegeTown);
        tvLoc2 = (TextView) findViewById(R.id.enggQuad);
        tvLoc3 = (TextView) findViewById(R.id.artsQuad);
        tvLoc4 = (TextView) findViewById(R.id.aggQuad);
        
        try {
        	JSONObject jsonObject = new JSONObject(sharedPref.getString("rewardScheme", "Not Available"));
        	loc_1=(int)(Float.parseFloat(jsonObject.get("loc_1").toString())*100)+"";
        	loc_2=(int)(Float.parseFloat(jsonObject.get("loc_2").toString())*100)+"";
        	loc_3=(int)(Float.parseFloat(jsonObject.get("loc_3").toString())*100)+"";
        	loc_4=(int)(Float.parseFloat(jsonObject.get("loc_4").toString())*100)+"";
        } catch (JSONException e) {
            e.printStackTrace();
            loc_1="1";
            loc_2="1";
            loc_3="1";
            loc_4="1";
            Log.i("MainActivity","Error with Json "+e.toString());
        }
        
        tvLoc1.setText(loc_1);
        tvLoc2.setText(loc_2);
        tvLoc3.setText(loc_3);
        tvLoc4.setText(loc_4);
        
        
    }
    
    private BroadcastReceiver the_receiver = new BroadcastReceiver(){
    	@Override
    	public void onReceive(Context c, Intent i) {
    		//update the rewardmap and total reward earned
            sampleCount = (TextView) findViewById(R.id.sampleCount);
            String TotalRewardEarned = (int)(Float.parseFloat(sharedPref.getString("TotalRewardEarned", "0"))*100)+"";
            sampleCount.setText(TotalRewardEarned);
            
            try {
            	JSONObject jsonObject = new JSONObject(sharedPref.getString("rewardScheme", "Not Available"));
            	loc_1=(int)(Float.parseFloat(jsonObject.get("loc_1").toString())*100)+"";
            	loc_2=(int)(Float.parseFloat(jsonObject.get("loc_2").toString())*100)+"";
            	loc_3=(int)(Float.parseFloat(jsonObject.get("loc_3").toString())*100)+"";
            	loc_4=(int)(Float.parseFloat(jsonObject.get("loc_4").toString())*100)+"";
            } catch (JSONException e) {
                e.printStackTrace();
                loc_1="1";
                loc_2="1";
                loc_3="1";
                loc_4="1";
                Log.i("MainActivity","Error with Json "+e.toString());
            }
            
            tvLoc1.setText(loc_1);
            tvLoc2.setText(loc_2);
            tvLoc3.setText(loc_3);
            tvLoc4.setText(loc_4);
    	}
	};
	
	public void onResume() {
        super.onResume();
        registerReceiver(the_receiver, new IntentFilter("com.example.alarmactivity.ServiceNetwork"));
    }
 
    public void onPause() {
        super.onPause();
        unregisterReceiver(the_receiver);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.activity_main, menu);
      return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
      switch (item.getItemId()) {
      case R.id.menu_settings:
    	Intent intent = new Intent(this, SettingsActivity.class);
      	startActivity(intent);
        break;
      case R.id.deleteLastSample:
    	sharedPref = getPreferences(Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = sharedPref.edit();
    	editor.putInt("indexOfLastSampletobeDeleted", (int)db.getContactsCount());
    	editor.commit();
        break;
      
      default:
        break;
      }
      
      return true;
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		db.close();
		
		sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.Flag_isRunning), isRunning);
        editor.commit();
        
        //Stopping the ServiceLocation service
		stopService(new Intent(this,ServiceNetwork.class));
        
	}


	public void onClickStartnStop(View view) {

        if(isRunning==false)
        {
            //Here I am starting the ServiceLocation
            Intent intent = new Intent(this, ServiceLocation.class);
            intent.putExtra("From", 1);//It carries signal that starting from MainActivity
            intent.putExtra("Delay", sampPeriodMin);//i
            startService(intent);
            
            startnStop.setText("Stop");
            startnStop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_pause, 0, 0, 0);
            isRunning=true;
        }
        else if(isRunning==true)
        {
        	//Stopping the ServiceLocation service
    		stopService(new Intent(this,ServiceLocation.class));
    		
        	startnStop.setText("Start");
        	startnStop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_play_clip, 0, 0, 0);
            isRunning=false;
        }
	}
	
	public void onManualCapture(View view){
    	Intent intent = new Intent(this, ManualCaptureActivity.class);
    	intent.putExtra("IsRunning", isRunning);//Passing isRunning boolean
    	startActivityForResult(intent, MANUAL_CAPTURE_RQST_CODE);
    }
	
	public void onLeaderBoard(View view){
		Intent intent = new Intent(this, LeaderboardActivity.class);
        startActivity(intent);
	}
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode==RESULT_SUCCESS) {
		}
	}
	
    
}

