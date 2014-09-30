package com.example.driver;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class loadDevices extends Activity {

    private static MainDriver mainDriver;
    //private static MainActivity.DevicesView devicesView;
    private ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
    protected Map<String, String> hashValues = new HashMap<String, String>();
    protected String address = "";
    private MainActivity.DevicesView devicesView = new MainActivity.DevicesView();
    private MainActivity.DetailsView detailsView = new MainActivity.DetailsView();

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
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
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read
    // or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressWarnings("serial")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // final String mAddress = intent.;
            if (action.equals(MainDriver.ACTION_STOP_DISCOVERY)) {
                return;
            } else if (action.equals(MainDriver.ACTION_FIND_DEVICE)) {
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
            } else if (action.equals(MainDriver.ACTION_NEW_DATA)
                    || action.equals(MainDriver.ACTION_UPDATED)) {

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
            } else if (action.equals(MainDriver.ACTION_CONNECTED)) {
                for (Map<String, String> element : devicesView.getList()) {
                    if (element.get("Address").equals(
                            intent.getExtras().getString(
                                    getString(R.string.DEVICE_ADDRESS)))) {
                        element.put("Connection", "ON");
                        devicesView.getAdapter().notifyDataSetChanged();
                        return;
                    }
                }
            } else if (action.equals(MainDriver.ACTION_DISCONNECTED)) {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent mainDriverIntent = new Intent(this, MainDriver.class);
        bindService(mainDriverIntent, mServiceConnection, BIND_AUTO_CREATE);

        setContentView(R.layout.devices);

        getFragmentManager().beginTransaction().add(R.id.container, detailsView).commit();
        getFragmentManager().beginTransaction().remove(detailsView).commit();
        getFragmentManager().beginTransaction().add(R.id.container, devicesView).commit();

        mainDriver.startDiscovery();

        //devicesView.getList();
        //Map<String, String> newMap = new HashMap<String, String>();
        //Map<String, String> newMap = devicesView.getList();

        list = devicesView.getList();

        Log.v("XXXXX",list.toString());

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





    public static class DevicesView extends Fragment {

        protected ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        protected String address = "";
        private SimpleAdapter adapter;


        public DevicesView() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);

            final ListView listview = (ListView) rootView
                    .findViewById(R.id.listDevices);
            Log.d("LIST", "1");
            adapter  = new SimpleAdapter(getActivity(), list, R.layout.row,
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
                            chkDev.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                @Override
                                public void onCheckedChanged(
                                        CompoundButton buttonView,
                                        boolean isChecked) {
                                    if (isChecked) {
                                        mainDriver.connect(address);
                                    } else {
                                        mainDriver.disconnect(address);
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
                mainDriver.startDiscovery();
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
            View rootView = inflater.inflate(R.layout.details_fragment,
                    container, false);
            ListView listView = (ListView) rootView.findViewById(R.id.listInfo);
            adapter = new SimpleAdapter(getActivity(), list,
                    R.layout.row_data, new String[] { "Parameter", "Value" },
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
                mainDriver.readScale();
            }

            if (id == R.id.action_read_pair) {
                mainDriver.applyCommand(address, MainDriver.GET_PAIR_CODE, "");
            }

            if (id == R.id.action_set_pair) {
                mainDriver.applyCommand(address, MainDriver.SET_PAIR_CODE,
                        "26504");
            }

            if (id == R.id.action_get_korex) {
                mainDriver.applyCommand(address,
                        MainDriver.GET_REGISTERED_DATA, "");
            }
            if (id == R.id.action_LED_Blue) {
                mainDriver.applyCommand(address, MainDriver.SET_LED,
                        "1,0,0,255");
            }
            if (id == R.id.action_LED_Red) {
                mainDriver.applyCommand(address, MainDriver.SET_LED,
                        "1,255,0,0");
            }
            if (id == R.id.action_LED_Green) {
                mainDriver.applyCommand(address, MainDriver.SET_LED,
                        "1,0,255,0");
            }
            if (id == R.id.action_LED_Off) {
                mainDriver.applyCommand(address, MainDriver.SET_LED, "0,0,0,0");
            }
            if (id == R.id.action_correct_clock) {
                mainDriver.applyCommand(address, MainDriver.SET_ADJUST_CLOCK,
                        "");
            }
            if (id == R.id.action_alarm) {
                mainDriver.applyCommand(address, MainDriver.SET_ALARM, "");
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






    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.carne:
                if (checked)
                    Log.v("XXXXX","Carne Checked");
                else
                    Log.v("XXXXX","Carne removida");
                break;
            case R.id.queijo:
                if (checked)
                    Log.v("XXXXX","" +
                            "Queijo Checked");
                else
                    Log.v("XXXXX","queijo removido");
                break;
        }
    }


}
