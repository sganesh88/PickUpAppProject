package com.mycallers.pickup;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.analytics.GoogleAnalytics;


public class MainActivity extends ActionBarActivity {
	
	public final static String EXTRA_MESSAGE = "com.mycallers.pickup.MESSAGE";
	public static String FILENAME = "appPickup/contactids";
	public static String Service_Stopped_FILENAME = "appPickup/stop";
	public static String Service_Initialize_FILENAME = "appPickup/initialized";
	public FileOperations fileOperations = new FileOperations();
	TableLayout table;
	private TextView textViewDetectState;
	private Button buttonToggleDetect;
	static int ContactCount=0;
	public static String METRIC_CATEGORY_CONTACT = "Contact";
	public static String METRIC_ACTION_CONTACT_ADD = "Contact Add";
	public static String METRIC_LABEL_BACKFILL = "Back fill";
	public static String METRIC_CATEGORY_SHARE = "Sharing";
	public static String METRIC_ACTION_SHARE_ATTEMPT = "Sharing Attempt";
	public static String METRIC_CATEGORY_RATE = "Rating";
	public static String METRIC_ACTION_RATE_ATTEMPT = "User willing to rate";
	public static String METRIC_CATEGORY_GOAL = "Goal";
	public static String METRIC_ACTION_GOAL_RINGING = "Ringing";
	public static String METRIC_CATEGORY_SETTINGS = "Settings";
	public static String METRIC_ACTION_CHANGE_COUNT = "Change Call Count";
	public static String METRIC_ACTION_CHANGE_DURATION = "Change Duration";
	
