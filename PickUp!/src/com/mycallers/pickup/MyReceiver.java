package com.mycallers.pickup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {   
FileOperations fileOperations = new FileOperations();
    @Override
    public void onReceive(Context context, Intent intent) {
    	if(!fileOperations.fileExists(MainActivity.Service_Stopped_FILENAME)){
		     Intent myIntent = new Intent(context, CallDetectService.class);
		     context.startService(myIntent);
		     Log.d("com.mycallers.pickup", "restarting service after reboot");
    	}
    }

	
}