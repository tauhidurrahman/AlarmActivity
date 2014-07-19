package com.example.alarmactivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class SettingsActivity extends Activity {
	SharedPreferences sharedPref;
	EditText time;
	int sampPeriodMin;
	int DefaultSampPeriodMin=10;
	EditText u_id,password;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        sharedPref = getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
        
        //Configuring the time EditText
        time = (EditText) findViewById(R.id.time);
        time.setText(""+sharedPref.getInt(getString(R.string.SamplingPeriod), DefaultSampPeriodMin));
        
        //Configure the u_id and password
        u_id = (EditText) findViewById(R.id.u_id);
        u_id.setText(sharedPref.getString("u_id", ""));
		password = (EditText) findViewById(R.id.password);
		password.setText(sharedPref.getString("password", ""));
    }
    
    public void onLogin(View view){
		
    	/*
		//this is just for the time being
		u_id.setText("user_1");
		password.setText("password");
        */
    	//saving the userid and password and Sampling Period
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("u_id", u_id.getText().toString());
        editor.putString("password",password.getText().toString());
        editor.commit();
        
        password.setCursorVisible(false);
        Toast.makeText(this, "The User ID and Password are saved.", Toast.LENGTH_LONG).show();
        
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//saving the userid and password and Sampling Period
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.SamplingPeriod), Integer.parseInt(time.getText().toString()));
        editor.commit();
	}
    
    

}
