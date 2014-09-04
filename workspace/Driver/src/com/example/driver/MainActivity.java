package com.example.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends ListActivity{

	private static final int REQUEST_ENABLE_BT = 0;
	private static MainDriver mainDriver;
	private static String address = null;

	ArrayList<Map<String, String>> list;
	SimpleAdapter adapter;
	
	 // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
 
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mainDriver = ((MainDriver.LocalBinder) service).getService();
            if (!mainDriver.initialize()) {
                Log.d("MAIN_THREAD", "Unable to initialize Bluetooth");
                finish();
            }
        }
 
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	Log.d("MAIN_THREAD", "Service Disconnected");
            mainDriver = null;
        }
    };	
    
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("MAIN_THREAD", "Received Action:" + action);
			//final String mAddress = intent.;
			if (action.equals(mainDriver.WARNING_STOP_DISCOVERY)) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//Button local = (Button)findViewById(R.id.btnStartDiscovery);
						//local.setText("Start");
					}
				});
				return;
			}
			if (action.equals(mainDriver.WARNING_FIND_DEVICE)) {
				Log.d("MAIN_THREAD", "Find Device");
				Map<String, String> newMap = new HashMap<String, String>();
				newMap.put("Address", intent.getStringExtra("address").substring(0,17));
				newMap.put("Name", intent.getStringExtra("address").substring(17));
				newMap.put("Value1", "?");
				newMap.put("Value2", "?");
				list.add(newMap);
				adapter.notifyDataSetChanged();
				
				//final String localmsg = (String) msg.obj;
				//mAddress = localmsg.substring(0, 17);
				//Log.d("MAIN ACT", "Finding " + mAddress);
/*				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//ListView local = (ListView)findViewById(R.id.listView1);
						//local.addView(newText);
						
					}
				});*/
				return;
			}
			if (action.equals(mainDriver.WARNING_NEW_DATA)) {
				//mAddress = (String) msg.obj;
				//Log.d("MAIN_THREAD", "Message arrived " + mAddress);
				/*
				final String value1 = String.valueOf(msg.arg1);
				final String value2 = String.valueOf(msg.arg2);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						for (Map<String, String> element : list) {
							if (element.get("Address").equals(mAddress)) {
								Log.d("MAIN_THREAD", "Item Found");				
								element.put("Value1", value1);
								element.put("Value2", value2);
								adapter.notifyDataSetChanged();
								return;
							}
						}
						
						//ListView local = (ListView)findViewById(R.id.listView1);
						//local.addView(newText);
						
					}
				});*/
				return;
			}
			
			if (action.equals(mainDriver.WARNING_CONNECTED)) {
				//mAddress = (String) msg.obj;
				/*
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						for (Map<String, String> element : list) {
							if (element.get("Address").equals(address)) {
								Log.d("MAIN_THREAD", "Item Found");				
								element.put("Value1", "CONNECTED");
								element.put("Value2", "CONNECTED");
								adapter.notifyDataSetChanged();
								return;
							}
						}
						
						//ListView local = (ListView)findViewById(R.id.listView1);
						//local.addView(newText);
						
					}
				});*/
				return;
			}

			if (action.equals(mainDriver.WARNING_DISCONNECTED)) {
				//mAddress = (String) msg.obj;
				/*runOnUiThread(new Runnable() {
					@Override
					public void run() {
						for (Map<String, String> element : list) {
							if (element.get("Address").equals(mAddress)) {
								Log.d("MAIN_THREAD", "Item Found");				
								element.put("Value1", "DISCONNECTED");
								element.put("Value2", "DISCONNECTED");
								adapter.notifyDataSetChanged();
								return;
							}
						}
					}
				});*/
				return;
			}
        }
    };
        
        
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		    list = buildData();
		    String[] from = { "Address", "Name", "Value1", "Value2"};
		    int[] to = { R.id.txtAddress, R.id.txtName, R.id.txtValue1, R.id.txtValue2};

	        final Intent intent = getIntent();
		    
		    adapter = new SimpleAdapter(this, list, R.layout.row, from, to);
		    setListAdapter(adapter);
			//final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			
		    Intent mainDriverIntent = new Intent(this, MainDriver.class);
	        bindService(mainDriverIntent, mServiceConnection, BIND_AUTO_CREATE);
			
			//mBluetoothAdapter = bluetoothManager.getAdapter();
			//final Handler mHandler = new Handler(new Handler.Callback() {

			//});

			//mHandler.sendEmptyMessage(-1);
			//mainDriver.init(mBluetoothAdapter, mHandler);
	}

	private ArrayList<Map<String, String>> buildData() {
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		return list;
	}

/*	private HashMap<String, String> putData(String address, String[] purpose) {
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("Address", address);
		item.put("Name", purpose[0]);
		item.put("Value1", purpose[1]);
		item.put("Value2", purpose[2]);
		return item;
	}*/
		  
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
		if (id == R.id.action_discovery) {
			// Initializes Bluetooth adapter.
			//if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			    //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			//}
			//else {
			//	Log.d("MAIN_THREAD", "BtAdapter Enabled");
			Log.d("MAIN_THREAD","Starting Discovery");
			mainDriver.startDiscovery();
				
			//}
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		@SuppressWarnings("unchecked")
		Map<String, String> itemAtPosition = (Map<String, String>) l.getItemAtPosition(position);
		Log.d("MAIN_THREAD", "Trying connect to " + itemAtPosition.get("Address"));
		mainDriver.connect(itemAtPosition.get("Address"));
	}
	
	
	public void disconnectDevice(View view) {
		Log.d("MAIN_THREAD", "Trying disconnect to " + address);
		mainDriver.disconnect(address);
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
//      if (mainDriver != null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.d(TAG, "Connect request result=" + result);
 //     }
    }	
	
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainDriver.WARNING_CONNECTED);
        intentFilter.addAction(MainDriver.WARNING_DISCONNECTED);
        intentFilter.addAction(MainDriver.WARNING_FIND_DEVICE);
        intentFilter.addAction(MainDriver.WARNING_NEW_DATA);
        intentFilter.addAction(MainDriver.WARNING_STOP_DISCOVERY);
        return intentFilter;
    }    
    
	@Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
 
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mainDriver = null;
    }

    
    
}
