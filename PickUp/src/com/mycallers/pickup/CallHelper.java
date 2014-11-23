package com.mycallers.pickup;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

/**
 * Helper class to detect incoming and outgoing calls.
 * @author Moskvichev Andrey V.
 *
 */
public class CallHelper {

	public static boolean contAdded=false;
	public int lastState;
	public String lastNumber;
	public long waitTime=300000;
	public long timeOut=3600000;
	public int callCountLimit=2;
	public long[][] contArray;
	public int lastContStub=-100;
	public long lastContId=lastContStub; // null contact id
	public boolean ringModeChanged=false;
	public int lastSetRingMode;
	public Vibrator vibrator; 
	public static String LogsFileName="appPickup/Logs";
	public FileOperations fileOperations = new FileOperations();
	public String strContactName=null;
	public int originalVolume;
	AudioManager audioManager; 
	
	public CallHelper(Context ctx) {
		this.ctx = ctx;
		constructContactArray();
		getConfig();
		callStateListener = new CallStateListener();
	//	outgoingReceiver = new OutgoingReceiver();
		audioManager=(AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
	}
	
	private void getConfig(){
		String[] ConfigValues=null;
		try{
		String strFile=fileOperations.read(SettingsActivity.Config_FILENAME);
		ConfigValues=strFile.split("\n");
		waitTime=Integer.parseInt(ConfigValues[1])*60*1000;
		callCountLimit=Integer.parseInt(ConfigValues[0]);
		Log.d("Config File Read from CallHelper", "done");
		}
		catch(Exception e){
			Log.d("Config File Read from CallHelper", "Exception " + e.toString());
			//do nothing			
		}
	}
	
	public ArrayList<String> readFileGetContactIDs(){
		ArrayList<String> strList= new ArrayList<String>();
		String strIDsRaw=fileOperations.read(MainActivity.FILENAME);
		if(strIDsRaw!=null){
			String[] strArr= strIDsRaw.split("\n");
			for(int i=0;i<strArr.length;i++){
				strList.add(strArr[i]);
			}
			
		}
		Log.d("Contacts File Read from CallHelper", "done");
		return strList;
	}
	
	// This array will have the contact ids and two more columns
	//The second column will have the time when the first call from the contact is received.
	//the third column will have the number of calls made by the person within the stipulated time ( waitTime)
	//The second column will be erased and re-written when a call from the contact is received at a later point of time so the 
	//cycle of verifications can start again
	public void constructContactArray(){
		ArrayList<String> strList= readFileGetContactIDs();
		contArray = new long[strList.size()][3];
		int i=0;
		for(String strCont:strList){
			if(!strCont.equals("")){
				contArray[i][0]=Integer.parseInt(strCont);
				contArray[i][1]=0;
				contArray[i++][2]=0;	
			}
		}
		//contArray=Arrays.copyOf(contArray, i);
		contArray=truncateArray(contArray, i, 3);
	}
	
	//reinventing the wheel as arrays.copyOf not available in Android 2.2
	private long[][] truncateArray(long[][] strInput,int newLength,int colLength){
		long[][] newArray=new long[newLength][colLength];
		
		for(int i=0;i<newLength;i++){
			for(int j=0;j<colLength;j++){
				newArray[i][j]=strInput[i][j];
			}
		}
		return newArray;
	}
	
	
	/**
	 * Listener to detect incoming calls. 
	 */
	private class CallStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			revert();
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				// called when someone is ringing to this phone
				executeRingLogic(incomingNumber);
				
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				executeIdleLogic();
			}
			
			
			
