package com.example.alarmactivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class ServiceNetwork extends Service{
	SharedPreferences sharedPref;
    boolean isLoginSuccessful,isUploadSuccessful;
    DatabaseHandler db;
    String charset = "UTF-8";
    String u_id,password;
    int dbpointer;
    int start_Id;
    int indexOfLastSampletobeDeleted;
    List<String> cookies;
    String rewardScheme;//college town
    
    // Contacts Table Columns names
    private static final String KEY_IS_MANUAL = "is_manual";
    private static final String KEY_DATE_TIME = "date_time";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_PROVIDER = "provider";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_BEARING = "bearing";
    private static final String KEY_AUDIOFILENAME="audiofilename";
    private static final String KEY_IMAGEFILENAME="imagefilename";
    private static final String KEY_IMAGEFILECONTENT="imagefile";
    private static final String KEY_QUESTION = "questions";
    private static final String KEY_QUESTION0="question0";
    private static final String KEY_QUESTION1="question1";
    private static final String KEY_QUESTION2="question2";
    private static final String KEY_QUESTION3="question3";
    private static final String KEY_QUESTION4="question4";
    private static final String KEY_QUESTION5="question5";
    private static final String KEY_QUESTION6="question6";
    private static final String KEY_QUESTION7="question7";
    private static final String KEY_QUESTION8="question8";
    private static final String KEY_QUESTION9="question9";
    private static final String KEY_QUESTION10="question10";
    private static final String KEY_QUESTION11="question11";
    private static final String KEY_QUESTION12="PAMemotion";
    
	@Override
	public void onCreate() {
		super.onCreate();
	}
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		start_Id=startId;
		isLoginSuccessful = false;
		isUploadSuccessful = false;
		//Sqlite
		db = new DatabaseHandler(getApplicationContext());
		sharedPref = getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
		dbpointer = sharedPref.getInt("DbPointer", 1);
		u_id = sharedPref.getString("u_id", "");
		password = sharedPref.getString("password", "");
		indexOfLastSampletobeDeleted=sharedPref.getInt("indexOfLastSampletobeDeleted", 0);
		
    	//Checking if the connection is running
        if (isNetworkAvailable()==true) {
        	Log.i("MainActivity", "Network is available");
        	//Trying to login and then upload
        	new LoginServerAndUploadAndGetRewardmap().execute();
        }else{
        	Log.i("MainActivity", "Network is not available");
        	Toast.makeText(getBaseContext(),"No network connection available.", Toast.LENGTH_SHORT).show();
        }
        
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		db.close();
	}
	
	//AsyncTask for creating an additional working thread to handle Login
    private class LoginServerAndUploadAndGetRewardmap extends AsyncTask<Void, Void, Boolean> {
    	
    	protected Boolean doInBackground(Void... voids) {
    		
    		return loginUrlPassword();
        }
    	
    	@Override
		protected void onPostExecute(Boolean success) {
			// async task finished
    		isLoginSuccessful=success;
    		Log.i("MainActivity", "isLoginSuccessful? "+isLoginSuccessful);
    		if(isLoginSuccessful==true){
    			Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_LONG ).show();
    			//Now trying to upload
    			if(dbpointer<db.getContactsCount()){
    				
/*    				//Here we basically try to find if the indexOfLastSampletobeDeleted flag is on...
    				if(dbpointer==indexOfLastSampletobeDeleted){
    					dbpointer=dbpointer+1;
    					SharedPreferences.Editor editor = sharedPref.edit();
    		            editor.putInt("DbPointer", dbpointer);
    		            editor.commit();
    		            //Now trying to get reward
            			new GetReward().execute();
    				}else{
    					new UploadData2Server().execute();
    				}*/
    				
    				new UploadData2Server().execute();
    				
    			}else{
    				//Now trying to get reward
        			new GetReward().execute();
    			}
    			
    		}else{
    			Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_LONG ).show();
    			stopSelf(start_Id);
    		}
			
		}
    	
    }
    
    private void sendBroadcastIntent(){
    	Intent broadCastIntent = new Intent();
        broadCastIntent.setAction( "com.example.alarmactivity.ServiceNetwork" );
        broadCastIntent.setPackage( "com.example.alarmactivity" );
       
        getApplicationContext().sendBroadcast( broadCastIntent );
    }
    
    
    private boolean loginUrlPassword(){
    	URL loginUrl;
    	HttpURLConnection conn = null;
    	boolean success=false;
    	
    	try{
    		//if you are using https, make sure to import java.net.HttpsURLConnection http://www.pac.cs.cornell.edu:5000/api/login
    		//loginUrl=new URL("http", "pac.cs.cornell.edu", 5000, "/api/login");
    		loginUrl=new URL("http://pac.cs.cornell.edu/anthroposophia/api/login");
    		//you need to encode ONLY the values of the parameters
    		String param="u_id=" + URLEncoder.encode(u_id,charset)+
    				"&password="+URLEncoder.encode(password,charset);
    		
    		conn=(HttpURLConnection)loginUrl.openConnection(); 
    		conn.setReadTimeout(10000); //millisec
            conn.setConnectTimeout(10000); //millisec
    		//set the output to true, indicating you are outputting(uploading) POST data
    		conn.setDoOutput(true);
    		//once you set the output to true, you don't really need to set the request method to post, but I'm doing it anyway
    		conn.setRequestMethod("POST");
    		//Android documentation suggested that you set the length of the data you are sending to the server, BUT
    		// do NOT specify this length in the header by using conn.setRequestProperty("Content-Length", length);
    		//use this instead.
    		//conn.setFixedLengthStreamingMode(param.getBytes().length);
    		
    		//Request headers may also include metadata such as credentials, preferred content types, and session cookies.
    		//conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    		conn.setRequestProperty("Accept-Charset", charset);
    		
    		//send the POST out
    		PrintWriter out = new PrintWriter(conn.getOutputStream());
    		
    		out.print(param);
    		out.close();
    		
    		int responseCode = conn.getResponseCode();
    		Log.i("MainActivity", responseCode+"");
    		
    		//build the string to store the response text from the server
    		String response= "";
    		//start listening to the stream
    		Scanner inStream = new Scanner(conn.getInputStream());
    		//process the stream and store it in StringBuilder
    		while(inStream.hasNextLine()){
    			response+=(inStream.nextLine());
    		}
    		
    		if (responseCode==200){
    			success=true;
    			Log.i("MainActivity", response);
    			//Getting the cookies
        		cookies = conn.getHeaderFields().get("Set-Cookie");
    		}
		
    	}
    	//catch some error
    	catch(MalformedURLException ex){
    		Log.i("MainActivity", ex.toString());
    	}
    	// and some more
    	catch(IOException ex){
    		Log.i("MainActivity", ex.toString());
    	}finally{
    		if(conn!=null){
    			conn.disconnect();
    		}
    	}
    	
    	return success;
    }
    
	
    
  //AsyncTask for creating an additional working thread to handle Login
    private class UploadData2Server extends AsyncTask<Void, Void, Boolean> {
    	
    	protected Boolean doInBackground(Void... voids) {
    		
    		return uploadURLData();
        }
    	
    	@Override
		protected void onPostExecute(Boolean success) {
			// async task finished
    		isUploadSuccessful=success;
    		Log.i("MainActivity", "isUploadSuccessful? "+isUploadSuccessful);
    		if(isUploadSuccessful==true){
    			//update the UI via increasing reward point
    			Toast.makeText(getApplicationContext(), "Upload Successful", Toast.LENGTH_LONG ).show();
    			dbpointer=dbpointer+1;
    			//saving the new dbpointer
    			//save reward in shared preferences
	            SharedPreferences.Editor editor = sharedPref.edit();
	            editor.putInt("DbPointer", dbpointer);
	            editor.commit();
    			if(dbpointer<db.getContactsCount()){
    				//Here we are checking if the dbpointer happend to coincide with indexOfLastSampletobeDeleted
    				if(dbpointer==indexOfLastSampletobeDeleted){
    					dbpointer=dbpointer+1;
    		            editor.putInt("DbPointer", dbpointer);
    		            editor.commit();
    		            //Now trying to get reward
            			new GetReward().execute();
    				}else{
    					new UploadData2Server().execute();
    				}
    				
    			}else{
    				//Now trying to get reward 
        			new GetReward().execute();
    			}
    		}else{
    			Toast.makeText(getApplicationContext(), "Upload Failed", Toast.LENGTH_LONG ).show();
    			//Now trying to get reward
    			new GetReward().execute();
    		}
			
		}
    	
    }
    
    private boolean uploadURLData(){
    	//do this wherever you are wanting to POST
    	URL uploadUrl;
    	HttpURLConnection conn = null;
    	FileInputStream fileInputStream = null;
    	InputStream in = null;
    	boolean success=false;
    	
    	try{
    		//if you are using https, make sure to import java.net.HttpsURLConnection http://www.pac.cs.cornell.edu:5000/api/login
    		//uploadUrl=new URL("http", "pac.cs.cornell.edu", 5000, "/api/data");
    		uploadUrl=new URL("http://pac.cs.cornell.edu/anthroposophia/api/data");
    		//Preparing the data to send
			SensorData sd= db.getContact(dbpointer);//specify index
			
	    	JSONObject jsonObject = new JSONObject();
	    	JSONObject jsonObjectQuestion = new JSONObject();
	    	
	    	String audioFileName=sd.getAudioFileName();
	    	String imageFileName=sd.getImageFileName();
	    	
	    	try {
	    		jsonObject.put(KEY_IS_MANUAL, sd.getIsManual());
	    		jsonObject.put(KEY_DATE_TIME, sd.getDateAndTime());
	    		jsonObject.put(KEY_LATITUDE, sd.getLatitude());
	    		jsonObject.put(KEY_LONGITUDE, sd.getLongitude());
	    		jsonObject.put(KEY_ALTITUDE, sd.getAltitude());
	    		jsonObject.put(KEY_ALTITUDE, sd.getAltitude());
	    		jsonObject.put(KEY_PROVIDER, sd.getProvider());
	    		jsonObject.put(KEY_SPEED, sd.getSpeed());
	    		jsonObject.put(KEY_BEARING, sd.getBearing());
	    		jsonObject.put(KEY_AUDIOFILENAME, audioFileName);
	    		jsonObject.put(KEY_IMAGEFILENAME, imageFileName);
	    		
	    		//HashMap< String, String> jsonObjectQuestion = new HashMap<String, String>();
	    		jsonObjectQuestion.put(KEY_QUESTION0, new JSONArray().put(sd.getQuestion0()));
	    		jsonObjectQuestion.put(KEY_QUESTION1, new JSONArray().put(sd.getQuestion1()));
	    		jsonObjectQuestion.put(KEY_QUESTION2, new JSONArray().put(sd.getQuestion2()));
	    		jsonObjectQuestion.put(KEY_QUESTION3, new JSONArray().put(sd.getQuestion3()));
	    		jsonObjectQuestion.put(KEY_QUESTION4, new JSONArray().put(sd.getQuestion4()));
	    		jsonObjectQuestion.put(KEY_QUESTION5, new JSONArray().put(sd.getQuestion5()));
	    		jsonObjectQuestion.put(KEY_QUESTION6, new JSONArray().put(sd.getQuestion6()));
	    		jsonObjectQuestion.put(KEY_QUESTION7, new JSONArray().put(sd.getQuestion7()));
	    		jsonObjectQuestion.put(KEY_QUESTION8, new JSONArray().put(sd.getQuestion8()));
	    		jsonObjectQuestion.put(KEY_QUESTION9, new JSONArray().put(sd.getQuestion9()));
	    		jsonObjectQuestion.put(KEY_QUESTION10, new JSONArray().put(sd.getQuestion10()));
	    		jsonObjectQuestion.put(KEY_QUESTION11, new JSONArray().put(sd.getQuestion11()));
	    		jsonObjectQuestion.put(KEY_QUESTION12, new JSONArray().put(sd.getQuestion12()));
	    		
	    		jsonObject.put(KEY_QUESTION, jsonObjectQuestion);
	    		
	    	} catch (JSONException e) {
	    		e.printStackTrace();
	    		Log.i("MainActivity", e.toString());
	    	}
	    	
	    	Log.i("MainActivity",jsonObject.toString());
	    	
	    	//read audio file
	    	String paramAudio="";
	    	byte[] finalBuffer = new byte[1000000];
	    	if(audioFileName.length()!=0){
	    		Log.i("MainActivity","uploading Audio");
	    		
	    		try {
	    			fileInputStream = new FileInputStream(new File(audioFileName) );
	    			in = new BufferedInputStream(fileInputStream);
	    			//Log.i("MainActivity","Total file size to read (in bytes) : " + fileInputStream.available());
	    			int bytesRead, bytesAvailable, bufferSize;
	    	        int maxBufferSize = 1*1024*1024;
	    			// create a buffer of maximum size
	    	        bytesAvailable = fileInputStream.available();
	    	        bufferSize = Math.min(bytesAvailable, maxBufferSize);
	    	        byte[] buffer = new byte[maxBufferSize];
	    	        
	    	        
	    	        // read file and write it into form...
	    	        bytesRead = in.read(buffer, 0, bufferSize);
	    	        int offset = 0;
	    	        Log.i("MainActivity", buffer.toString());
	    			while (bytesRead > 0)
	    	        {	//
	    				Log.i("MainActivity", buffer.toString());
	    				
	    				System.arraycopy(buffer, 0, finalBuffer, offset, bytesRead);
	    				//paramAudio = paramAudio+Base64.encodeToString(finalBuffer, Base64.URL_SAFE);
	    				bytesAvailable = fileInputStream.available();
	    				bufferSize = Math.min(bytesAvailable, maxBufferSize);
	    				bytesRead = in.read(buffer, 0, bufferSize);
	    				offset=offset+bytesRead;
	    	        }
	    			paramAudio=Base64.encodeToString(finalBuffer, Base64.URL_SAFE);
	    			
	    		} catch (IOException e) {
	    			Log.i("MainActivity",e.toString());
	    			paramAudio="Error";
	    		} finally {
	    			try {
	    				if (fileInputStream != null)
	    					fileInputStream.close();
	    			} catch (IOException ex) {
	    				Log.i("MainActivity", ex.toString());
	    			}
	    		}
	    		
	    	}else{
	    		paramAudio="";
	    	}
	    	
	    	//read audio file
	    	String paramImage="";
	    	if(imageFileName.length()!=0){
	    		paramImage=sd.getImageContent();
	    	}else{
	    		paramImage="";
	    	}
	    	
	    	
	    	//you need to encode ONLY the values of the parameters URLEncoder.encode()
    		String param="meta_data=" + jsonObject.toString()
    				+"&audio_raw_data=" + paramAudio
    				+"&image_raw_data="+paramImage;
    		//Log.i("MainActivity",param);
    		conn=(HttpURLConnection)uploadUrl.openConnection();
    		
    		conn.setReadTimeout(10000); //millisec
            conn.setConnectTimeout(15000); //millisec
            // Allow Inputs
            conn.setDoInput(true);
    		//set the output to true, indicating you are outputting(uploading) POST data
    		conn.setDoOutput(true);
    		// Don't use a cached copy.
            conn.setUseCaches(false);
    		//once you set the output to true, you don't really need to set the request method to post, but I'm doing it anyway
    		conn.setRequestMethod("POST");
    		
    		//If I enable this, the post wont work
    		//conn.setFixedLengthStreamingMode(param.getBytes().length);
    		
    		for (String cookie : cookies) {
    		    conn.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
    		}
    		
    		conn.setRequestProperty("Accept-Charset", charset);
    		
    		//Send request
            DataOutputStream wr = new DataOutputStream (conn.getOutputStream ());
            wr.writeBytes (param);
            wr.flush ();
            wr.close ();
            
    		//Log.i("MainActivity", "Perfect!");
    		
    		int responseCode = conn.getResponseCode();
    		Log.i("MainActivity", responseCode+"");
    		
    		//build the string to store the response text from the server
    		String response= "";
    		//start listening to the stream
    		Scanner inStream = new Scanner(conn.getInputStream());
    		//process the stream and store it in StringBuilder
    		while(inStream.hasNextLine()){
    			response+=(inStream.nextLine());
    		}
    		
    		if (responseCode==201){
    			success=true;
    			try {
		        	JSONObject jsonObject_reward = new JSONObject(response);
		        	float reward=Float.parseFloat(jsonObject_reward.get("reward").toString());
		        	
		        	float totalReward = Float.parseFloat(sharedPref.getString("TotalRewardEarned", "0"));
		        	totalReward=totalReward+reward;
		        	
		        	//save reward in shared preferences
		            SharedPreferences.Editor editor = sharedPref.edit();
		            editor.putString("TotalRewardEarned", ""+totalReward);
		            editor.commit();
		            
		        	success=true;
		        } catch (Exception e) {
		            e.printStackTrace();
		            Log.i("MainActivity","Error with Json "+e.toString());
		        }
    			Log.i("MainActivity", response);
    		}
		
    	}
    	//catch some error
    	catch(MalformedURLException ex){
    		Log.i("MainActivity", ex.toString());
    	}
    	// and some more
    	catch(IOException ex){
    		Log.i("MainActivity", ex.toString());
    	}finally{
    		if(conn!=null){
    			conn.disconnect();
    		}
    	}
    	
    	return success;
    }
    
    //AsyncTask for creating an additional working thread to handle getting reward scheme
    private class GetReward extends AsyncTask<Void, Void, Boolean> {
    	
    	protected Boolean doInBackground(Void... voids) {
    		return getRewardUrl();
        }
    	
    	@Override
		protected void onPostExecute(Boolean success) {
    		//try to update LeaderBoard
    		new GetLeaderBoard().execute();
    		
		}
    	
    }
    
    private boolean getRewardUrl(){
    	HttpURLConnection conn=null;
    	URL rewardUrl;
    	InputStream is = null;
    	boolean success = false;
	    // Only display the first 500 characters of the retrieved content from URL
    	
    	try{
    		Log.i("MainActivity", "AsyncTask getReward");
    		//rewardUrl=new URL("http", "pac.cs.cornell.edu", 5000, "/api/reward/scheme");
    		rewardUrl=new URL("http://pac.cs.cornell.edu/anthroposophia/api/reward/scheme");
    		conn = (HttpURLConnection) rewardUrl.openConnection();
            conn.setReadTimeout(10000); //millisec
            conn.setConnectTimeout(15000); //millisec
            // This is how we shall use cookies this time
    		for (String cookie : cookies) {
    		    conn.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
    		}
    		
    		conn.setRequestProperty("Accept-Charset", charset);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.i("MainActivity", "The response is: " + response);
            
            if(response==200){
	            is = conn.getInputStream();
		        // Convert the InputStream into a string
	            String contentAsString = readURLFeed(is);
		        Log.i("MainActivity", "The reward scheme is " + contentAsString);
		        //save reward in shared preferences
	            SharedPreferences.Editor editor = sharedPref.edit();
	            editor.putString("rewardScheme", contentAsString);
	            editor.commit();
		        
	        	success=true;
            }
	        is.close();
	        
            	
    	}//catch some error
    	catch(MalformedURLException ex){
    		Log.i("MainActivity", ex.toString());
    	}
    	// and some more
    	catch(IOException ex){
    		Log.i("MainActivity", ex.toString());
    	}finally{
    		if(conn!=null){
    			conn.disconnect();
    		}
    	}
    	rewardUrl=null;
    	//return the reward values
    	return success;
    }
	 
  //AsyncTask for creating an additional working thread to handle getting reward scheme
    private class GetLeaderBoard extends AsyncTask<Void, Void, Boolean> {
    	
    	protected Boolean doInBackground(Void... voids) {
    		return getLeaderBoardUrl();
        }
    	
    	@Override
		protected void onPostExecute(Boolean success) {
    		sendBroadcastIntent();
    		stopSelf(start_Id);
		}
    	
    }
    
    private boolean getLeaderBoardUrl(){
    	HttpURLConnection conn=null;
    	URL leaderBoardUrl;
    	InputStream is = null;
    	boolean success = false;
	    // Only display the first 500 characters of the retrieved content from URL
    	
    	try{
    		Log.i("MainActivity", "AsyncTask getLeaderBoard");
    		//leaderBoardUrl=new URL("http", "pac.cs.cornell.edu", 5000, "/api/reward/leaderboard");
    		leaderBoardUrl=new URL("http://pac.cs.cornell.edu/anthroposophia/api/reward/leaderboard");
    		conn = (HttpURLConnection) leaderBoardUrl.openConnection();
            conn.setReadTimeout(10000); //millisec
            conn.setConnectTimeout(15000); //millisec
            // This is how we shall use cookies this time
    		for (String cookie : cookies) {
    		    conn.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
    		}
    		
    		conn.setRequestProperty("Accept-Charset", charset);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.i("MainActivity", "The response is: " + response);
            
            if(response==200){
	            is = conn.getInputStream();
		        // Convert the InputStream into a string
	            String contentAsString = readURLFeed(is);
		        Log.i("MainActivity", "The leaderboard is " + contentAsString);
		        
		        SharedPreferences.Editor editor = sharedPref.edit();
	            editor.putString("LeaderBoard", contentAsString);
	            editor.commit();
		        
            }
	        is.close();
	        
            	
    	}//catch some error
    	catch(MalformedURLException ex){
    		Log.i("MainActivity", ex.toString());
    	}
    	// and some more
    	catch(IOException ex){
    		Log.i("MainActivity", ex.toString());
    	}finally{
    		if(conn!=null){
    			conn.disconnect();
    		}
    	}
    	leaderBoardUrl=null;
    	//return the reward values
    	return success;
    }
    
	 public boolean isNetworkAvailable() {
		    ConnectivityManager cm = (ConnectivityManager) 
		    getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		    // if no network is available networkInfo will be null
		    // otherwise check if we are connected
		    if (networkInfo != null && networkInfo.isConnected()) {
		        return true;
		    }
		    return false;
	 }
	 
	 public String readURLFeed(InputStream stream) {
		    StringBuilder builder = new StringBuilder();
		    try {
		        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		        String line;
		        while ((line = reader.readLine()) != null) {
		          builder.append(line);
		        }
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		    return builder.toString();
		  }
	 	
	 	//AsyncTask for creating an additional working thread to handle Login
	    private class LoginServerAndGetReward extends AsyncTask<Void, Void, Boolean> {
	    	
	    	protected Boolean doInBackground(Void... voids) {
	    		
	    		return loginUrlPassword();
	        }
	    	
	    	@Override
			protected void onPostExecute(Boolean success) {
				// async task finished
	    		isLoginSuccessful=success;
	    		Log.i("MainActivity", "isLoginSuccessful? "+isLoginSuccessful);
	    		if(isLoginSuccessful==true){
	    			Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_LONG ).show();
	    			//Now trying to get reward 
	    			new GetReward().execute();
	    		}else{
	    			Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_LONG ).show();
	    			stopSelf(start_Id);
	    		}
				
			}
	    	
	    }
	
}
