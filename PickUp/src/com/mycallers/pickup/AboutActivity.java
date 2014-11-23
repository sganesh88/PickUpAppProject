package com.mycallers.pickup;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

public class AboutActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		updateVersionName();
	}

	
	public void updateVersionName(){
		PackageInfo pInfo;
		String strVersionName;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			strVersionName = pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			strVersionName="NA";
		}
		TextView version = (TextView) findViewById(R.id.Version);
		version.setText(version.getText() + " - " + strVersionName);
	}
	
}
