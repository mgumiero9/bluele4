package com.example.driver;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.example.driver.BtDevice.OPERATION;

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
import android.text.format.Time;
import android.util.Log;


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
	
	/**
	 * Timeout for Read/Write Parameters and Connection. From command to callback.
	 */
	final static int TIMEOUT_CONNECTION   = 10000;
	final static int TIMEOUT_READWRITE   = 1000;
	
	// Data for CDC Device
	final UUID UUID_SERVICE_GENERIC_ACCESS = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
		final UUID UUID_DEVICE_NAME = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
		final UUID UUID_APPEARENCE = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
		final UUID UUID_PREFERRED_PARAMETERS = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb");
		
	final UUID UUID_SERVICE_GENERIC_ATTRIBUTE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
	
	final UUID UUID_SERVICE_DEVICE_INFORMATION = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
		final UUID UUID_SERIAL_NUMBER = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb"); // utf8s
		final UUID UUID_HARDWARE_REV = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");	// utf8s
		final UUID UUID_FIRMWARE_REV = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb"); // utf8s
		final UUID UUID_SOFTWARE_REV = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");	// utf8s
		final UUID UUID_MANUFACTURER = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb"); // utf8s
		final UUID UUID_PNP_ID = UUID.fromString("00002a50-0000-1000-8000-00805f9b34fb");		// uint8/16
		
	final UUID UUID_SERVICE_RUNNING_SPEED = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
		final UUID UUID_RUNNING_FEATURE = UUID.fromString("00002a54-0000-1000-8000-00805f9b34fb"); // 16bit bin
		final UUID UUID_RUNNING_SPEED = UUID.fromString("00002a53-0000-1000-8000-00805f9b34fb"); // 16bit bin
	
	
		// final UUID UUID_MODEL = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb"); // utf8s
	final UUID UUID_SERVICE_BATTERY = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
		final UUID UUID_BATTERY_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");	// uint8 0-100%
	
	final UUID UUID_SERVICE_CYCLING_SPEED = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb");
		final UUID UUID_CSC_MEASUREMENT = UUID.fromString("00002a5b-0000-1000-8000-00805f9b34fb");	// Characteristic
			final UUID UUID_CLIENT_CHARACTERISTICS = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");	// Descriptor
		final UUID UUID_CSC_FEATURE = UUID.fromString("00002a5c-0000-1000-8000-00805f9b34fb");	// uint16
		// CSC_FEATURE: Bit 0 - Wheel Revolution Data Supported
		// CSC_FEATURE: Bit 1 - Crank Revolution Data Supported 	
		// CSC_FEATURE: Bit 2 - Multiple Sensor Locations Supported
		final UUID UUID_SENSOR_LOCATION = UUID.fromString("00002a5d-0000-1000-8000-00805f9b34fb");	// uint8
		final UUID UUID_CONTROL_POINT = UUID.fromString("00002a55-0000-1000-8000-00805f9b34fb");	// used for calibration
	

	final UUID UUID_BODY_LOCATION = UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb");	// uint8
	final UUID UUID_HEART_CP = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");	// uint8
	final UUID UUID_HEART_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");	// uint8	
		
	final UUID UUID_SYSTEM_ID = UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb");
	final UUID UUID_CERTIFICATION = UUID.fromString("00002a2a-0000-1000-8000-00805f9b34fb");
	final UUID UUID_MODEL = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
		
	private static final String MAIN_DRIVER = "MAIN DRIVER";
	private static final long SCAN_PERIOD = 10000;
	
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothManager mBluetoothManager = null;
	private boolean LeDiscovering = false;
	
	private Handler mTimeoutHandler = new Handler();
	private Handler mHandler = new Handler();
	private Handler mDiscoveryHandler = new Handler();
	private HashMap<String,BtDevice> lstDevices = new HashMap<String, BtDevice>();
	
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
			if (device.state == MainDriver.STATE_CONNECTING) {
				Log.e("BT DEVICE", "Connecting timeout " + device.gatt.getDevice().getAddress() + ". Trying again...");
				device.gatt.connect();
				mHandler.postDelayed(new ConnectingTimeout(device), TIMEOUT_CONNECTION);
			}
			if (device.state == MainDriver.STATE_DISCONNECTING) {
				Log.e("BT DEVICE", "Disconnecting timeout " + device.gatt.getDevice().getAddress() + ". Trying again...");
				device.gatt.disconnect();
				mHandler.postDelayed(new ConnectingTimeout(device), TIMEOUT_CONNECTION);
			}
		}
		
	}
	
	
	/**
     * Call BtDevice for the next operation.
     * THINK MORE
     * Its possible notification stops without any feedback about connection?
	 * 
	 * @return
	 */
	private class OperationTimeout implements Runnable {

		BtDevice device;
		BtDevice.ProcessIO process;
		
		public OperationTimeout(BtDevice btDevice) {
			if(btDevice == null) {
				mHandler.removeCallbacks(this);
				return;
			}
			device = btDevice;
			process = device.execNextOperation();
		}
		
		@Override
		public void run() {
			if (process != null) {
				Log.d()
				device.addOperation(process);
				mHandler.postDelayed(new OperationTimeout(device), TIMEOUT_READWRITE);
			}
		}
	}
	
	
	/*
	 * Register callback and interface (probably on)
	 */
	public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.d(MAIN_DRIVER, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
 
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(MAIN_DRIVER, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
		return true;
	}
	
	/*
	 * Start Discovery process.
	 * The process is stopped by time (10s hardcoded) or
	 * calling stopDiscovery. Automatic stop is signalled by message code 0.
	 */
	public boolean startDiscovery() {
		if ((mBluetoothAdapter != null) && (mBluetoothAdapter.isEnabled())) {
			Log.d(MAIN_DRIVER, "Starting Discovering");
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
		mDiscoveryHandler.removeCallbacks(DiscoveringTime);
		if ((mBluetoothAdapter != null) && (LeDiscovering))
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		broadcastUpdate(ACTION_STOP_DISCOVERY, "Adapter", "");
		LeDiscovering = false;
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
					Log.d(MAIN_DRIVER, "Found Device "+ device.getName() + "-" + device.getAddress() + "-" + device.getBluetoothClass().toString());
					broadcastUpdate(ACTION_FIND_DEVICE, device.getAddress(), device.getName());
				}
			};
			
	/**
	 * Why would be necessary disconnect ?
	 * @param address
	 * @return
	 */
	public boolean disconnect(String address) {
		BtDevice btDevice = lstDevices.get(address);
		if (btDevice != null) {
			if ((btDevice.state != STATE_DISCONNECTED) && (btDevice.state != STATE_DISCONNECTING)) {
				btDevice.clearOperation();
				btDevice.gatt.disconnect();
				btDevice.state = STATE_DISCONNECTING;
				mHandler.postDelayed(new ConnectingTimeout(btDevice), TIMEOUT_CONNECTION);
				Log.d(MAIN_DRIVER, "Disconnecting " + address);
				
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Connect to device with address specified
	 * The devices is get from Bluetooth adapter and insert in DeviceList when necessary
	 * 
	 */
	public boolean connect(String address) {
		stopDiscovery(); // Its necessary stopDiscovery before any connection
		if (mBluetoothAdapter.getRemoteDevice(address) == null) {
			Log.e(MAIN_DRIVER, "Adapter has no device with address " + address);
			return false;
		}
		if (!lstDevices.containsKey(address))
			lstDevices.put(address, new BtDevice(STATE_DISCONNECTED));
		BtDevice btDevice = lstDevices.get(address);
		if ((btDevice.state != STATE_CONNECTED) && (btDevice.state != STATE_CONNECTING)) {
			btDevice.gatt = mBluetoothAdapter.getRemoteDevice(address).connectGatt(null, true, mGattCallback);
			//btDevice.gatt.connect();
			btDevice.state = STATE_CONNECTING;
			mHandler.postDelayed(new ConnectingTimeout(btDevice), TIMEOUT_CONNECTION);
			return true;
		}
		return false;
	}

	/**
	 * All device callbacks are handle here
	 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
    	
		/**
		 * When the device is connected for the first time, scan for services
		 * Afterwards is only enable notifications
		 * ???? How to known whether values are updated or its necessary readCharacteristics ????
		 */
		@Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				BtDevice btDevice = lstDevices.get(gatt.getDevice().getAddress());
				if (btDevice == null) {
					lstDevices.put(gatt.getDevice().getAddress(), new BtDevice(newState));
				} else {
					btDevice.state = newState;
				}
				Log.d(MAIN_DRIVER, "Device " + gatt.getDevice().getAddress() + " State (" + newState + ")");
				// On connection : list services
				switch(newState) {
				case STATE_CONNECTED:
					mHandler.removeCallbacks(btDevice.ConnectingTime);
					broadcastUpdate(ACTION_CONNECTED, gatt.getDevice().getAddress(), "");
					if ((btDevice.gatt.getServices() == null) || (btDevice.gatt.getServices().isEmpty())){
						Log.d(MAIN_DRIVER, gatt.getDevice().getAddress() + ": Discovering services");
						btDevice.gatt.discoverServices();
					} else {
						listServices(gatt, false);
					}
					break;
				case STATE_DISCONNECTED:
					mHandler.removeCallbacks(btDevice.ConnectingTime);
					btDevice.operations.clear();
					broadcastUpdate(ACTION_DISCONNECTED, gatt.getDevice().getAddress(), "");
					break;
				case STATE_DISCONNECTING:
					Log.d(MAIN_DRIVER, "Device " + gatt.getDevice().getAddress() + " DISCONNECTING");
					btDevice.clearOperation();
					break;
				case STATE_CONNECTING:
					Log.d(MAIN_DRIVER, "Device " + gatt.getDevice().getAddress() + " CONNECTING");
					btDevice.operations.clear();
					break;
				}
			}
			else
				// The error 257 should be treated. Some tests stops because this error code
				Log.d(MAIN_DRIVER, "Connection State Error (" + status + ")");
        }

        /**
         * Happens after call .discoverServices(). And if not????? timeout? Not happen until now...
         * Enable notification
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        	if (status == BluetoothGatt.GATT_SUCCESS) {
				enableNotifications(gatt);
				listServices(gatt, false);
            } else {
            	// Never found any error
                Log.w(MAIN_DRIVER, "onServicesDiscovered received: " + status);
            }
        }

        /**
         * How manipulate configuration???
         * 
         * @see android.bluetooth.BluetoothGattCallback#onCharacteristicWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)
         */
        public void onCharacteristicWrite(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic, int status) {
        	Log.d(MAIN_DRIVER, "Characteristic Written (" + status + ")");
        }

        /**
         * Fired after .readCharacteristics
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                int status) {
        	
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	final String msg;
            	
            	if ((characteristic.getUuid().equals(UUID_DEVICE_NAME)) || (characteristic.getUuid().equals(UUID_CSC_MEASUREMENT)) ||
            		(characteristic.getUuid().equals(UUID_FIRMWARE_REV)) || (characteristic.getUuid().equals(UUID_HARDWARE_REV)) ||
            		(characteristic.getUuid().equals(UUID_MANUFACTURER)) || (characteristic.getUuid().equals(UUID_SERIAL_NUMBER)) ||
            		(characteristic.getUuid().equals(UUID_SOFTWARE_REV)) || (characteristic.getUuid().equals(UUID_PREFERRED_PARAMETERS))) {
            		msg = uuidToString(characteristic.getUuid()) +"="+ characteristic.getStringValue(0) + "; ";
            	} else if((characteristic.getUuid().equals(UUID_PNP_ID)) ||
            	(characteristic.getUuid().equals(UUID_APPEARENCE)) || (characteristic.getUuid().equals(UUID_CSC_FEATURE)) ||
            	(characteristic.getUuid().equals(UUID_CLIENT_CHARACTERISTICS))) {
            		msg = uuidToString(characteristic.getUuid()) + "="+ String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0)) + "; ";
            	} else if ((characteristic.getUuid().equals(UUID_PREFERRED_PARAMETERS)) || (characteristic.getUuid().equals(UUID_BATTERY_LEVEL)) ||
            			(characteristic.getUuid().equals(UUID_SENSOR_LOCATION)) || (characteristic.getUuid().equals(UUID_HEART_CP)) || 
            			(characteristic.getUuid().equals(UUID_BODY_LOCATION))) {
            		msg = uuidToString(characteristic.getUuid()) + "=" + String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)) + "; ";
            	} else {
            		msg = uuidToString(characteristic.getUuid()) +"="+ characteristic.getStringValue(0) + " " + 
            	characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0) + " " +
            	characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0) + " " +
            	characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 0) + " (" + characteristic.getProperties() + ")";
            	}
            	Log.d(MAIN_DRIVER, "Dev" + gatt.getDevice().getAddress() + "Read " + msg);
            } else {
            	Log.d(MAIN_DRIVER, "Dev" + gatt.getDevice().getAddress() + "Error Read Characteristic " +  characteristic.getUuid().toString());
            }
            
            mHandler.postDelayed(new OperationTimeout(lstDevices.get(gatt.getDevice().getAddress())), TIMEOUT_READWRITE);
        }
        
        /**
         * Notifications are treated here. And if notifications stops without any feedback?
         */
		public void onCharacteristicChanged(BluetoothGatt gatt,
		        BluetoothGattCharacteristic characteristic) {
			
			if (characteristic.getUuid().equals(UUID_CSC_MEASUREMENT)) {
				Log.d(MAIN_DRIVER, "CSC size=" + characteristic.getValue().length + " Flag=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
				int pos = 1;
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x01) == 0x01) {
					int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, pos);
					pos = pos + 4;
					int ts = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos);
					broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value1", String.valueOf(value));
					broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value2", String.valueOf(ts));
				}
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x02) == 0x02) {
					Log.d(MAIN_DRIVER, "Crank Val=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos));
					broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value3", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos)));
					pos = pos + 2;
					Log.d(MAIN_DRIVER, "Crank TS=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos));
					broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value4", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos)));
				}
			}
			else if (characteristic.getUuid().equals(UUID_RUNNING_SPEED)) {
				Log.d(MAIN_DRIVER, "RUN size=" + characteristic.getValue().length + " flag=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
				int pos = 1;
				int instantaneousSpeed = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos);
				pos = pos + 2;
				broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value1", String.valueOf(instantaneousSpeed));
				int instantaneousCadence = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, pos);
				pos++;
				broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value2", String.valueOf(instantaneousCadence));

				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x01) == 0x01) {
					Log.d(MAIN_DRIVER, "Read Avanço=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos));
					broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value3", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos)));
					pos = pos + 2;
				}
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x02) == 0x02) {
					Log.d(MAIN_DRIVER, "Read Total Distance=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, pos));
					broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value4", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, pos)));
				}
			}
			else if (characteristic.getUuid().equals(UUID_HEART_MEASUREMENT)) {
				Log.d(MAIN_DRIVER, "HEART size=" + characteristic.getValue().length + " flag=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
				int pos = 1;
				int contact = (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) / 2) & 0x3;
				Log.d(MAIN_DRIVER, "Contato Status=" + contact);
				broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value1", String.valueOf(contact));

				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x01) == 0x01) {
					Log.d(MAIN_DRIVER, "Batimentos=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos));
					broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value2", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos)));
					pos++;
					pos++;
				} else {
					Log.d(MAIN_DRIVER, "Batimentos=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, pos));
					broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value2", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, pos)));
					pos++;
				}
				
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x08) == 0x08) {
					Log.d(MAIN_DRIVER, "Energia=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos));
					broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value3", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos)));
					pos = pos + 2;
				}
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x10) == 0x10) {
					Log.d(MAIN_DRIVER, "RR Interval=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos));
					broadcastUpdate(ACTION_NEW_DATA, gatt.getDevice().getAddress(), "Value4", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos)));
					pos = pos + 2;
				}
				
			}
			else
				Log.d(MAIN_DRIVER, "------- Sth Changed in " + characteristic.getUuid().toString() + " ----");

		}
		
		/**
		 * Fired after .readDescriptor
		 */
	    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
	    	int value = descriptor.getValue()[0] + descriptor.getValue()[1] * 256;
	    	if(descriptor.getUuid().equals(UUID_CLIENT_CHARACTERISTICS)) {
	    		Log.d(MAIN_DRIVER, "Dev" + gatt.getDevice().getAddress() + "Read " + uuidToString(descriptor.getCharacteristic().getUuid()) + " Descriptor=" + value + "; ");
	    	} else {
	    		Log.d(MAIN_DRIVER, "Dev" + gatt.getDevice().getAddress() + "Read " + uuidToString(descriptor.getCharacteristic().getUuid()) + " Descriptor=" + descriptor.getUuid().toString() + "=" + value + "; ");
	    	}
            mHandler.postDelayed(new OperationTimeout(lstDevices.get(gatt.getDevice().getAddress())), TIMEOUT_READWRITE);
	    }
	    
	    /**
	     * Firead after .writeDescriptor (needed for notification setup)
	     */
	    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
	    	
	    	if(descriptor.getUuid().equals(UUID_CLIENT_CHARACTERISTICS)) {
	    		int value = descriptor.getValue()[0] + descriptor.getValue()[1] * 256;
	    		Log.d(MAIN_DRIVER, "Dev" + gatt.getDevice().getAddress() + "Cliente Characteristics from " + uuidToString(descriptor.getCharacteristic().getUuid()) + " Read=" + value + " ("+ status + "); ");
	    	}
	    	if (lstDevices.containsKey(gatt.getDevice().getAddress()))
	            mHandler.postDelayed(new OperationTimeout(lstDevices.get(gatt.getDevice().getAddress())), TIMEOUT_READWRITE);
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
			Log.d(MAIN_DRIVER, "------------ Sth Changed RSSI " + rssi);
	    }
	};

	/**
	 * Identify any notification service/characteristic and add to operations
	 * @param gatt
	 */
	private void enableNotifications(BluetoothGatt gatt) {
    	BtDevice btDevice = lstDevices.get(gatt.getDevice().getAddress());
    	if (btDevice == null)
    		return;
    	// Scan possible notification services
    	// Foot Pod
    	if (gatt.getService(UUID_SERVICE_RUNNING_SPEED) != null) {
    		if (gatt.getService(UUID_SERVICE_RUNNING_SPEED).getCharacteristic(UUID_RUNNING_SPEED) != null) {
    			btDevice.addOperation(gatt.getService(UUID_SERVICE_RUNNING_SPEED).getCharacteristic(UUID_RUNNING_SPEED), null, OPERATION.SET_NOT);
    			Log.d(MAIN_DRIVER, "Set Notification for Foot Pod on " + gatt.getDevice().getAddress());
    		}
    	}
    	// Heart Monitor Device
    	if (gatt.getService(UUID_HEART_MEASUREMENT) != null) {
    		if (gatt.getService(UUID_HEART_MEASUREMENT).getCharacteristic(UUID_HEART_MEASUREMENT) != null) {
    			btDevice.addOperation(gatt.getService(UUID_HEART_MEASUREMENT).getCharacteristic(UUID_HEART_MEASUREMENT), null, OPERATION.SET_NOT);
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
	}
		
	/**
	 * Queue read for each parameter when update is true
	 * Or just list parameters for debug
	 * For notification is better store the Services UUID or scan each time 
	 * @param gatt
	 * @param update
	 */
    private void listServices(BluetoothGatt gatt, boolean update) {
    	BtDevice btDevice = lstDevices.get(gatt.getDevice().getAddress());
    	if (btDevice == null)
    		return;
    	
		for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
			Log.d(MAIN_DRIVER, "Found Service " + uuidToString(bluetoothGattService.getUuid()));
			for(BluetoothGattCharacteristic bluetoothGattCharacteristic: bluetoothGattService.getCharacteristics()) {
				if(bluetoothGattCharacteristic.getDescriptors().isEmpty()) {
					Log.d(MAIN_DRIVER, "Characteristic:" + uuidToString(bluetoothGattCharacteristic.getUuid()) + "(" + bluetoothGattCharacteristic.getPermissions() + ")");
					//if ((update) && ((bluetoothGattCharacteristic.getPermissions() & BluetoothGattCharacteristic.PERMISSION_READ) == BluetoothGattCharacteristic.PERMISSION_READ)) {
					
					if (update) {
						btDevice.addOperation(bluetoothGattCharacteristic, null, BtDevice.OPERATION.READ);
					}
				} else {
					// Dont work for CSC but test for scale
					btDevice.addOperation(bluetoothGattCharacteristic, null, BtDevice.OPERATION.READ);
    				for(BluetoothGattDescriptor bluetoothGattDescriptor: bluetoothGattCharacteristic.getDescriptors()) {
    					Log.d(MAIN_DRIVER, "Characteristic:" + uuidToString(bluetoothGattCharacteristic.getUuid()) + " Descriptor:" + uuidToString(bluetoothGattDescriptor.getUuid())  + "(" + bluetoothGattDescriptor.getPermissions() + ")");
    					//if ((update) && ((bluetoothGattDescriptor.getPermissions() & BluetoothGattDescriptor.PERMISSION_READ) == BluetoothGattDescriptor.PERMISSION_READ)) {
    					if (update) {
    						btDevice.addOperation(bluetoothGattCharacteristic, bluetoothGattDescriptor, BtDevice.OPERATION.READ);
    					}
    				}
				}
			}
		}
		
        mHandler.postDelayed(new OperationTimeout(lstDevices.get(gatt.getDevice().getAddress())), TIMEOUT_READWRITE);
    }
	
	@Override
	public List<BluetoothDevice> getConnectedDevices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getConnectionState(BluetoothDevice device) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void broadcastUpdate(final String action,
		final String parameter, final String value) {
		final Intent intent = new Intent(action);
		if(action.equals(ACTION_FIND_DEVICE)) {
			lstDevices.put(parameter,  new BtDevice(STATE_DISCONNECTED));
			Log.d(MAIN_DRIVER, "Sending Message: New Device Found");
			intent.putExtra("address", parameter + value);
			sendBroadcast(intent);
		}
		if((action.equals(ACTION_DISCONNECTED) || action.equals(ACTION_CONNECTED))) {
			intent.putExtra("address", parameter);
			sendBroadcast(intent);
		}
	}

	private void broadcastUpdate(final String action, final String address,
		final String parameter, final String value) {
		final Intent intent = new Intent(action);
		intent.putExtra("address", address);
		intent.putExtra(parameter, value);
		sendBroadcast(intent);
	}
	
	
	 
	private String uuidToString(UUID uuid) {
    	if (uuid.equals(UUID_DEVICE_NAME)) {
   		 	return "Device Name";
	   	} else if (uuid.equals(UUID_BATTERY_LEVEL)) {
	   		return "Battery Level";
	   	} else if (uuid.equals(UUID_CSC_MEASUREMENT)) {
	   		return "Revolucoes";
	   	} else if(uuid.equals(UUID_PREFERRED_PARAMETERS)) {
	   		return "Preferred Parameters";
	   	} else if(uuid.equals(UUID_PNP_ID)) {
	   		return "PNP ID";
	   	} else if(uuid.equals(UUID_FIRMWARE_REV)) {
	   		return "Firmware";
	   	} else if(uuid.equals(UUID_HARDWARE_REV)) {
	   		return "Hardware";
	   	} else if(uuid.equals(UUID_MANUFACTURER)) {
	   		return "Manufacturer";
	   	} else if(uuid.equals(UUID_SERIAL_NUMBER)) {
	   		return "Serial Number";
	   	} else if(uuid.equals(UUID_SOFTWARE_REV)) {
	   		return "Software Ver";
	   	} else if (uuid.equals(UUID_CSC_FEATURE)) {
	   		return "Features";
	   	} else if (uuid.equals(UUID_SENSOR_LOCATION)) {
	   		return "Sensor Location";
	   	} else if(uuid.equals(UUID_PREFERRED_PARAMETERS)) {
	   		return "Preferred Params";
	   	} else if(uuid.equals(UUID_APPEARENCE)) {
	   		return "Appearence";
	   	} else if(uuid.equals(UUID_CLIENT_CHARACTERISTICS)) {
	   		return "Client Characteristics";
	   	} else if(uuid.equals(UUID_CONTROL_POINT)) {
	   		return "Control Point";
	   	} else if(uuid.equals(UUID_SERVICE_CYCLING_SPEED)) {
			return "Cycling Speed and Cadence";
		} else if(uuid.equals(UUID_SERVICE_GENERIC_ACCESS)) {
			return "Generic Access";
		} else if(uuid.equals(UUID_SERVICE_GENERIC_ATTRIBUTE)) {
			return "Generic Attribute";
		} else if(uuid.equals(UUID_SERVICE_DEVICE_INFORMATION)) {
			return "Device Information";
		} else if(uuid.equals(UUID_SERVICE_BATTERY)) {
			return "Battery";
		} else if(uuid.equals(UUID_SYSTEM_ID)) {
			return "System ID";
		} else if(uuid.equals(UUID_CERTIFICATION)) {
			return "Regulatory Certification";
		} else if((uuid.equals(UUID_RUNNING_SPEED)) || (uuid.equals(UUID_SERVICE_RUNNING_SPEED))) {
			return "Running Speed";
		} else if(uuid.equals(UUID_RUNNING_FEATURE)) {
			return "Running Speed Feature";
		} else if(uuid.equals(UUID_MODEL)) {
			return "Model Number";
		} else if(uuid.equals(UUID_BODY_LOCATION)) {
			return "Body Sensor Location";
		} else if(uuid.equals(UUID_HEART_CP)) {
			return "Heart Control Point";
	   	} else {
	   		return uuid.toString();
	   	}
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
 
	
	
}

