package com.example.driver;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.app.Service;

public class MainActivity extends Activity{

	private static final int REQUEST_ENABLE_BT = 0;
	private static MainDriver mainDriver = new MainDriver();
	private static String address = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	/* Start Discovery
	 * Some conditions must be verified : Bluetooth enabled and OS Capability
	 * 
	 */
	public void startDiscovery(View view) {
		Button local = (Button)findViewById(R.id.btnStartDiscovery);
		if (local.getText().equals("Start")) {
			// Initializes Bluetooth adapter.
			final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
			if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
			else {
				Log.d("MAIN_THREAD", "BtAdapter Enabled");
				mainDriver.init(mBluetoothAdapter, mainDriverHandler);
				mainDriver.startDiscovery();
				local.setText("Stop");
			}
		} else {
			mainDriver.stopDiscovery();
			local.setText("Start");
		}
	}
	
	public void connectDevice(View view) {
		Log.d("MAIN_THREAD", "Trying connect to " + address);
		mainDriver.connect(address);
	}

	public void disconnectDevice(View view) {
		Log.d("MAIN_THREAD", "Trying disconnect to " + address);
		mainDriver.disconnect(address);
	}
	
	public void notify(View view) {
		Log.d("MAIN_THREAD", "Trying set notification " + address);
		mainDriver.setNotification(address);
	}

	
	public Handler mainDriverHandler = new Handler(new Handler.Callback() {
		public boolean handleMessage(Message msg) {
			if (msg.what == 115) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Button local = (Button)findViewById(R.id.btnStartDiscovery);
						local.setText("Start");
					}
				});
				return true;
			}
			if (msg.what == 1) {
				final String localmsg = (String) msg.obj;
				Log.d("HANDLER", "FOUND " + localmsg);
				address = localmsg.substring(0, 17);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						TextView local = (TextView)findViewById(R.id.lstInfo);
						local.append(localmsg + "\n");
					}
				});
				return true;
			}
			
			//Log.d("CallBack", msg.getData().toString());
			return false;
		}
	});
	
	
	
}
