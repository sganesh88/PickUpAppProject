package com.mycallers.pickup;


import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LogsActivity extends ActionBarActivity {

	FileOperations fileOperations = new FileOperations();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logs);
		loadLogs();
	}
	
	public void clearLogs(View vw){
		fileOperations.deleteFile(CallHelper.LogsFileName);
		loadLogs();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadLogs();
	}
	private void loadLogs(){
		String strLogs=getString();
		TextView vw= (TextView) findViewById(R.id.LogsContainer);
		if(strLogs!=null)
			vw.setText(strLogs);
		else
			vw.setText("Nothing yet..");
	}
	
	public String getString(){
		return fileOperations.read(CallHelper.LogsFileName);
	}
	
	
	
	
}
