/*These permissions must be set in the
 * application's AndroidManifest.xml file, with something like:
 *
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.RECORD_AUDIO" />
 *
 */
package com.example.alarmactivity;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ServiceMediaRecorder extends Service{
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;
    private long audioLengthSecond = 10;// 10 seconds
    private boolean startRecordings, startPlaying;
    
    private Handler rHandler = new Handler();
    private Handler pHandler = new Handler();
    
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startRecordings = true;
		startPlaying = true;
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/"+System.currentTimeMillis()+".3gp";
        setupRecording();
        Toast.makeText(getBaseContext(), "onStartCommand ServiceAudioCapture", Toast.LENGTH_LONG).show();
        rHandler.removeCallbacks(recordAudio);
        rHandler.postDelayed(recordAudio,1000);//<<-temporarily
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	  public Runnable recordAudio = new Runnable(){
		    public void run(){
				if(startRecordings==true){
					startRecording();
					startRecordings=false;
					rHandler.postDelayed(recordAudio,audioLengthSecond*1000);// register again to start after 1 seconds...
				}else{
					stopRecording();
					pHandler.postDelayed(playAudio,1);
					//stopSelf();
				}
		    }
	  };
	  
	  public Runnable playAudio = new Runnable(){
		    public void run(){
				if(startPlaying==true){
					startPlaying();
					startPlaying=false;
					pHandler.postDelayed(playAudio,audioLengthSecond*1000);// register again to start after 1 seconds...
				}else{
					stopPlaying();
					stopSelf();
				}
		    }
	  };
		  
		  
    
    @Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(getBaseContext(), "onDestroy ServiceAudioCapture", Toast.LENGTH_LONG).show();
		rHandler.removeCallbacks(recordAudio);
		pHandler.removeCallbacks(playAudio);
		rHandler=null;
		pHandler=null;
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
	}
    
    private void setupRecording(){
    	mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setAudioChannels(2);
        mRecorder.setAudioSamplingRate(8000);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//AMR_NB support only 8khz
    }
    
    private void startRecording() {

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        
        mRecorder.start();
        Toast.makeText(getBaseContext(), "Starting Recording", Toast.LENGTH_LONG).show();
        
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        Toast.makeText(getBaseContext(), "Stopping ServiceAudioCapture", Toast.LENGTH_LONG).show();
    }
	
	private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }
    
}
