package com.example.driver;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothHealthAppConfiguration;
import android.bluetooth.BluetoothHealthCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.audiofx.AudioEffect.Descriptor;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


public class MainDriver extends Service implements BluetoothProfile {

	final static String WARNING_STOP_DISCOVERY = "STOP_DISCOVERY";
	final static String WARNING_FIND_DEVICE 	= "FIND_DEVICE";
	final static String WARNING_NEW_DATA 		= "NEW_DATA";
	final static String WARNING_CONNECTED 		= "CONNECTED";
	final static String WARNING_DISCONNECTED   = "DISCONNECTED";	
	
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
		
		
	final UUID UUID_SYSTEM_ID = UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb");
	final UUID UUID_CERTIFICATION = UUID.fromString("00002a2a-0000-1000-8000-00805f9b34fb");
	final UUID UUID_MODEL = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
		
	private static final String MAIN_DRIVER = "MAIN DRIVER";
	private static final long SCAN_PERIOD = 10000;
	
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothManager mBluetoothManager = null;
	private boolean LeDiscovering = false;
	
	private Handler mTimeoutHandler = new Handler();
	public Handler mHandler = new Handler();
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
	
	public void stopDiscovery() {
		mDiscoveryHandler.removeCallbacks(DiscoveringTime);
		if ((mBluetoothAdapter != null) && (LeDiscovering))
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		LeDiscovering = false;
	}
	