	public static boolean SettingsChanged=false;
	 private SimpleAdapter sa;
	 ListView listView;
	  ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
	  SharedPreferences prefs = null;
	  public int totCont;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.listView);
		textViewDetectState = (TextView) findViewById(R.id.serviceStateId);        
        buttonToggleDetect = (Button) findViewById(R.id.serviceStateChg);//serviceStateChg
        showStub();
		prepareContactsTable();
		prefs = getSharedPreferences("com.mycallers.pickup", MODE_PRIVATE);	
		AppRater.app_launched(this);
		((pickup)getApplication()).getTracker(pickup.TrackerName.APP_TRACKER);
		SendMetrics.sendContactMetricsOnStart(this);
	}
		
	
	private void updateServiceState(){
		if(isMyServiceRunning())
			updateText(true);
		else
			updateText(false);
	}
	
	private boolean isMyServiceRunning(){
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.mycallers.pickup.CallDetectService".equals(service.service.getClassName())) 
	        	return true;
	       }
	    return false;
	}
	private void updateText(boolean isItOn){
		if(isItOn){
			
			buttonToggleDetect.setText("Stop");
    		textViewDetectState.setText(getText(R.string.service_state) + "Active");
    		textViewDetectState.setTextColor(Color.rgb(0, 200, 30));
		}
		else{			
			buttonToggleDetect.setText("Start");
			textViewDetectState.setText(getText(R.string.service_state) + "Disabled");
			textViewDetectState.setTextColor(Color.RED);
		}
			
	}
	
	@Override
	protected void onResume() {		
		super.onResume();
		if (prefs.getBoolean("firstrun", true)) {			
			new AlertDialog.Builder(this)
		    .setTitle("Using the PickUp! App")
		    .setMessage("Add Whitelisted Contacts and start the service.\nIn Silent/Vibrate mode, when a whitelisted contact calls you 2 times within 5 minutes,"
		    		+ "PickUp! will let the device ring sensing an emergency. You can change these default values in the Settings menu")
		    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        }
			})
		    
		    .setIcon(android.R.drawable.ic_dialog_alert)
		     .show();
            prefs.edit().putBoolean("firstrun", false).commit();
        }
		
		if(!isMyServiceRunning()){
			if(!fileOperations.fileExists(Service_Stopped_FILENAME))
				changeServiceState();
		}
		updateServiceState();
		if(SettingsChanged){
			restartService();
			SettingsChanged=false;			
		}
	}
	
	
	private void showStub(){
		updateServiceState();
	}
	

	
	
	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
	  		
		 Intent intent = new Intent(Intent.ACTION_PICK);
         intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
         startActivityForResult(intent, 1);  
		
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (data != null) {
	        Uri uri = data.getData();

	        if (uri != null) {
	            Cursor c = null;
	            try {
	                c = getContentResolver().query(uri, new String[]{ 
	                            ContactsContract.CommonDataKinds.Phone.NUMBER,  
	                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
	                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
	                			},
	                        null, null, null);

	                if (c != null && c.moveToFirst()) {
	                    String number = c.getString(0);
	                    String name = c.getString(1);
	                    String strContactId=c.getString(2);
	                    String[][] newCont={{name,number,strContactId}};
	                    if(addItemToContactList(newCont)){
	                    	SendMetrics.sendMetrics(this, METRIC_CATEGORY_CONTACT, METRIC_ACTION_CONTACT_ADD, "New", 1);
	                    	writeFile(c.getString(2));
	                    	Log.d("Add new item", "Attempting to Restart service");
	                    	restartService();
	                    }
	                }
	            } finally {
	                if (c != null) {
	                    c.close();
	                }
	            }
	        }
	    }
	}

	
	
	public ArrayList<String> getPhoneNumber(String id){
		ArrayList<String> phonesInfo = new ArrayList<String>();

		Cursor cursor = getContentResolver().query(
		        CommonDataKinds.Phone.CONTENT_URI, 
		        null, 
		        CommonDataKinds.Phone.CONTACT_ID +" = ?", 
		        new String[]{id}, null);

		while (cursor.moveToNext()) 
		{
			String strName=cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME));
			String strNumber=cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));
			//Toast.makeText(this, " ID =" + id + " Number: " + strNumber, Toast.LENGTH_LONG).show(); 
			phonesInfo.add(strName);
			phonesInfo.add(strNumber);
		} 

		cursor.close();
		return(phonesInfo);
	}
	
	public void writeFile(String strID){
		try{
			fileOperations.write(FILENAME, strID);
			//Toast.makeText(this, strID + " saved to file " + FILENAME, Toast.LENGTH_LONG).show();
		/*FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
		fos.write(strID.getBytes());
		fos.close();*/
		}
		catch(Exception e){
			Toast.makeText(this, "Exception writing the contact IDs to a file", Toast.LENGTH_LONG).show();
		}
	}
	
	public void prepareContactsTable(){
		
		readFileGetContactIDs();
		registerForContextMenu(listView);
	}

	
	private boolean isAlreadyPresent(String strContId,String strName){
		for(HashMap<String,String> hashMap:list){
			if(hashMap.get("line3").equalsIgnoreCase(strContId)){
				Toast.makeText(this, "Contact " + strName + " already present. All numbers under this contact will be automatically considered. You don't have to add them individually.", Toast.LENGTH_LONG).show();
				return true;
			}
		}
		return false;
	}
	
	private boolean addItemToContactList(String[][] strContArray){
		boolean somethingAdded=false;
		HashMap<String,String> item;
	    for(int i=0;i<strContArray.length;i++){
		    if(!isAlreadyPresent(strContArray[i][2],strContArray[i][0])){
		    	  //Log.d("dupe check", strContArray[i][0] + " not present");
			      somethingAdded=true;
			      item = new HashMap<String,String>();
			      item.put( "line1", strContArray[i][0]);
			      item.put( "line2", strContArray[i][1]);
			      item.put( "line3", strContArray[i][2]);
			      list.add( item );
		    }
	    }
	    
	    		
			      sa = new SimpleAdapter(this, list,
			    	      R.layout.my_two_lines,
			    	      new String[] { "line1","line2","line3" },
			    	      new int[] {R.id.line_a, R.id.line_b, R.id.line_c});
			      listView.setAdapter( sa );
			      if(somethingAdded)
			      return true;
	    
	    return false;
	}
	
	
	
	@Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    super.onCreateContextMenu(menu, v, menuInfo);  
        menu.setHeaderTitle("Contact Delete");  
        menu.add(0, v.getId(), 0, "Delete");  
         
    }  
	
	 @Override  
	    public boolean onContextItemSelected(MenuItem item) {
		 AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		 fileOperations.removeId(FILENAME, list.get(info.position).get("line3"));
		 readFileGetContactIDs();
		 restartService();
		 return true;  
	    }  
	
	 
	 
	public ArrayList<String> readFileGetContactIDs(){
		list = new ArrayList<HashMap<String,String>>(); //refreshing the list		
		String strIDsRaw=fileOperations.read(FILENAME);
		if(strIDsRaw!=null){
			String[] strArr= strIDsRaw.split("\n");
			String[][] strContIdArray=new String[strArr.length][3];
			totCont=0;
			for(int i=0;i<strArr.length;i++){
				if(!isEmpty(strArr[i])){
					ArrayList<String> strList=getPhoneNumber(strArr[i]); //get the phone number and display name from the contact ID
					if(strList.size()>0){						
						strContIdArray[totCont][0]=strList.get(0);
						strContIdArray[totCont][1]=strList.get(1);
						strContIdArray[totCont++][2]=strArr[i];
					}
					else
						fileOperations.removeId(FILENAME, strArr[i]); //contact doesn't exist anymore
				}
				//createTable(strList.get(0), strList.get(1)); // add the corresponding entry to the table
			}
			//strContIdArray=Arrays.copyOf(strContIdArray, totCont);
			strContIdArray=truncateArray(strContIdArray, totCont, 3);
			ContactCount = strContIdArray.length;
			addItemToContactList(strContIdArray);
			//Toast.makeText(this, "Contact table refreshed", Toast.LENGTH_SHORT).show();
		}
		return null;
	}
	
	//reinventing the wheel as arrays.copyOf not available in Android 2.2
		private String[][] truncateArray(String[][] strInput,int newLength,int colLength){
			String[][] newArray=new String[newLength][colLength];
			
			for(int i=0;i<newLength;i++){
				for(int j=0;j<colLength;j++){
					newArray[i][j]=strInput[i][j];
				}
			}
			return newArray;
		}
	
	private boolean isEmpty(String strSomething){
		if(strSomething==null)
			return true;
		else{
			if(strSomething.equals(""))
				return true;
			else
				return false;
		}
	}
	
	
	private void restartService(){
		if(isMyServiceRunning()){
			Log.d("restart service", "Restarting..");
			Intent intent = new Intent(this, CallDetectService.class);
			stopService(intent);
			startService(intent);
			//Toast.makeText(this, "Restarting service..", Toast.LENGTH_SHORT).show(); too much of a distraction. 
			//User would feel they are bringing a speeding car to a screeching halt and then re-cranking it every time this toast appears  
		}
	}
	
	
	
	 private void changeServiceState() {
		 
		 boolean enable =isMyServiceRunning();
		 Log.d("Change state", "boolean " + enable);
	    	
	    	
	        Intent intent = new Intent(this, CallDetectService.class);
	    	if (!enable) {
	    		 // start detect service 
	            startService(intent);	            
	            updateText(true);
	    		Toast.makeText(this, "Starting Service..", Toast.LENGTH_SHORT).show();
	    		fileOperations.deleteFile(Service_Stopped_FILENAME);
	    	}
	    	else {
	    		// stop detect service
	    		stopService(intent);
	    		updateText(false);
	    		Toast.makeText(this, "Stopping Service..", Toast.LENGTH_SHORT).show();
	    		fileOperations.createFile(Service_Stopped_FILENAME); //indicator that the service was stopped manually
	    	}
	    }
	 
	 public void stateChange(View view){
		 changeServiceState();
	 }
	 
	 @Override
		public boolean onCreateOptionsMenu(Menu menu) {
		 	
			// Inflate the menu; this adds items to the action bar if it is present.
			//getMenuInflater().inflate(R.menu.main, menu);	
		 	menu.add("About");
		 	menu.add("Settings");	
		 	menu.add("History");
		 	menu.add("Rate PickUp!");
		 	menu.add("Share!");
		 	menu.add("Exit");
			
			return super.onCreateOptionsMenu(menu);
		//	return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// Handle action bar item clicks here. The action bar will
			// automatically handle clicks on the Home/Up button, so long
			// as you specify a parent activity in AndroidManifest.xml.
			String strOptionSelected = item.getTitle().toString();
			Log.d("options", item.getTitle().toString());
			if (strOptionSelected.equalsIgnoreCase("settings")) {
				Log.d("Options Menu", "Opening Settings");
				Intent intent = new Intent(this, SettingsActivity.class);
			    startActivity(intent);
				return true;
			}
			if (strOptionSelected.equalsIgnoreCase("History")) {
				Log.d("Options Menu", "Opening Logs");
				Intent intent = new Intent(this, LogsActivity.class);
			    startActivity(intent);
				return true;
			}
			if (strOptionSelected.equalsIgnoreCase("exit")) {
				Log.d("Options Menu", "Exiting App");			
				finish();
				return true;
			}
			if (strOptionSelected.equalsIgnoreCase("rate pickup!")) {
				SendMetrics.sendMetrics(this,METRIC_CATEGORY_RATE,METRIC_ACTION_RATE_ATTEMPT,"None",1);
				Log.d("Options Menu", "Rating the app. Taking the user to the market!");
				AppRater.rateApp(this);
				return true;
			}
			if (strOptionSelected.equalsIgnoreCase("about")) {
				Log.d("Options Menu", "Opening About UI");
				Intent intent = new Intent(this, AboutActivity.class);
			    startActivity(intent);
				return true;
			}
			if (strOptionSelected.equalsIgnoreCase("share!")) {
				Log.d("Options Menu", "Sharing App. nice!");
				shareApp();
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
		
		private void shareApp(){
			try
			{ 
			  SendMetrics.sendMetrics(this, METRIC_CATEGORY_SHARE, METRIC_ACTION_SHARE_ATTEMPT, "None", 1);	
			  Intent i = new Intent(Intent.ACTION_SEND);  
			  i.setType("text/plain");
			  i.putExtra(Intent.EXTRA_SUBJECT, "This is gonna be insanely useful!");
			  String sAux = "\n\nDon't miss important calls from your loved ones just because you put your phone in Silent Mode. Install this app!\n\n";
			  sAux = sAux + "https://play.google.com/store/apps/details?id=com.mycallers.pickup\n";
			  i.putExtra(Intent.EXTRA_TEXT, sAux);  
			  startActivity(Intent.createChooser(i, "Choose one"));
			}
			catch(Exception e)
			{ //e.toString();
			}   
		}
	
}
