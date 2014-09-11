package com.example.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends ListActivity{

	private static MainDriver mainDriver;
	
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
    		//final String mAddress = intent.;
    		if (action.equals(MainDriver.ACTION_STOP_DISCOVERY)) {
    			return;
    		}
    		else if (action.equals(MainDriver.ACTION_FIND_DEVICE)) {
    			Map<String, String> newMap = new HashMap<String, String>();
    			newMap.put("Address", intent.getStringExtra("address").substring(0,17));
    			if (intent.getStringExtra("address").length() >=21)
    				newMap.put("Name", intent.getStringExtra("address").substring(17, 21));
    			else 
    				newMap.put("Name", intent.getStringExtra("address").substring(17));
    			newMap.put("Connection", "OFF");
    			newMap.put("Value1", "?");
    			newMap.put("Value2", "?");
    			newMap.put("Value3", "?");
    			newMap.put("Value4", "?");
    			list.add(newMap);
    			Log.d("LIST", "List Size " + list.size() );
    			adapter.notifyDataSetChanged();
    			return;
    		}
    		else if (action.equals(MainDriver.ACTION_NEW_DATA)) {
    			for (Map<String, String> element : list) {
    				if (element.get("Address").equals(intent.getStringExtra("address"))) {
    					if (intent.hasExtra("Value1"))
    						element.put("Value1", intent.getStringExtra("Value1"));
    					if (intent.hasExtra("Value2"))
    						element.put("Value2", intent.getStringExtra("Value2"));
    					if (intent.hasExtra("Value3"))
    						element.put("Value3", intent.getStringExtra("Value3"));
    					if (intent.hasExtra("Value4"))
    						element.put("Value4", intent.getStringExtra("Value4"));
    					adapter.notifyDataSetChanged();
    					return;
    				}
    			}
    			return;
    		} 
    		else if (action.equals(MainDriver.ACTION_CONNECTED)) {
    			for (Map<String, String> element : list) {
    				if (element.get("Address").equals(intent.getStringExtra("address"))) {
    					element.put("Connection", "ON");
    					adapter.notifyDataSetChanged();
    					return;
    				}
    			}
    		}
    		else if (action.equals(MainDriver.ACTION_DISCONNECTED)) {
    			for (Map<String, String> element : list) {
    				if (element.get("Address").equals(intent.getStringExtra("address"))) {
    					element.put("Connection", "OFF");
    					adapter.notifyDataSetChanged();
    					return;
    				}
    			}
    		}
    		else if (action.equals(MainDriver.ACTION_UPDATED)) {
    			// Call Dialog
    			showDialogInfo(intent.getExtras());
    		}
    	}
    };

    @Override

    /**
     * 
     */
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    list = buildData();
	    String[] from = { "Address", "Name", "Connection", "Value1", "Value2", "Value3", "Value4"};
	    int[] to = { R.id.txtAddress, R.id.txtName, R.id.txtCon , R.id.txtValue1, R.id.txtValue2, R.id.txtValue3, R.id.txtValue4};

	    adapter = new SimpleAdapter(this, list, R.layout.row, from, to)
	    {
	    	@Override
	    	public void notifyDataSetChanged() {
	    		super.notifyDataSetChanged();
	    	}
	    	
            @Override
            public View getView (int position, View convertView, ViewGroup parent)
            {
                View v = super.getView(position, convertView, parent);

                TextView txtView = (TextView) v.findViewById(R.id.txtAddress);
                if (txtView != null) {
                	final String address = txtView.getText().toString();
	                Button btnConfig = (Button) v.findViewById(R.id.btnConfig);
	                Button btnInfo = (Button) v.findViewById(R.id.btnInfo);
	                CheckBox chkDev = (CheckBox) v.findViewById(R.id.chkDev);
	                
	                chkDev.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if (isChecked) {
								mainDriver.connect(address);
							} else {
								mainDriver.disconnect(address);
							}
							
						}
					});
	                
                	btnConfig.setOnClickListener(new OnClickListener() {
					
						@Override
						public void onClick(View v) {
							Log.d("MAIN ACT", "Botao Config Clicked");
							
						}
					});
                	
                	btnInfo.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// Call dialog for details
							mainDriver.readAll(address);
							
						}
					});
                }
        	    

        	    // Trying divide uniformly
        		//GridLayout.LayoutParams params = (LayoutParams) v.getLayoutParams();
        		//params.width = (parent.getWidth()/parent.getColumnCount()) -params.rightMargin - params.leftMargin;
        		//child.setLayoutParams(params);

        	    return v;
	        };
	    };
	    
	    setListAdapter(adapter);

	    Intent mainDriverIntent = new Intent(this, MainDriver.class);
        bindService(mainDriverIntent, mServiceConnection, BIND_AUTO_CREATE);

	}

	private ArrayList<Map<String, String>> buildData() {
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		return list;
	}

	private void showDialogInfo(Bundle list) {
		
		for (String key : list.keySet()) {
			Log.d("DETALHES", "Info: " + key + " = " + list.getString(key));	
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
		if (id == R.id.action_discovery) {
			Log.d("MAIN_THREAD","Starting Discovery");
			mainDriver.startDiscovery();
		}
		
		if (id == R.id.action_clear) {
			list.clear();
			adapter.notifyDataSetChanged();
		}

		if (id == R.id.action_read) {
			mainDriver.readTest();
		}
		
		
		return super.onOptionsItemSelected(item);
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MAIN THREAD", "onResume");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }	
    
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainDriver.ACTION_CONNECTED);
        intentFilter.addAction(MainDriver.ACTION_DISCONNECTED);
        intentFilter.addAction(MainDriver.ACTION_FIND_DEVICE);
        intentFilter.addAction(MainDriver.ACTION_NEW_DATA);
        intentFilter.addAction(MainDriver.ACTION_STOP_DISCOVERY);
        intentFilter.addAction(MainDriver.ACTION_UPDATED);
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
