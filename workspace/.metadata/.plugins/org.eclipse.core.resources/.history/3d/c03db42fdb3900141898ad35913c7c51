package com.example.driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.example.driver.BtDevice.OPERATION;
import com.example.driver.DriverUUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


/**
 * This service treats all bluetooth communication
 * Normal operation is:
 * 		initialize() 		when application starts.
 * 		close()				when application stops.
 * 		
 * 		startDiscovery() 	to see which devices are available. ACTIO_FIND_DEVICE will be fired for each device found.
 * 							the discovery is stopped 10 seconds after its start. Any moment, stopDiscovery() stops the process.
 * 							the key for device operations is its address (MAC Address)
 * 		
 * 		wait for ACTION_FIND_DEVICE, memorizing the address received. This address will be used on connection
 * 				
 * 		in this point could be necessary some device configuration. This procedure have not developed yet.
 * 						
 * 		connect(address)	when information from device are needed call connect passing its address.
 * 							the MainDriver will connect and retry if it fails.
 * 							ACTION_CONNECTED and ACTION_DISCONNECTED will give feedback about device state
 * 
 * 		wait for ACTION_NEW_DATA
 * 							if everything is right (device connected, operating and properly configured) new data will be
 * 							automatically received.
 * 
 * 		disconnect(address)	after sometime devices disconnect automatically but, if disconnect is not called, MainDriver
 * 							will keep trying reconnect. So disconnect must be called when exercise ends.
 * 							
 * The devices address list must be memorize to facilitate user operation but discovery must be executed each time application starts.
 *  
 * Organization
 * 		All methods which fire ACTIONs are labeled broadcast*
 * 		
 * 
 * Speed calculation considers only one device for each parameters (CSC and RSC counts). Changes must be made for two or more devices capturing 
 * 	simultaneous.
 * 
 * 
 * @author asantos
 *
 */
public class MainDriver extends Service implements BluetoothProfile {