	/*
	 * Each device is stored in a local (should be local?) list (should be hash?).
	 */
	private final BluetoothAdapter.LeScanCallback mLeScanCallback = 
			new BluetoothAdapter.LeScanCallback() {
				@Override
				public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
					Log.d(MAIN_DRIVER, "Found Device "+ device.getName() + "-" + device.getAddress() + "-" + device.getType());
					if (!lstDevices.isEmpty() && (lstDevices.containsKey(device.getAddress()))) {
						return;
					}
					Log.d(MAIN_DRIVER, "New Device Found");
					broadcastUpdate(WARNING_FIND_DEVICE, device.getAddress(), device.getName());
				}
				
			};
			
		
	/**
	 * Why would be necessary disconnect ?
	 * @param address
	 * @return
	 */
	public boolean disconnect(String address) {
		BtDevice btDevice = lstDevices.get(address);
		if ((btDevice != null) && (btDevice.state != STATE_DISCONNECTED) && (btDevice.state != STATE_DISCONNECTING)) {
			btDevice.gatt.disconnect();
			btDevice.state = STATE_DISCONNECTING;
			mHandler.postDelayed(btDevice.ConnectingTime, 5000);
			Log.d(MAIN_DRIVER, "Disconnecting " + address);
			
			return true;
		}
		return false;
	}
	
	/**
	 * How will Activity get this address?
	 * 
	 */

	public boolean connect(String address) {
		stopDiscovery();
		Log.d(MAIN_DRIVER, "Verifying " + address);
		if (lstDevices.containsKey(address)) {
			BtDevice btDevice = lstDevices.get(address);
			Log.d(MAIN_DRIVER, "Device " + address + " State = " + btDevice.state);
			if ((btDevice.state != STATE_CONNECTED) && (btDevice.state != STATE_CONNECTING)) {
				if (mBluetoothAdapter.getRemoteDevice(address) == null)
					Log.d(MAIN_DRIVER, "Adapter has no device");
				btDevice.gatt = mBluetoothAdapter.getRemoteDevice(address).connectGatt(this, true, mGattCallback);
				btDevice.state = STATE_CONNECTING;
				mHandler.postDelayed(btDevice.ConnectingTime, 5000);
				Log.d(MAIN_DRIVER, "Connecting " + address);
				return true;
			}
		} else {
			Log.d(MAIN_DRIVER, "Device is not in the list");
		}
		
		return false;
	}

	/**
	 * All device callbacks are handle here
	 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
    		
		// When the device is connected for the first time, scan for services
		// ???? How to known whether values are updated or its necessary readCharacteristics ????
		@Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				BtDevice btDevice = lstDevices.get(gatt.getDevice().getAddress());
				if (btDevice != null) {
					mHandler.removeCallbacks(btDevice.ConnectingTime);
					btDevice.state = newState;
					// DEBUG
					switch(newState) {
					case STATE_CONNECTED:
						Log.d(MAIN_DRIVER, "Device " + gatt.getDevice().getAddress() + " CONNECTED");
						break;
					case STATE_DISCONNECTED:
						Log.d(MAIN_DRIVER, "Device " + gatt.getDevice().getAddress() + " DISCONNECTED");
						break;
					case STATE_DISCONNECTING:
						Log.d(MAIN_DRIVER, "Device " + gatt.getDevice().getAddress() + " DISCONNECTING");
						btDevice.operations.clear();
						break;
					case STATE_CONNECTING:
						Log.d(MAIN_DRIVER, "Device " + gatt.getDevice().getAddress() + " CONNECTING");
						btDevice.operations.clear();
						break;
					}
					// DEBUG
					if (newState == STATE_CONNECTED) {
						//if ((btDevice.gatt.getServices() == null) || (btDevice.gatt.getServices().isEmpty())){
							Log.d(MAIN_DRIVER, "Getting Services");
							btDevice.gatt.discoverServices();
							broadcastUpdate(WARNING_CONNECTED, gatt.getDevice().getAddress(), "");
												//} else {
						//	Log.d(MAIN_DRIVER, "Services Already Updated");
						//	listServices(gatt, true);
						//}
					} else {
						btDevice.operations.clear();
						broadcastUpdate(WARNING_DISCONNECTED, gatt.getDevice().getAddress(), "");
					}
					return;
				} else {
					Log.d(MAIN_DRIVER, "Device not found in callback");
				}
				
			}
			else
				Log.d(MAIN_DRIVER, "Connection State Error (" + status + ")");
        }

        /**
         * Happens after call .discoverServices()
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        	if (status == BluetoothGatt.GATT_SUCCESS) {
				listServices(gatt, true);
            } else {
                Log.w(MAIN_DRIVER, "onServicesDiscovered received: " + status);
            }
        }

        /**
         * Which writes are needed?
         * (non-Javadoc)
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
            
            NextOperation(lstDevices.get(gatt.getDevice().getAddress()));
            /*
            if (index < lstCharacteristics.size()) {
	            if (lstCharacteristics.size() > 1) {
	            	mBluetoothGatt.readCharacteristic(lstCharacteristics.get(index));
	            }
            }	
            index++;
            */            	
            	
        }
		public void onCharacteristicChanged(BluetoothGatt gatt,
		        BluetoothGattCharacteristic characteristic) {
			if (characteristic.getUuid().equals(UUID_CSC_MEASUREMENT)) {
				Log.d(MAIN_DRIVER, "Read size=" + characteristic.getValue().length + " Flag=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
				int pos = 1;
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x01) == 0x01) {
					int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, pos);
					pos = pos + 4;
					int ts = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos);
					broadcastUpdate(WARNING_NEW_DATA, gatt.getDevice().getAddress(), "Value1", String.valueOf(value));
					broadcastUpdate(WARNING_NEW_DATA, gatt.getDevice().getAddress(), "Value2", String.valueOf(ts));
				}
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x02) == 0x02) {
					Log.d(MAIN_DRIVER, "Crank Val=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos));
					broadcastUpdate(WARNING_NEW_DATA, gatt.getDevice().getAddress(), "Value3", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos)));
					pos = pos + 2;
					Log.d(MAIN_DRIVER, "Crank TS=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos));
					broadcastUpdate(WARNING_NEW_DATA, gatt.getDevice().getAddress(), "Value4", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos)));
				}
			}
			else if (characteristic.getUuid().equals(UUID_RUNNING_SPEED)) {
				Log.d(MAIN_DRIVER, "Read size=" + characteristic.getValue().length);
				Log.d(MAIN_DRIVER, "Read flag=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
				int pos = 1;
				int instantaneousSpeed = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos);
				pos = pos + 2;
				broadcastUpdate(WARNING_NEW_DATA, gatt.getDevice().getAddress(), "Value1", String.valueOf(instantaneousSpeed));
				int instantaneousCadence = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, pos);
				pos++;
				broadcastUpdate(WARNING_NEW_DATA, gatt.getDevice().getAddress(), "Value2", String.valueOf(instantaneousCadence));

				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x01) == 0x01) {
					Log.d(MAIN_DRIVER, "Read Avanço=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos));
					broadcastUpdate(WARNING_NEW_DATA, gatt.getDevice().getAddress(), "Value3", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos)));
					pos = pos + 2;
				}
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x02) == 0x02) {
					Log.d(MAIN_DRIVER, "Read Total Distance=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, pos));
					broadcastUpdate(WARNING_NEW_DATA, gatt.getDevice().getAddress(), "Value4", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, pos)));
				}
			}
