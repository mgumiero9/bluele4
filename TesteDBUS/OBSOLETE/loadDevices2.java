package com.example.driver;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class loadDevices2 extends Activity {

	private static MainDriver2 mainDriver2;
	protected Map<String, String> hashValues = new HashMap<String, String>();
	protected String address = "";
	private DevicesView devicesView = new DevicesView();
	private DetailsView detailsView = new DetailsView();


	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mainDriver2 = ((MainDriver2.LocalBinder) service).getService();
			if (!mainDriver2.initialize()) {
				Log.d("MAIN_THREAD", "Unable to initialize Bluetooth");
				finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.d("MAIN_THREAD", "Service Disconnected");
			mainDriver2 = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@SuppressWarnings("serial")
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			// final String mAddress = intent.;
			if (action.equals(MainDriver2.ACTION_STOP_DISCOVERY)) {
				return;
			} else if (action.equals(MainDriver2.ACTION_FIND_DEVICE)) {
				Map<String, String> newMap = new HashMap<String, String>();
				for (String key : intent.getExtras().keySet())
					Log.d("EXTRAS",
							key + " = " + intent.getExtras().getString(key));
				newMap.put(
						"Address",
						intent.getExtras().getString(
								getString(R.string.DEVICE_ADDRESS)));
				newMap.put(
						"Name",
						intent.getExtras().getString(
								getString(R.string.DEVICE_NAME)));
				newMap.put("Connection", "OFF");
				Log.d("MAP", "Map Size " + newMap.size());
				
				devicesView.getList().add(newMap);
				devicesView.getAdapter().notifyDataSetChanged();
				return;
			} else if (action.equals(MainDriver2.ACTION_NEW_DATA)
					|| action.equals(MainDriver2.ACTION_UPDATED)) {

				String address = "(-)";
				for (String key : intent.getExtras().keySet())
					Log.d("EXTRAS",
							key + " = " + intent.getExtras().getString(key));
				if (intent.getExtras().getString(
						getString(R.string.DEVICE_ADDRESS)) != null) {
					address = "( "
							+ intent.getExtras().getString(
									getString(R.string.DEVICE_ADDRESS)) + " )";
					intent.getExtras().remove(
							getString(R.string.DEVICE_ADDRESS));
				}
				for (String key : intent.getExtras().keySet()) {
					hashValues.put(key, intent.getExtras().getString(key)
							+ address);
				}
				detailsView.getList().clear();
				Map<String, String> newValue = new HashMap<String, String>();
				for (final String key : hashValues.keySet()) {
					Log.d("ADD",
							"Parameter = " + key + " Value="
									+ hashValues.get(key));
					detailsView.getList().add(new HashMap<String, String>() {
						{
							put("Parameter", key);
							put("Value", hashValues.get(key));
						}
					});

				}
				detailsView.getAdapter().notifyDataSetChanged();
				return;
			} else if (action.equals(MainDriver2.ACTION_CONNECTED)) {
				for (Map<String, String> element : devicesView.getList()) {
					if (element.get("Address").equals(
							intent.getExtras().getString(
									getString(R.string.DEVICE_ADDRESS)))) {
						element.put("Connection", "ON");
						devicesView.getAdapter().notifyDataSetChanged();
						return;
					}
				}
			} else if (action.equals(MainDriver2.ACTION_DISCONNECTED)) {
				for (Map<String, String> element : devicesView.getList()) {
					if (element.get("Address").equals(
							intent.getExtras().getString(
									getString(R.string.DEVICE_ADDRESS)))) {
						element.put("Connection", "OFF");
						devicesView.getAdapter().notifyDataSetChanged();
						return;
					}
				}
			}
		}
	};

	@Override
	/**
	 * 
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		Intent mainDriver2Intent = new Intent(this, MainDriver2.class);
		bindService(mainDriver2Intent, mServiceConnection, BIND_AUTO_CREATE);

		setContentView(R.layout.activity_main2);

		// if (savedInstanceState == null) {
		getFragmentManager().beginTransaction().add(R.id.container, detailsView).commit();
		getFragmentManager().beginTransaction().remove(detailsView).commit();
		getFragmentManager().beginTransaction().add(R.id.container, devicesView).commit();
		
		// }

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_devices) {

			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.container, devicesView);
			transaction.commit();
			return true;
		}
		if (id == R.id.action_details) {
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.container, detailsView);
			transaction.commit();
			return true;
		}
		if (id == R.id.action_real_all) {
			mainDriver2.readAll(address);
			return true;
		}
        if (id == R.id.chronometer) {
        Intent myIntent = new Intent(loadDevices2.this, loadChronometer.class);
        //myIntent.putExtra("key", value); //Optional parameters
        loadDevices2.this.startActivity(myIntent);
            return true;
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
		intentFilter.addAction(MainDriver2.ACTION_CONNECTED);
		intentFilter.addAction(MainDriver2.ACTION_DISCONNECTED);
		intentFilter.addAction(MainDriver2.ACTION_FIND_DEVICE);
		intentFilter.addAction(MainDriver2.ACTION_NEW_DATA);
		intentFilter.addAction(MainDriver2.ACTION_STOP_DISCOVERY);
		intentFilter.addAction(MainDriver2.ACTION_UPDATED);
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
		mainDriver2 = null;
	}

	public static class DevicesView extends Fragment {

		protected ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		protected String address = "";
		private SimpleAdapter adapter;
		
		
		public DevicesView() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main2, container,
					false);

			final ListView listview = (ListView) rootView
					.findViewById(R.id.listDevices);
			Log.d("LIST", "1");
			adapter  = new SimpleAdapter(getActivity(), list, R.layout.row2,
					new String[] { "Address", "Name", "Connection" },
					new int[] { R.id.txtAddress, R.id.txtName, R.id.txtCon }) {
				@Override
				public void notifyDataSetChanged() {
					Log.d("ADAPTER", "Notify");
					super.notifyDataSetChanged();
				}

				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View v = super.getView(position, convertView, parent);

					TextView txtView = (TextView) v
							.findViewById(R.id.txtAddress);
					if (txtView != null) {
						address = txtView.getText().toString();
						CheckBox chkDev = (CheckBox) v
								.findViewById(R.id.chkDev);
						if (chkDev != null) {
							chkDev.setOnCheckedChangeListener(new OnCheckedChangeListener() {

								@Override
								public void onCheckedChanged(
										CompoundButton buttonView,
										boolean isChecked) {
									if (isChecked) {
										mainDriver2.connect(address);
									} else {
										mainDriver2.disconnect(address);
									}

								}
							});
						}
					}

					return v;
				};
			};

			listview.setAdapter(adapter);
			setHasOptionsMenu(true);
			return rootView;
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.menu_main, menu);
			super.onCreateOptionsMenu(menu, inflater);

		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();

			if (id == R.id.action_discovery) {
				mainDriver2.startDiscovery();
			}

			if (id == R.id.action_clear) {
				list.clear();
				adapter.notifyDataSetChanged();
			}

			return super.onOptionsItemSelected(item);

		}
		
		public ArrayList<Map<String, String>> getList() {
			return list;
		}
		
		public SimpleAdapter getAdapter() {
			return adapter;
		}
		
	}

	public static class DetailsView extends Fragment {

		private String address = "";
		private ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		protected SimpleAdapter adapter;
		
		public DetailsView() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.details_fragment2,
					container, false);
			ListView listView = (ListView) rootView.findViewById(R.id.listInfo);
			adapter = new SimpleAdapter(getActivity(), list,
					R.layout.row_data2, new String[] { "Parameter", "Value" },
					new int[] { R.id.txtParameter, R.id.txtValue }) {
				@Override
				public void notifyDataSetChanged() {
					Log.d("LIST_ADAPTER", "Changed");
					super.notifyDataSetChanged();
				}

				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View v = super.getView(position, convertView, parent);
					return v;
				};
			};

			listView.setAdapter(adapter);
			setHasOptionsMenu(true);
			return rootView;
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.menu_device, menu);
			super.onCreateOptionsMenu(menu, inflater);

		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();

			if (id == R.id.action_read) {
				mainDriver2.readScale();
			}

			if (id == R.id.action_read_pair) {
				mainDriver2.applyCommand(address, MainDriver2.GET_PAIR_CODE, "");
			}

			if (id == R.id.action_set_pair) {
				mainDriver2.applyCommand(address, MainDriver2.SET_PAIR_CODE,
						"26504");
			}

			if (id == R.id.action_get_korex) {
				mainDriver2.applyCommand(address,
						MainDriver2.GET_REGISTERED_DATA, "");
			}
			if (id == R.id.action_LED_Blue) {
				mainDriver2.applyCommand(address, MainDriver2.SET_LED,
						"1,0,0,255");
			}
			if (id == R.id.action_LED_Red) {
				mainDriver2.applyCommand(address, MainDriver2.SET_LED,
						"1,255,0,0");
			}
			if (id == R.id.action_LED_Green) {
				mainDriver2.applyCommand(address, MainDriver2.SET_LED,
						"1,0,255,0");
			}
			if (id == R.id.action_LED_Off) {
				mainDriver2.applyCommand(address, MainDriver2.SET_LED, "0,0,0,0");
			}
			if (id == R.id.action_correct_clock) {
				mainDriver2.applyCommand(address, MainDriver2.SET_ADJUST_CLOCK,
						"");
			}
			if (id == R.id.action_alarm) {
				mainDriver2.applyCommand(address, MainDriver2.SET_ALARM, "");
			}

			return super.onOptionsItemSelected(item);

		}
		public ArrayList<Map<String, String>> getList() {
			return list;
		}
		
		public SimpleAdapter getAdapter() {
			return adapter;
		}

		
	}

		
}
