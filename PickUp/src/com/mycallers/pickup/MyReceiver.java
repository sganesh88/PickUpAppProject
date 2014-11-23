package com.mycallers.pickup;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {   
FileOperations fileOperations = new FileOperations();
Context ctx;
    @Override
    public void onReceive(Context context, Intent intent) {
    	this.ctx=context;
   	 Log.d("com.mycallers.pickup", "Receiver getting intent " + intent.getAction());
   	if((!fileOperations.fileExists(MainActivity.Service_Stopped_FILENAME)) && (!isMyServiceRunning())){
		     Intent myIntent = new Intent(context, CallDetectService.class);
		     context.startService(myIntent);
		     Log.d("com.mycallers.pickup", "restarting service after reboot");
    	}
    }
    
    private boolean isMyServiceRunning(){
		ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.mycallers.pickup.CallDetectService".equals(service.service.getClassName())) 
	        	return true;
	       }
	    return false;
	}
    
    

	
}