			lastState = state;
		}
	}
	
	//change back to Silent or Vibe after the call is either missed or attended 
	public void revert(){
		if(ringModeChanged){	
			Log.d("revert flow", "last volume " + originalVolume + "; last ring mode " + lastSetRingMode);
			audioManager.setStreamVolume(AudioManager.STREAM_RING, originalVolume, 0);
			audioManager.setRingerMode(lastSetRingMode);
			ringModeChanged=false;
			vibrator.cancel();
			Toast.makeText(ctx, "Reverting to the previous Ring mode..", Toast.LENGTH_LONG).show();
		}
			
	}
	
	public String getContactID(String phoneNumber){
		
		  Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		  Cursor cursor = ctx.getContentResolver().query(uri, new String[]{PhoneLookup._ID}, phoneNumber, null, null );
		  if(cursor.moveToFirst()){
			  phoneNumber =      cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
			  cursor.close();
			  return phoneNumber;
			  
		  }
		  cursor.close();
		return null;
	}
	
	public String getContactName(String phoneNumber){
		
		  Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		  Cursor cursor = ctx.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, phoneNumber, null, null );
		  if(cursor.moveToFirst()){
			  phoneNumber =      cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			  cursor.close();
			  return phoneNumber;
			  
		  }
		  cursor.close();
		return null;
	}
	
	/**
	 * Broadcast receiver to detect the outgoing calls.
	 */
	/*public class OutgoingReceiver extends BroadcastReceiver {
	    public OutgoingReceiver() {
	    }

	    @Override
	    public void onReceive(Context context, Intent intent) {
	        String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
	        
	        Toast.makeText(ctx, 
	        		"Outgoing: "+number, 
	        		Toast.LENGTH_LONG).show();
	    }
  
	}*/

	private Context ctx;
	private TelephonyManager tm;
	private CallStateListener callStateListener;
	
	//private OutgoingReceiver outgoingReceiver;

	
	
	
	
	/**
	 * Start calls detection.
	 */
	public void start() {
		tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		/*IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		ctx.registerReceiver(outgoingReceiver, intentFilter);*/
	}
	
	/**
	 * Stop calls detection.
	 */
	public void stop() {
		tm.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
		//ctx.unregisterReceiver(outgoingReceiver);
	}
	
	
	public void executeRingLogic(String incomingNumber){
		if(isSilent()){
			String strContId=getContactID(incomingNumber);
			strContactName=getContactName(incomingNumber);
			if(strContId!=null){
				lastContId=Integer.parseInt(strContId);
				
				if(shouldIRing(lastContId)){
					
					setItRinging(); //oh yeah!!
					}
			}
			else
				lastContId=lastContStub;
			lastNumber=incomingNumber;
		}
	}
	
	public void setItRinging(){
		
		
		originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
		lastSetRingMode=audioManager.getRingerMode();
		audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		vibrator  = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = {0, 200, 700};
		vibrator.vibrate(pattern, 0);
		ringModeChanged=true;
		Toast.makeText(ctx, "Urgent call detected. Increasing the Ring volume..", Toast.LENGTH_LONG).show();
		//woah! The developer should know the good news. "Create a dent in the universe" - Check!
		SendMetrics.sendMetrics(ctx, MainActivity.METRIC_CATEGORY_GOAL, MainActivity.METRIC_ACTION_GOAL_RINGING, "none", 1);
		writeLogsFile();
	}
	
	private void writeLogsFile(){
		fileOperations.writeCallLogs(LogsFileName, "Call from " + strContactName + " set to ring. Time : " + getDateString());
	}
	
	public String getDateString(){
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		String strCurrTime=today.monthDay + "-" + (today.month+1) + "-" + today.year + " , " + today.format("%k:%M:%S");
		return strCurrTime;
	}
	
	public void executeIdleLogic(){
		
		if(isSilent()){
			
			if(lastState==TelephonyManager.CALL_STATE_RINGING){
				
				updateArray(lastContId);
			}
		}
	}
	
	public boolean shouldIRing(long contId){
		if(isSilent()){
			if(contId!=lastContStub){
				
			for(int i=0;i<contArray.length;i++){
				if(contArray[i][0]==contId){
					
					if(System.currentTimeMillis()-contArray[i][1]<waitTime){
						
						if((contArray[i][2]+1)>=callCountLimit)
							return true;					
					}
					break;
				}
			}
			
			}
	}
		return false;
	}
	
	
	public boolean isSilent(){
		
		int getRingerMode=audioManager.getRingerMode();
		if(getRingerMode==AudioManager.RINGER_MODE_VIBRATE ||getRingerMode==AudioManager.RINGER_MODE_SILENT)
			return true;
			else
			return false;
	}
	
	public void updateArray(long contId){
		if(contId!=lastContStub){
		for(int i=0;i<contArray.length;i++){
			if(contArray[i][0]==contId){
				if(System.currentTimeMillis()-contArray[i][1]<waitTime){
					
					contArray[i][2]++; // increasing the call count
				}
				else{					
					contArray[i][1]=System.currentTimeMillis();
					contArray[i][2]=1;
				}
			}
		}
	}
	}
}