/*			else if (characteristic.getUuid().equals(UUID_)) {
				Log.d(MAIN_DRIVER, "Read size=" + characteristic.getValue().length);
				Log.d(MAIN_DRIVER, "Read flag=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
				int pos = 1;
				int instantaneousSpeed = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos);
				pos = pos + 2;
				broadcastUpdate(WARNING_NEW_DATA, gatt.getDevice().getAddress(), "Value1", String.valueOf(instantaneousSpeed));
				int instantaneousCadence = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, pos);
				pos++;
				broadcastUpdate(WARNING_NEW_DATA, gatt.getDevice().getAddress(), "Value2", String.valueOf(instantaneousCadence));

				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x01) == 0x01) {
					Log.d(MAIN_DRIVER, "Read Avanço=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos));
					broadcastUpdate(WARNING_NEW_DATA, gatt.getDevice().getAddress(), "Value3", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos)));
					pos = pos + 2;
				}
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x02) == 0x02) {
					Log.d(MAIN_DRIVER, "Read Total Distance=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, pos));
					broadcastUpdate(WARNING_NEW_DATA, gatt.getDevice().getAddress(), "Value4", String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, pos)));
				}
			}*/
			else
				Log.d(MAIN_DRIVER, "------------ Sth Changed in " + characteristic.getUuid().toString());

		}
		
	    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
	    	int value = descriptor.getValue()[0] + descriptor.getValue()[1] * 256;
	    	if(descriptor.getUuid().equals(UUID_CLIENT_CHARACTERISTICS)) {
	    		Log.d(MAIN_DRIVER, "Dev" + gatt.getDevice().getAddress() + "Read " + uuidToString(descriptor.getCharacteristic().getUuid()) + " Descriptor=" + value + "; ");
	    	} else {
	    		Log.d(MAIN_DRIVER, "Dev" + gatt.getDevice().getAddress() + "Read " + uuidToString(descriptor.getCharacteristic().getUuid()) + " Descriptor=" + descriptor.getUuid().toString() + "=" + value + "; ");
	    	}
	    	NextOperation(lstDevices.get(gatt.getDevice().getAddress()));
	    }
	    
	    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
	    	
	    	if(descriptor.getUuid().equals(UUID_CLIENT_CHARACTERISTICS)) {
	    		int value = descriptor.getValue()[0] + descriptor.getValue()[1] * 256;
	    		Log.d(MAIN_DRIVER, "Dev" + gatt.getDevice().getAddress() + "Cliente Characteristics from " + uuidToString(descriptor.getCharacteristic().getUuid()) + " Read=" + value + " ("+ status + "); ");
	    	}
	    	if (lstDevices.containsKey(gatt.getDevice().getAddress()))
	    		NextOperation(lstDevices.get(gatt.getDevice().getAddress()));
	    }
	    
	    /**
	     * Callback invoked when a reliable write transaction has been completed.
	     *
	     * @param gatt GATT client invoked {@link BluetoothGatt#executeReliableWrite}
	     * @param status {@link BluetoothGatt#GATT_SUCCESS} if the reliable write
	     *               transaction was executed successfully
	     */
	    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
	    }

	    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			Log.d(MAIN_DRIVER, "------------ Sth Changed RSSI " + rssi);
	    }
	    
	};
	
    protected void listServices(BluetoothGatt gatt, boolean update) {
    	BtDevice btDevice = lstDevices.get(gatt.getDevice().getAddress());
    	if (btDevice == null)
    		return;
		for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
			Log.d(MAIN_DRIVER, "Found Service " + uuidToString(bluetoothGattService.getUuid()) + "(" + bluetoothGattService.getType() +")");
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
		
		for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
			for(BluetoothGattCharacteristic bluetoothGattCharacteristic: bluetoothGattService.getCharacteristics()) {
				// Trying more generic procedure
				if ((bluetoothGattCharacteristic.getDescriptor(UUID_CLIENT_CHARACTERISTICS) != null)) {
/*				if ((bluetoothGattCharacteristic.getUuid().equals(UUID_CSC_MEASUREMENT)) ||
						(bluetoothGattCharacteristic.getUuid().equals(UUID_CONTROL_POINT)) ||
						(bluetoothGattCharacteristic.getUuid().equals(UUID_BATTERY_LEVEL))) { */
					btDevice.addOperation(bluetoothGattCharacteristic, null, BtDevice.OPERATION.SET_NOT);
				}
			}
		}
		NextOperation(btDevice);
    }
	
	public void NextOperation(BtDevice btDevice) {
		if(btDevice == null)
			return;
		Log.d(MAIN_DRIVER, "Issuing next operation");
		mTimeoutHandler.removeCallbacks(btDevice.ReadWriteTimeout);
		if (btDevice.execNextOperation())
			mTimeoutHandler.postDelayed(btDevice.ReadWriteTimeout, 3000);
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

    }    

    
    /**
     * Android Services
     * Service shoud not 
     */
    
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
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
		if(action.equals(WARNING_FIND_DEVICE)) {
			lstDevices.put(parameter,  new BtDevice(STATE_DISCONNECTED));
			Log.d(MAIN_DRIVER, "Sending Message: New Device Found");
			intent.putExtra("address", parameter + value);
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
	
	
	 
	String uuidToString(UUID uuid) {
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
}

