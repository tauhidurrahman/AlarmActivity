package com.example.alarmactivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import com.example.alarmactivity.ServiceLocation.LocalBinder;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class ManualCaptureActivity extends Activity {
	final int CAMERA_CAPTURE_RQST_CODE = 1;
	final int AUDIO_CAPTURE = 2;
	final int RESULT_SUCCESS = 1;
	final int RESULT_FAILURE = 0;
	private boolean imageCaptured;
	private boolean audioCaptured;
	private boolean isServiceLocationRunning,locationServiceStarted;
	String imageFileName,audioFileName;
	long manualCaptureStartTime;
	byte[] byte_img_data;
	long numQuesAnswered;
	ServiceLocation cService;
    boolean mBound;
    EditText question0;
    Spinner question1,question2,question3,question4;
    Button next;
    
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,IBinder service) {
            // We've bound to CounterService, cast the IBinder and get CounterService instance
            LocalBinder binder = (LocalBinder) service;
            cService = binder.getService();
            mBound = true;
            Log.i("MainActivity", "mBound onServiceConnected "+mBound);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.i("MainActivity", "mBound onServiceConnected "+mBound);
        }
    };
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_capture);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.i("MainActivity", "onCreate ManualCaptureActivity");
        numQuesAnswered=0;
        mBound = false;
        imageCaptured=false;
    	audioCaptured=false;
    	next = (Button) findViewById(R.id.next);
    	
    	// Get the intent that started this activity
        Intent intentfromMainActivity = getIntent();
        isServiceLocationRunning=intentfromMainActivity.getExtras().getBoolean("IsRunning");
        Log.i("MainActivity", "isServiceLocationRunning "+isServiceLocationRunning);
        if(isServiceLocationRunning==false){
        	// Start ServiceLocation
            Intent intentServiceLocation = new Intent(this, ServiceLocation.class);
            intentServiceLocation.putExtra("From", 2);//It carries signal that starting from ManualCaptureActivity
            intentServiceLocation.putExtra("Delay", 10);//Delay 10 min, this is actually not used, if intent start from 2 that is ManualCaptureActivity
            startService(intentServiceLocation);
            locationServiceStarted=true;
        }
        // Bind to ServiceLocation
        Intent intentBindServiceLocation = new Intent(this, ServiceLocation.class);
        bindService(intentBindServiceLocation, mConnection, Context.BIND_AUTO_CREATE);
        
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Log.i("MainActivity", "onDestroy ManualCaptureActivity");
        // Unbind from the ServiceLocation
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        
        // Stop ServiceLocation if service is not on from MainActivity
        if(isServiceLocationRunning==false && locationServiceStarted==true){
	        stopService(new Intent(this, ServiceLocation.class));
        }
	}

	public void onCaptureCamera(View view){
    	
    	try {
    		numQuesAnswered+=1;
			Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			//we will handle the returned data in onActivityResult
			startActivityForResult(captureIntent, CAMERA_CAPTURE_RQST_CODE);
		} catch (ActivityNotFoundException anfe) {
			//display an error message
		    String errorMessage = "Your device doesn't support capturing images!";
		    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
		}
    }
    
    public void onCaptureAudio(View view){
    	
		//We need to do something to capture audio
		
    }
    
    @SuppressWarnings("static-access")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode==Activity.RESULT_OK) {
			ImageView imageView = (ImageView) findViewById(R.id.imageView1);
			if (requestCode == CAMERA_CAPTURE_RQST_CODE) {
				if (data != null) {
					Bitmap photo = (Bitmap) data.getExtras().get("data");
					
					//transferring the Bitmap to byte[]
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					photo.compress(Bitmap.CompressFormat.PNG, 100, baos); 
				    byte_img_data = baos.toByteArray();
				    
				    //Showing it in a imageView
					Bitmap photo1 = Bitmap.createScaledBitmap(photo, 100, 100, false);
					imageView.setImageBitmap(photo1);
					
					String filePath = Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
							+ File.separator + System.currentTimeMillis() + ".jpg";
					
					imageFileName = filePath;
					/*
					//Bitmap largeBitmap ;  // save your Bitmap from data[]
					FileOutputStream fileOutputStream = null;
					BufferedOutputStream bos = null;
					int quality = 100;

					File pictureFile = new File(filePath);

					try {
						fileOutputStream = new FileOutputStream(pictureFile);
						bos = new BufferedOutputStream(fileOutputStream);
						photo.compress(CompressFormat.PNG, quality, bos);
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}

					if (bos != null) {
						try {
							bos.close();
						} catch (IOException e) {
							// ignore close error
						}
					}
					*/
					
					imageCaptured=true;
				}
				
			} else if (requestCode == AUDIO_CAPTURE) {

			} else if (requestCode == MANUAL_CAPTURE_2_RQST_CODE){
				Log.i("MainActivity", imageCaptured+"");
				
				
				//if (imageCaptured==true || audioCaptured==true){
		    	//put all the information in the SQLite database
				DatabaseHandler db = new DatabaseHandler(getApplicationContext());
				SensorData sd= new SensorData();
				
				//getting the current date and time
		        sd.setDateAndTime(System.currentTimeMillis()+"");
		        if (imageCaptured==true){
					//setting image file name
					sd.setImageFileName(imageFileName);
					sd.setImageContent(Base64.encodeToString(byte_img_data, Base64.URL_SAFE));
		        }
		        if (audioCaptured==true){
		        	//setting image file name
					sd.setAudioFileName(audioFileName);
		        }
				
		        Boolean locReceived=false;
				//Setting location values
		        if (mBound) {
		            // Call a method from the LocalService.
		            // However, if this call were something that might hang, then this request should
		            // occur in a separate thread to avoid slowing down the activity performance.
		            Location tempLoc= cService.getCurrentLocation();
		            if(tempLoc!=null){
			            if(tempLoc.getTime()>manualCaptureStartTime){
			            	locReceived=true;
			            	sd.setGPSData(tempLoc.getLatitude(), tempLoc.getLongitude(), tempLoc.getAltitude(), 
			                		tempLoc.getProvider(), tempLoc.getSpeed(), tempLoc.getBearing());
			            }
		            }
		        }
		        
		        //Setting Spinner selected item
		        question0 = (EditText)findViewById(R.id.question0);//(Spinner) findViewById(R.id.question1);
		        question1 = (Spinner) findViewById(R.id.question1);
		        question2 = (Spinner) findViewById(R.id.question2);
		        question3 = (Spinner) findViewById(R.id.question3);
		        question4 = (Spinner) findViewById(R.id.question4);
		        
		        long q1id = question1.getSelectedItemId()>0?1:0;
		    	long q2id = question2.getSelectedItemId()>0?1:0;
		    	long q3id = question3.getSelectedItemId()>0?1:0;
				long q4id = question4.getSelectedItemId()>0?1:0;
		        
		        if(question0.length()>0){
		        	numQuesAnswered+=1;
		        }
		        
		        numQuesAnswered+=q1id+q2id+q3id+q4id;
            	numQuesAnswered+=(Long) data.getExtras().get("numQuesAnsweredinPart2");
            	
            	String q1,q2,q3,q4;
            	if(q1id==0){
            		q1 = "";
            	}else{
            		q1 = String.valueOf(question1.getSelectedItem());
            	}
            	if(q2id==0){
            		q2 = "";
            	}else{
            		q2 = String.valueOf(question2.getSelectedItem());
            	}
            	if(q3id==0){
            		q3 = "";
            	}else{
            		q3 = String.valueOf(question3.getSelectedItem());
            	}
            	if(q4id==0){
            		q4 = "";
            	}else{
            		q4 = String.valueOf(question4.getSelectedItem());
            	}
            	String q5 = (String) data.getExtras().get("question5");
		        String q6 = (String) data.getExtras().get("question6");
		        String q7 = (String) data.getExtras().get("question7");
		        String q8 = (String) data.getExtras().get("question8");
		        String q9 = (String) data.getExtras().get("question9");
		        String q10 = (String) data.getExtras().get("PAMemotion");
		        
		        sd.setIsManual("true");
		        
		        sd.setQuestions(question0.getText().toString(), q1, q2, q3, q4, 
		        		q5, q6, q7, q8, q9, q10, "", "");
		        
		        //Toast.makeText(getApplicationContext(), ""+numQuesAnswered, Toast.LENGTH_LONG).show();
				//returning to MainActivity
				if(locReceived==true && numQuesAnswered>=7){
					//adding entry to the database
					db.addContact(sd);
					db.close();
					//sending an intent to ServiceNetwork.
					Intent intentNetworkService = new Intent(getApplicationContext(), ServiceNetwork.class);
					
			        getApplicationContext().startService(intentNetworkService);
			        
			    	setResult(RESULT_SUCCESS);
			    	finish();
		    	}else{
		    		db.close();
		    		setResult(RESULT_FAILURE);
		    		finish();
		    	}
				
			}
		}
	}
    
    private int MANUAL_CAPTURE_2_RQST_CODE=777;
    
    public void onClickNext(View view){
    	
		Intent intent = new Intent(this, ManualCapture2Activity.class);
    	startActivityForResult(intent, MANUAL_CAPTURE_2_RQST_CODE);
    	
    }
    
    
}
