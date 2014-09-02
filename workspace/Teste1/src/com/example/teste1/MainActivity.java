package com.example.teste1;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			// Use this check to determine whether BLE is supported on the device. Then
			// you can selectively disable BLE-related features.
			if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
				TextView textView = (TextView) findViewById(R.id.main_msg);
				textView.setText("LIXO");
			//    finish();
			}
			else {
				Intent intent = new Intent(this, mainDriver.class);
			    startActivity(intent);			
		    }
			return true;
		}
		if(id == R.id.exit_settings) {
			finish();
		}
		
		
		return super.onOptionsItemSelected(item);
	}
}
