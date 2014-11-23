package com.mycallers.pickup;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class LogsActivity extends ActionBarActivity {
	ListView listView;
	FileOperations fileOperations = new FileOperations();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logs);
		listView = (ListView) findViewById(R.id.listView1);
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
		String[] strLogs=getString();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				  R.layout.logs_layout, strLogs);

		/*ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				  android.R.layout.simple_list_item_1, android.R.id.text1, strLogs);*/
				// Assign adapter to ListView
				listView.setAdapter(adapter);
				adapter=null;
		Log.d("Logging", "Displaying log");
		
	}
	
	public String[] getString(){
		String strDump= fileOperations.read(CallHelper.LogsFileName);
		String[] strArr;
		if(strDump!=null)
			strArr=strDump.split("\n\n");
		else{		
			strArr=new String[1];
			strArr[0]="Nothing yet";
			}
		return strArr;
	}
	
	
	
	
}
