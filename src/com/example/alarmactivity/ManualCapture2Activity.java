package com.example.alarmactivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;

public class ManualCapture2Activity extends Activity {
	public static final String PAM_SELECTION = PAMActivity.class.getPackage().getName() + "PAM_SELECTION";
	public static final String PAM_PHOTO_ID = PAMActivity.class.getPackage().getName() + "PAM_PHOTO_ID";
	int selectionPAM,photoidPAM;
	Spinner question5,question6,question7,question8,question9;
	int PAM_RQST_CODE=467;
	long numQuesAnswered;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_capture2);
        numQuesAnswered=0;
        
        question5 = (Spinner) findViewById(R.id.question5);
        question6 = (Spinner) findViewById(R.id.question6);
        question7 = (Spinner) findViewById(R.id.question7);
        question8 = (Spinner) findViewById(R.id.question8);
        question9 = (Spinner) findViewById(R.id.question9);
    }
    
    public void onClickPAM(View view){
    	Intent intent =new Intent(this,PAMActivity.class);
    	startActivityForResult(intent, PAM_RQST_CODE);
    	
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode==Activity.RESULT_OK) {
			if (requestCode == PAM_RQST_CODE) {
				if (data != null) {
					selectionPAM = (Integer) data.getExtras().get(PAM_SELECTION);
					photoidPAM = (Integer) data.getExtras().get(PAM_PHOTO_ID);
					numQuesAnswered=1;
				}
			}
		}
    }
    
    private String getPAMEmotion(int selectionPAM){
    	String emotion="";
    	switch (selectionPAM+1) {
        case 1:
        	emotion="afraid";
        	break;
        case 2:
        	emotion="tense";
        	break;
        case 3:
        	emotion="excited";
        	break;
        case 4:
        	emotion="delighted";
        	break;
        case 5:
        	emotion="frustrated";
        	break;
        case 6:
        	emotion="angry";
        	break;
        case 7:
        	emotion="happy";
        	break;
        case 8:
        	emotion="glad";
        	break;
        case 9:
        	emotion="miserable";
        	break;
        case 10:
        	emotion="sad";
        	break;
        case 11:
        	emotion="calm";
        	break;
        case 12:
        	emotion="satisfied";
        	break;
        case 13:
        	emotion="gloomy";
        	break;
        case 14:
        	emotion="tired";
        	break;
        case 15:
        	emotion="sleepy";
        	break;
        case 16:
        	emotion="serene";
        	break;
        default:
          break;
        }
		return emotion;
    }
    
    public void onClickDone(View view){
    	long q5 = question5.getSelectedItemId()>0?1:0;
    	long q6 = question6.getSelectedItemId()>0?1:0;
    	long q7 = question7.getSelectedItemId()>0?1:0;
		long q8 = question8.getSelectedItemId()>0?1:0;
		long q9 = question9.getSelectedItemId()>0?1:0;
    	
    	Intent result = new Intent(this, ManualCaptureActivity.class);
    	numQuesAnswered+=q5+q6+q7+q8+q9;
    	result.putExtra("numQuesAnsweredinPart2", numQuesAnswered);
    	if(q5==0){
    		result.putExtra("question5", "");
    	}else{
    		result.putExtra("question5", String.valueOf(question5.getSelectedItem()));
    	}
    	if(q6==0){
    		result.putExtra("question6", "");
    	}else{
    		result.putExtra("question6", String.valueOf(question6.getSelectedItem()));
    	}
    	if(q7==0){
    		result.putExtra("question7", "");
    	}else{
    		result.putExtra("question7", String.valueOf(question7.getSelectedItem()));
    	}
    	if(q8==0){
    		result.putExtra("question8", "");
    	}else{
    		result.putExtra("question8", String.valueOf(question8.getSelectedItem()));
    	}
    	if(q9==0){
    		result.putExtra("question9", "");
    	}else{
    		result.putExtra("question9", String.valueOf(question9.getSelectedItem()));
    	}
    	
    	result.putExtra("PAMemotion", getPAMEmotion(selectionPAM));
    	setResult(Activity.RESULT_OK, result);
		finish();
    	
    }

}
