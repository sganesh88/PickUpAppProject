package com.mycallers.pickup;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class SendMetrics {
	static Tracker tracker;
	static FileOperations fileOperations= new FileOperations();
	public static void sendMetrics(Context context,String strCategory,String strAction,String strLabel,long value){
		if(tracker==null){
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
			tracker=analytics.newTracker(R.xml.app_tracker);
		}
		tracker.send(new HitBuilders.EventBuilder()
        .setCategory(strCategory)
        .setAction(strAction)
        .setLabel(strLabel)
        .setValue(value)
        .build());
		//Toast.makeText(context, strCategory +"/" +  strAction + "/" + strLabel, Toast.LENGTH_LONG).show();
	}
	
	public static void sendContactMetricsOnStart(Context context){
		FileOperations fileOperations= new FileOperations();
		int ContactCount=MainActivity.ContactCount;
		
		if(!fileOperations.fileExists(MainActivity.Service_Initialize_FILENAME)){
			Log.d("Metrics", "Sending the current contact count to GA");
			if(ContactCount==0)
				ContactCount=fileOperations.noOfLines(MainActivity.FILENAME);
	    	 fileOperations.createFile(MainActivity.Service_Initialize_FILENAME);
			//sending the initial contact count to GA. When am i ever gonna code elegantly?
			sendMetrics(context, MainActivity.METRIC_CATEGORY_CONTACT, MainActivity.METRIC_ACTION_CONTACT_ADD, 
					MainActivity.METRIC_LABEL_BACKFILL,ContactCount);
		}
	}
	
}
