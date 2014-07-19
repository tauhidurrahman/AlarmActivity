package com.example.alarmactivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class LeaderboardActivity extends Activity {
	private String EightSpaces="         ";
	private String TenSpaces="          ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        
        SharedPreferences sharedPref = getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
		String contentAsString = sharedPref.getString("LeaderBoard", "Trying to retrieve information from Server.");
        
        TextView tv = (TextView) findViewById(R.id.leaderList);
        String leaders="";
        
        try {
        	JSONArray jsonArray = new JSONArray(contentAsString);
        	for(int i=0;i<Math.min(jsonArray.length(),10);i++){
        		JSONObject jsonObject = (JSONObject)jsonArray.get(i);
        		leaders=leaders+"\n"+(i+1)+EightSpaces+jsonObject.get("u_id").toString()+TenSpaces+(int)(Float.parseFloat(jsonObject.get("total_reward").toString())*100);
        		Log.i("MainActivity", jsonArray.get(i).toString());
        	}
        	
        } catch (JSONException e) {
            e.printStackTrace();
            leaders=leaders+"\nTrying to retrieve information from Server.";
            Log.i("MainActivity","Error with Json "+e.toString());
        }
        
        tv.setText(tv.getText().toString()+"\n"+leaders);
    }

}