	/**
	 * Message types to clients
	 * ACTION_STOP_DISCOVERY - discovery was interrupted by time or by user
	 * ACTION_FIND_DEVICE - a new device was found in discovery process (address)
	 * ACTION_NEW_DATA - new data was received from devices
	 */
	final static String ACTION_STOP_DISCOVERY 	= "STOP_DISCOVERY";
	final static String ACTION_FIND_DEVICE 		= "FIND_DEVICE";
	final static String ACTION_NEW_DATA 		= "NEW_DATA";
	final static String ACTION_CONNECTED 		= "CONNECTED";
	final static String ACTION_DISCONNECTED  	= "DISCONNECTED";
	final static String ACTION_UPDATED		  	= "UPDATED";
	
	
	/**
	 * Register callback and interface (probably on)
	 */
	public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(LOG_MAIN, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
 
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Log.e(LOG_MAIN, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        
        
		return true;
	}
	
	/**
	 * Start Discovery process.
	 * The process is stopped by time (10s hardcoded) or
	 * calling stopDiscovery. Automatic stop is signalled by message code 0.
	 */
	public boolean startDiscovery() {
		if ((mBluetoothAdapter != null) && (mBluetoothAdapter.isEnabled()) && !LeDiscovering) {
			Log.d(LOG_MAIN, "Starting Discovering");
            // Stops scanning after a pre-defined scan period.
            mDiscoveryHandler.postDelayed(DiscoveringTime, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            LeDiscovering = true;
            return true;
        }
		return false;
	}
	
	/**
	 * Stop the discovering process
	 */
	public void stopDiscovery() {
		if (LeDiscovering) {
			mDiscoveryHandler.removeCallbacks(DiscoveringTime);
			broadcastUpdate(ACTION_STOP_DISCOVERY, "Adapter", "");
		}
		if (mBluetoothAdapter != null)
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		LeDiscovering = false;
	}

	/**
	 * Why would be necessary disconnect ?
	 * @param address
	 * @return
	 */
	public boolean disconnect(String address) {
		BtDevice device = lstDevices.get(address);
		if (device != null) {
			device.enable = false;
			if ((device.state != STATE_DISCONNECTED) && (device.state != STATE_DISCONNECTING)) {
				device.clearOperation();
				device.gatt.disconnect();
				device.state = STATE_DISCONNECTING;
				Log.d(LOG_MAIN, "Disconnecting " + address);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Connect to device with address specified
	 * The devices is get from Bluetooth adapter and insert in DeviceList when necessary
	 * 
	 * 
	 */
	public boolean connect(String address) {
		if (mBluetoothAdapter.getRemoteDevice(address) == null) {
			Log.e(LOG_MAIN, "Adapter has no device with address " + address);
			return false;
		}
		if (!lstDevices.containsKey(address))
			lstDevices.put(address, new BtDevice(STATE_DISCONNECTED, address));
		BtDevice device = lstDevices.get(address);
		
		stopDiscovery(); // Its necessary stopDiscovery before any connection
		if (!device.enable) {
			device.enable = true;
			if ((device.state != STATE_CONNECTED) && (device.state != STATE_CONNECTING)) {
				device.gatt = mBluetoothAdapter.getRemoteDevice(address).connectGatt(null, true, mGattCallback);
				device.state = STATE_CONNECTING;
				mHandler.postDelayed(new ConnectingTimeout(device), TIMEOUT_CONNECTION);
				return true;
			}
		}
		return false;
	}

	
	@Override
	public List<BluetoothDevice> getConnectedDevices() {
		List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
		for (String address : lstDevices.keySet()) {
			devices.add(mBluetoothAdapter.getRemoteDevice(address));
		}
		return devices;
	}

	@Override
	public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
		List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
		BtDevice device;
		for (String address : lstDevices.keySet()) {
			device = lstDevices.get(address);
			if (Arrays.asList(states).contains(device.state))
				devices.add(mBluetoothAdapter.getRemoteDevice(address));
		}
		return devices;
	}

	@Override
	public int getConnectionState(BluetoothDevice device) {
		if (lstDevices.containsKey(device.getAddress()))
			return lstDevices.get(device.getAddress()).state;
		return 0;
	}
	
	final static int SET_ACCUMULATIVE_VALUE = 0;
	final static int SET_SENSOR_CALIBRATION = 1;
	final static int SET_SENSOR_POSITION = 2;
	final static int SET_REQUEST_SUPPORTED_POSITION = 3;
		
	/**
	 * Timeout for Read/Write Parameters and Connection. From command to callback.
	 */
	final static int TIMEOUT_CONNECTION   = 10000;
	final static int TIMEOUT_READWRITE   = 1000;

	
	private static final String LOG_MAIN = "DRIVER";
	private static final String LOG_SERVICE = "DRV SERVICE";
	private static final String LOG_NOT = "DRV NOTIFICATION";
	
	
	private static final long SCAN_PERIOD = 10000;
	
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothManager mBluetoothManager = null;
	private boolean LeDiscovering = false;
	
	private Handler mHandler = new Handler();
	private Handler mDiscoveryHandler = new Handler();
	private HashMap<String,BtDevice> lstDevices = new HashMap<String, BtDevice>();
	
	private final static boolean hasProperty(int value, int property) {
		return ((value & property) == property);
	}
	
	/**
	 * After SCAN_PERIOD, the discovering process is automatically stopped
	 */
	private Runnable DiscoveringTime = new Runnable() {
		@Override
		public void run() {
			stopDiscovery();
		}
	};
	
	/**
	 * Repeat connect/disconnect commands if there is no answer in TIMEOUT_CONNECTION ms
	 * @author asantos
	 *
	 */
	private class ConnectingTimeout implements Runnable {
		
		BtDevice device;
		
		ConnectingTimeout(BtDevice btDevice) {
			device = btDevice;
		}
		@Override
		public void run() {
			if ((device.state != STATE_CONNECTED) && (device.enable)) {
				Log.d(LOG_MAIN, "Connecting timeout " + device.address + ". Trying again...");
				connect(device.address);
				mHandler.postDelayed(new ConnectingTimeout(device), TIMEOUT_CONNECTION);
			}
		}
		
	}
	
	
	/**
	 * Each device is stored in a local (should be local?) list (should be hash?).
	 */
	private final BluetoothAdapter.LeScanCallback mLeScanCallback = 
			new BluetoothAdapter.LeScanCallback() {
				@Override
				public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
					if (!lstDevices.isEmpty() && (lstDevices.containsKey(device.getAddress()))) {
						return;
					}
					broadcastUpdate(ACTION_FIND_DEVICE, device.getAddress(), device.getName());
				}
			};

	/**
	 * All device callbacks are handle here
	 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
    	
		/**
		 * When the device is connected for the first time, scan for services
		 * Afterwards is just enable notifications
		 * ???? How to known whether values are updated or its necessary readCharacteristics ????
		 */
		@Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (!lstDevices.containsKey(gatt.getDevice().getAddress())) {
					lstDevices.put(gatt.getDevice().getAddress(), new BtDevice(newState, gatt.getDevice().getAddress()));
				}
				BtDevice device = lstDevices.get(gatt.getDevice().getAddress());
				
				// On connection : list services
				switch(newState) {
				case STATE_CONNECTED:
					mHandler.removeCallbacks(device.ConnectingTime);
					broadcastUpdate(ACTION_CONNECTED, device.address, "");
					if ((device.gatt.getServices() == null) || (device.gatt.getServices().isEmpty())){
						Log.d(LOG_SERVICE, device.address + ": Discovering services");
						device.gatt.discoverServices();
					} else {
						listServices(gatt, device.updating);
					}
					device.state = newState;
					NextOperation(device);
					break;
				case STATE_DISCONNECTED:
					mHandler.removeCallbacks(device.ConnectingTime);
					device.operations.clear();
					if (device.enable) {
						device.state = STATE_CONNECTING;
						device.gatt = mBluetoothAdapter.getRemoteDevice(device.address).connectGatt(null, true, this);
						mHandler.postDelayed(new ConnectingTimeout(device), TIMEOUT_CONNECTION);
					}
					else
						device.state = newState;
					broadcastUpdate(ACTION_DISCONNECTED, device.address, "");
					break;
				case STATE_DISCONNECTING:
					Log.d(LOG_MAIN, "Device " + device.address + " DISCONNECTING");
					device.clearOperation();
					device.state = newState;
					break;
				case STATE_CONNECTING:
					Log.d(LOG_MAIN, "Device " + device.address + " CONNECTING");
					device.operations.clear();
					device.state = newState;
					break;
				}
			}
			else
				// The error 257 should be treated. Some tests stops because this error code
				Log.e(LOG_MAIN, "Connection State Error (" + status + ")");
        }

        /**
         * Happens after call .discoverServices(). And if not????? timeout? Not happen until now...
         * Enable notification
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        	if (status == BluetoothGatt.GATT_SUCCESS) {
				enableNotifications(gatt);
				listServices(gatt, lstDevices.get(gatt.getDevice().getAddress()).updating);
            } else {
            	// Never found any error
                Log.e(LOG_MAIN, "onServicesDiscovered received: " + status);
                gatt.discoverServices();
            }
        }

        /**
         * How manipulate configuration???
         * 
         * @see android.bluetooth.BluetoothGattCallback#onCharacteristicWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)
         */
        public void onCharacteristicWrite(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic, int status) {
        	Log.d(LOG_MAIN, "Characteristic Written (" + status + ")");
        }

        /**
         * Fired after .readCharacteristics
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                int status) {
        	
            if (status != BluetoothGatt.GATT_SUCCESS) {
            	Log.e(LOG_MAIN, "Dev" + gatt.getDevice().getAddress() + "Error Read Characteristic " +  characteristic.getUuid().toString());
            	return;
            }
            if (characteristic.getUuid().equals(UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb"))) {
            	Log.d("SCALE", "Read " + characteristic.getValue()[0] + " " + characteristic.getValue()[1]);
            }
            
	    	if (lstDevices.containsKey(gatt.getDevice().getAddress())) {
	    		storeParameters(lstDevices.get(gatt.getDevice().getAddress()), characteristic);
    			NextOperation(lstDevices.get(gatt.getDevice().getAddress()));
	    	}
        }
        
   
		public void onCharacteristicChanged(BluetoothGatt gatt,
		        BluetoothGattCharacteristic characteristic) {
			readNotification(characteristic);
		}
		
		/**
		 * Fired after .readDescriptor
		 */
	    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
	    	if (lstDevices.containsKey(gatt.getDevice().getAddress())) {
    			NextOperation(lstDevices.get(gatt.getDevice().getAddress()));
	    	}
	    }
	    
