package com.mycallers.pickup;

import android.support.v7.app.ActionBarActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends ActionBarActivity {
	public static String Config_FILENAME = "appPickup/config";
	private FileOperations fileOperations = new FileOperations();
	int defNoOfCalls=2,defDuration=5;
	int lastSetNoOfCalls,lastSetduration;
	int toastCount=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		setValuesFromFile();
	}
	
	
	
	
	private String[] getConfig(){
		String[] ConfigValues=new String[2];
		try{
		String strFile=fileOperations.read(Config_FILENAME);
		ConfigValues=strFile.split("\n");
		return ConfigValues;
		}
		catch(Exception e){
			if(toastCount==0){
				Toast.makeText(this, "No Config file found. Using default values", Toast.LENGTH_LONG).show();
				toastCount++;
			}
			ConfigValues[0]=String.valueOf(defNoOfCalls);
			ConfigValues[1]=String.valueOf(defDuration);
			return ConfigValues;
		}
	}
	
	private void setValuesFromFile(){
		EditText edtCalls=(EditText)findViewById(R.id.editText1);
		EditText edtDuration=(EditText)findViewById(R.id.EditText01);
		String[] strInputs=getConfig();
		edtCalls.setText(strInputs[0]);
		edtDuration.setText(strInputs[1]);
		lastSetNoOfCalls=Integer.parseInt(strInputs[0]);
		lastSetduration=Integer.parseInt(strInputs[1]);
	}
	
	
	public void saveValues(View vw){
		try{
		EditText edtCalls=(EditText)findViewById(R.id.editText1);		
		int noOfCalls=Integer.parseInt(edtCalls.getText().toString());
		EditText edtDuration=(EditText)findViewById(R.id.EditText01);		
		int duration=Integer.parseInt(edtDuration.getText().toString());		
		if(noOfCalls>10||noOfCalls<1||duration>10||duration<1){
			Toast.makeText(this, "Values should be between 1 and 10 for both the fields. Reverting to the previously set values", Toast.LENGTH_LONG).show();
			setValuesFromFile();			
		}
		else{
			if(lastSetNoOfCalls==noOfCalls && lastSetduration==duration) //nothing to save
				finish();
			else{
				String strToWrite=noOfCalls + "\n" + duration;
				fileOperations.writeConfig(Config_FILENAME, strToWrite);
				MainActivity.SettingsChanged=true;
				Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
				SendMetrics.sendMetrics(this, MainActivity.METRIC_CATEGORY_SETTINGS, MainActivity.METRIC_ACTION_CHANGE_COUNT, String.valueOf(noOfCalls), 1);
				SendMetrics.sendMetrics(this, MainActivity.METRIC_CATEGORY_SETTINGS, MainActivity.METRIC_ACTION_CHANGE_DURATION, String.valueOf(duration), 1);
				finish();
			}
		}
		}
		catch(Exception e){
			Toast.makeText(this, "Exception caught. Reverting to the previously set values", Toast.LENGTH_LONG).show();
			setValuesFromFile();	
		}
	}

	
	
}