	    /**
	     * Fired after .writeDescriptor (needed for notification setup)
	     */
	    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
	    	if (lstDevices.containsKey(gatt.getDevice().getAddress())) {
    			NextOperation(lstDevices.get(gatt.getDevice().getAddress()));
	    	}
	    }
	    
	    /**
	     * Callback invoked when a reliable write transaction has been completed. None until now.
	     *
	     * @param gatt GATT client invoked {@link BluetoothGatt#executeReliableWrite}
	     * @param status {@link BluetoothGatt#GATT_SUCCESS} if the reliable write
	     *               transaction was executed successfully
	     */
	    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
	    }

	    /**
	     * For?
	     */
	    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
	    	Log.d(LOG_MAIN, "Device " + gatt.getDevice().getAddress() + " Sinal=" + rssi);
	    }
	};

	/**
	 * Notifications are treated here. And if notifications stops without any feedback?
     * Verify size to avoid exceptions
	 * @param characteristic
	 */
	private void readNotification(BluetoothGattCharacteristic characteristic) {
		String[] info = DriverUUID.getCharacteristicValue(this, characteristic);
		
		for(int i=0; i<info.length; i = i + 2)
			broadcastUpdate(ACTION_NEW_DATA, info[i], info[i+1]);
	}
	
	/**
	 * Identify any notification service/characteristic and add to operations
	 * @param gatt
	 */
 	private void enableNotifications(BluetoothGatt gatt) {
    	BtDevice btDevice = lstDevices.get(gatt.getDevice().getAddress());
    	if (btDevice == null)
    		return;
		for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
			for(BluetoothGattCharacteristic characteristic: bluetoothGattService.getCharacteristics()) {
				if (hasProperty(characteristic.getProperties(), BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
					Log.d(LOG_NOT, "Set Notification on " + gatt.getDevice().getAddress() + " for " + getString(DriverUUID.uuidToString(characteristic.getUuid())));
					btDevice.addOperation(characteristic, null, OPERATION.SET_NOT);
				}
				if (hasProperty(characteristic.getProperties(), BluetoothGattCharacteristic.PROPERTY_INDICATE)) {
					Log.d(LOG_NOT, "Possible Indication on " + gatt.getDevice().getAddress() + " for " + getString(DriverUUID.uuidToString(characteristic.getUuid())));
				}
			}
		}
		/*
    	// Scan possible notification services
    	// Foot Pod
    	if (gatt.getService(UUID_SERVICE_RUNNING_SPEED) != null) {
    		if (gatt.getService(UUID_SERVICE_RUNNING_SPEED).getCharacteristic(UUID_RUNNING_SPEED) != null) {
    			btDevice.addOperation(gatt.getService(UUID_SERVICE_RUNNING_SPEED).getCharacteristic(UUID_RUNNING_SPEED), null, OPERATION.SET_NOT);
    			Log.d(MAIN_DRIVER, "Set Notification for Foot Pod on " + gatt.getDevice().getAddress());
    		}
    	}
    	// Heart Monitor Device
    	if (gatt.getService(UUID_SERVICE_HEART_MEASUREMENT) != null) {
    		if (gatt.getService(UUID_SERVICE_HEART_MEASUREMENT).getCharacteristic(UUID_HEART_MEASUREMENT) != null) {
    			btDevice.addOperation(gatt.getService(UUID_SERVICE_HEART_MEASUREMENT).getCharacteristic(UUID_HEART_MEASUREMENT), null, OPERATION.SET_NOT);
    			Log.d(MAIN_DRIVER, "Set Notification for Heart Monitor on " + gatt.getDevice().getAddress());
    		}
    	}
    	// Cycling Device
    	if (gatt.getService(UUID_SERVICE_CYCLING_SPEED) != null) {
    		if (gatt.getService(UUID_SERVICE_CYCLING_SPEED).getCharacteristic(UUID_CSC_MEASUREMENT) != null) {
    			btDevice.addOperation(gatt.getService(UUID_SERVICE_CYCLING_SPEED).getCharacteristic(UUID_CSC_MEASUREMENT), null, OPERATION.SET_NOT);
    			Log.d(MAIN_DRIVER, "Set Notification for Cycling Device on " + gatt.getDevice().getAddress());
    		}
    	}

    	// Testing Scale
    	if (gatt.getService(UUID_SERVICE_SCALE_1) != null) {
    		if (gatt.getService(UUID_SERVICE_SCALE_1).getCharacteristic(UUID_SCALE_2) != null) {
    			btDevice.addOperation(gatt.getService(UUID_SERVICE_SCALE_1).getCharacteristic(UUID_SCALE_2), null, OPERATION.SET_NOT);
    			Log.d(MAIN_DRIVER, "Set Notification for Cycling Device on " + gatt.getDevice().getAddress());
    		}
    	}
    	if (gatt.getService(UUID_SERVICE_SCALE_2) != null) {
    		if (gatt.getService(UUID_SERVICE_SCALE_2).getCharacteristic(UUID_SCALE_5) != null) {
    			btDevice.addOperation(gatt.getService(UUID_SERVICE_SCALE_2).getCharacteristic(UUID_SCALE_5), null, OPERATION.SET_NOT);
    			Log.d(MAIN_DRIVER, "Set Notification for Cycling Device on " + gatt.getDevice().getAddress());
    		}
    	}
		*/
	}
		
	/**
	 * Queue read for each parameter when update is true
	 * Or just list parameters for debug
	 * For notification is better store the Services UUID or scan each time 
	 * @param gatt
	 * @param update
	 */
    private void listServices(BluetoothGatt gatt, boolean update) {
    	BtDevice device = lstDevices.get(gatt.getDevice().getAddress());
    	if (device == null)
    		return;
    	
    	Log.d(LOG_SERVICE, "Listing Services " + device.updating);
    	
		for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
			Log.d(LOG_SERVICE, "Service " + getString(DriverUUID.uuidToString(bluetoothGattService.getUuid())));
			for(BluetoothGattCharacteristic bluetoothGattCharacteristic: bluetoothGattService.getCharacteristics()) {
				if(bluetoothGattCharacteristic.getDescriptors().isEmpty()) {
					Log.d(LOG_SERVICE, "Characteristic:" + getString(DriverUUID.uuidToString(bluetoothGattCharacteristic.getUuid())) + "(" + bluetoothGattCharacteristic.getProperties() + ")");
					if ((device.updating) && (hasProperty(bluetoothGattCharacteristic.getProperties(), BluetoothGattCharacteristic.PROPERTY_READ))) {
						device.addOperation(bluetoothGattCharacteristic, null, BtDevice.OPERATION.READ);
					}
				}
				else {
					// Dont work for CSC but test for scale
					if ((device.updating) && (hasProperty(bluetoothGattCharacteristic.getProperties(), BluetoothGattCharacteristic.PROPERTY_READ))) {
						device.addOperation(bluetoothGattCharacteristic, null, BtDevice.OPERATION.READ);
					}
    				for(BluetoothGattDescriptor bluetoothGattDescriptor: bluetoothGattCharacteristic.getDescriptors()) {
					Log.d(LOG_SERVICE, "Characteristic:" + getString(DriverUUID.uuidToString(bluetoothGattCharacteristic.getUuid())) + " Descriptor:" + getString(DriverUUID.uuidToString(bluetoothGattDescriptor.getUuid()))  + "(" + bluetoothGattCharacteristic.getProperties()  + ")");
    					if (device.updating) {
    						device.addOperation(bluetoothGattCharacteristic, bluetoothGattDescriptor, BtDevice.OPERATION.READ);
    					}
    				}
				}
			}
		}
		NextOperation(device);
    }
	
    /**
     * readAll could be called for non connected devices.
     * 
     * @param address
     * @return
     */
    public boolean readAll(final String address) {
    	
    	BtDevice device = lstDevices.get(address);
    	if (device == null) {
    		return false;
    	}
    	
    	device.updating = true;

    	if (device.gatt == null) {
    		stopDiscovery();
    		device.gatt = mBluetoothAdapter.getRemoteDevice(address).connectGatt(null, true, mGattCallback);
			device.state = STATE_CONNECTING;
			mHandler.postDelayed(new ConnectingTimeout(device), TIMEOUT_CONNECTION);
    	} else {
    		listServices(device.gatt, device.updating);
    		NextOperation(device);
    	}
		device.intentRead.putExtra("address", device.gatt.getDevice().getAddress());
    	return true;
    }
    
    

	/**
	 * 
	 * @param action
	 * @param parameter
	 * @param value
	 */
	private void broadcastUpdate(final String action,
		final String parameter, final String value) {
		final Intent intent = new Intent(action);
		if(action.equals(ACTION_FIND_DEVICE)) {
			lstDevices.put(parameter,  new BtDevice(STATE_DISCONNECTED, parameter));
			intent.putExtra("address", parameter + value);
			sendBroadcast(intent);
		}
		if((action.equals(ACTION_DISCONNECTED) || action.equals(ACTION_CONNECTED))) {
			intent.putExtra("address", parameter);
			sendBroadcast(intent);
		}
	}

	/**
	 * 
	 * @param action
	 * @param address
	 * @param parameter
	 * @param value
	 */
	private void broadcastUpdate(final String action, final String address,
		final String parameter, final String value) {
		final Intent intent = new Intent(action);
		intent.putExtra("address", address);
		intent.putExtra(parameter, value);
		sendBroadcast(intent);
	}
	
	/**
	 * 
	 * @param device
	 */
	private void broadcastRead(BtDevice device) {
		device.updating = false;
		sendBroadcast(device.intentRead);
	}
	
	/**
	 * 
	 * @param device
	 * @param characteristic
	 */
	private void storeParameters(BtDevice device, BluetoothGattCharacteristic characteristic) {
		String[] msg = DriverUUID.getCharacteristicValue(this, characteristic);
		for(int i=0; i<msg.length; i=i+2)
			device.intentRead.putExtra(msg[i], msg[i+1]);
	}

	/**
	 * 
	 * @param address
	 * @param command
	 * @param parameter
	 * @return
	 */
	private boolean applyCommand(final String address, final int command, final String parameter) {
		BtDevice device = lstDevices.get(address);
		if (device == null)
			return false;

		switch(command) {
		case SET_ACCUMULATIVE_VALUE :
			break;
		case SET_REQUEST_SUPPORTED_POSITION :
			break;
		case SET_SENSOR_CALIBRATION :
			break;
		case SET_SENSOR_POSITION :
			break;
		}
		return false;
	}
	
	/**
	 * Execute the next registered operation on device
	 * The callback will be used for repost (on error)
	 * @param device
	 */
	private void NextOperation(final BtDevice device) {
		mHandler.removeCallbacks(device.ReadWriteTimeout);
		device.execNextOperation(new ReadWriteTimeoutCallback() {
			@Override
			public void callbackTimeout() {
				mHandler.postDelayed(device.ReadWriteTimeout, TIMEOUT_READWRITE);
			}

			@Override
			public void callbackNextOper() {
				NextOperation(device);
			}

			@Override
			public void callbackFinish() {
				if (device.updating)
					broadcastRead(device);				
			}
		});
		mHandler.postDelayed(device.ReadWriteTimeout, TIMEOUT_READWRITE);
	}
 	 

    public class LocalBinder extends Binder {
        MainDriver getService() {
            return MainDriver.this;
        }
    }
    
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
    	if (!lstDevices.isEmpty()) {
	    	for (BtDevice btDevice : lstDevices.values()) {
	    		if(btDevice.gatt != null)
	    			btDevice.gatt.close();
	    		btDevice.gatt = null;
	    		btDevice = null;
			}
    	}
    }    

    /**
     * Android Services
     * Service shoud not 
     */
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }
    
    private final IBinder mBinder = new LocalBinder();
    
    public void readTest() {
    	for (BtDevice device: lstDevices.values()) {
    		if (device.gatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")) != null) {
    			if (device.gatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb")) != null) {
    				device.addOperation(device.gatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb")),
    							null, OPERATION.READ);
    				return;
    			}
    		}
			
		}
    	Log.d("SCALE", "Scale not found");
    }
 	
}

