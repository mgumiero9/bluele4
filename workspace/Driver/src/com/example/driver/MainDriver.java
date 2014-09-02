package com.example.driver;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Deque;
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
import android.content.Context;
import android.content.Intent;
import android.media.audiofx.AudioEffect.Descriptor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;


public class MainDriver extends Service implements BluetoothProfile {

	final int WARNING_STOP_DISCOVERY = 0x01;
	final int WARNING_FIND_DEVICE = 0x02;
	final int WARNING_NEW_DATA = 0x03;
	
	
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
	
		
		
	final UUID UUID_SYSTEM_ID = UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb");
	final UUID UUID_CERTIFICATION = UUID.fromString("00002a2a-0000-1000-8000-00805f9b34fb");
	final UUID UUID_MODEL = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
		
	public enum OPERATION { READ, WRITE, SET_NOT};
	class ProcessIO {
		public ProcessIO(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic,
				BluetoothGattDescriptor descriptor, OPERATION operation) {
			this.gatt = gatt;
			this.characteristic = characteristic;
			this.descriptor = descriptor;
			this.operation = operation;
		}
		public BluetoothGatt gatt;
		public BluetoothGattCharacteristic characteristic;
		public BluetoothGattDescriptor descriptor;
		public OPERATION operation;
	};
	LinkedList<ProcessIO> operations = new LinkedList<MainDriver.ProcessIO>();
	 
	private static final String MAIN_DRIVER = "MAIN DRIVER";
	private static final long SCAN_PERIOD = 10000;
	private BluetoothAdapter mBluetoothAdapter = null;
	private Handler mHandler;
	private Handler mDiscoveryHandler = new Handler();
	private List<BtDevice> lstDevices = new ArrayList<BtDevice>();
	
	private Runnable DiscoveringTime = new Runnable() {
		@Override
		public void run() {
			if (mBluetoothAdapter != null) {
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
				mHandler.sendMessage(Message.obtain(mHandler, WARNING_STOP_DISCOVERY, ""));
			}
		}
	};
	
	private Runnable ReadWriteTimeout = new Runnable() {
		@Override
		public void run() {
			Log.d(MAIN_DRIVER, "Read/Write Operation Timeout");
			NextOperation();
		}
	};
	
	/*
	 * Register callback and interface (probably on)
	 */
	public boolean init(BluetoothAdapter adapter, Handler handler) {
		mBluetoothAdapter = adapter;
		mHandler = handler;
		return false;
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
            return true;
        }
		return false;
	}
	
	public void stopDiscovery() {
		if ((mBluetoothAdapter != null) && (mBluetoothAdapter.isDiscovering()))
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		mDiscoveryHandler.removeCallbacks(DiscoveringTime);
	}
	
	/*
	 * Each device is stored in a local (should be local?) list (should be hash?).
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = 
			new BluetoothAdapter.LeScanCallback() {
				@Override
				public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
					Log.d(MAIN_DRIVER, "Found Device "+ device.getName() + "-" + device.getAddress() + "-" + device.getType());
					if (!lstDevices.isEmpty())
						for (BtDevice btDevice : lstDevices) {
							if (btDevice.device.getAddress().equals(device.getAddress()))
								return;
						}
					Log.d(MAIN_DRIVER, "New Device Found");
					lstDevices.add(new BtDevice(device, STATE_DISCONNECTED));
					Log.d(MAIN_DRIVER, "Sending Message: New Device Found");
					mHandler.sendMessage(Message.obtain(mHandler, WARNING_FIND_DEVICE, 
							device.getAddress() + " : " + device.getName() + "(" + device.getType() + ")"));
				}
				
			};
			
	public boolean disconnect(String address) {
		for (BtDevice btDevice : lstDevices) {
			if (btDevice.device.getAddress().equals(address)) {
				btDevice.gatt.disconnect();
				btDevice.state = STATE_DISCONNECTING;
				Log.d(MAIN_DRIVER, "Disconnecting " + address);
				return true;
			}
		}
		return false;
	}

	public boolean connect(String address) {
		stopDiscovery();
		for (BtDevice btDevice : lstDevices) {
			if (btDevice.device.getAddress().equals(address)) {
				if ((btDevice.state != STATE_CONNECTED) && (btDevice.state != STATE_CONNECTING)) {
					btDevice.gatt = btDevice.device.connectGatt(this, true, mGattCallback);
					btDevice.state = STATE_CONNECTING;
					Log.d(MAIN_DRIVER, "Connecting " + address);
					return true;
				}
			}
		}
		return false;
	}

    public boolean setNotification(String address, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
		for (BtDevice btDevice : lstDevices) {
			if (btDevice.device.getAddress().equals(address)) {
				Log.d(MAIN_DRIVER, "Setting Notification");
				btDevice.gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
				BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(UUID_CLIENT_CHARACTERISTICS);
				descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				operations.add(new ProcessIO(btDevice.gatt, bluetoothGattCharacteristic, descriptor, OPERATION.WRITE));
				return true;
			}
		}
		return false;
    }

	
	final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
    		
		// When the device is connected for the first time, scan for services
		// ???? How to known whether values are updated or its necessary readCharacteristics ????
		@Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				for (BtDevice btDevice : lstDevices) {
					if (btDevice.gatt.equals(gatt)) {
						btDevice.state = newState;
						// DEBUG
						switch(newState) {
						case STATE_CONNECTED:
							Log.d(MAIN_DRIVER, "Device " + btDevice.device.getAddress() + " CONNECTED");
							break;
						case STATE_DISCONNECTED:
							Log.d(MAIN_DRIVER, "Device " + btDevice.device.getAddress() + " DISCONNECTED");
							break;
						case STATE_DISCONNECTING:
							Log.d(MAIN_DRIVER, "Device " + btDevice.device.getAddress() + " DISCONNECTING");
							break;
						case STATE_CONNECTING:
							Log.d(MAIN_DRIVER, "Device " + btDevice.device.getAddress() + " CONNECTING");
							break;
						}
						// DEBUG
						if (newState == STATE_CONNECTED) {
							//if ((btDevice.gatt.getServices() == null) || (btDevice.gatt.getServices().isEmpty())){
								Log.d(MAIN_DRIVER, "Getting Services");
								btDevice.gatt.discoverServices();
							//} else {
							//	Log.d(MAIN_DRIVER, "Services Already Updated");
							//	listServices(gatt, true);
							//}
						}
						return;
					}
				}
			}
			else
				Log.d(MAIN_DRIVER, "Connection State Error (" + status + ")");
        }

        // New services discovered
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        	if (status == BluetoothGatt.GATT_SUCCESS) {
    			Log.d(MAIN_DRIVER, "SERVICES DISCOVERED");
    			listServices(gatt, true);
            } else {
                Log.w(MAIN_DRIVER, "onServicesDiscovered received: " + status);
            }
        }

        
        
        public void onCharacteristicWrite(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic, int status) {
        	Log.d(MAIN_DRIVER, "Characteristic Written (" + status + ")");
        	
        }

        // Result of a characteristic read operation
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
            			(characteristic.getUuid().equals(UUID_SENSOR_LOCATION))) {
            		msg = uuidToString(characteristic.getUuid()) + "=" + String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)) + "; ";
            	} else {
            		msg = uuidToString(characteristic.getUuid()) +"="+ characteristic.getStringValue(0) + " " + 
            	characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0) + " " +
            	characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0) + " " +
            	characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 0) + " (" + characteristic.getProperties() + ")";
            	}
            	Log.d(MAIN_DRIVER, "Read " + msg);
            } else {
            	Log.d(MAIN_DRIVER, "Error on Read Characteristic " +  characteristic.getUuid().toString());
            }
            
            NextOperation();
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
			Log.d(MAIN_DRIVER, "------------ Sth Changed in " + characteristic.getUuid().toString());
			if (characteristic.getUuid().equals(UUID_CSC_MEASUREMENT)) {
				Log.d(MAIN_DRIVER, "Read size=" + characteristic.getValue().length);
				Log.d(MAIN_DRIVER, "Read flag=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
				int pos = 1;
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x01) == 0x01) {
					int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, pos);
					pos = pos + 4;
					int ts = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos);
					Log.d(MAIN_DRIVER, "Read Value Rev=" + value);
					Log.d(MAIN_DRIVER, "Read TS Rev=" + ts);
					mHandler.sendMessage(Message.obtain(mHandler, WARNING_NEW_DATA, value, ts, gatt.getDevice().getAddress()));
				}
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x02) == 0x02) {
					Log.d(MAIN_DRIVER, "Crank Val=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos));
					pos = pos + 2;
					Log.d(MAIN_DRIVER, "Crank TS=" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, pos));
					
				}
			
			}
		}
		
	    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
	    	int value = descriptor.getValue()[0] + descriptor.getValue()[1] * 256;
	    	if(descriptor.getUuid().equals(UUID_CLIENT_CHARACTERISTICS)) {
	    		Log.d(MAIN_DRIVER, "Read " + uuidToString(descriptor.getCharacteristic().getUuid()) + " Descriptor=" + value + "; ");
	    	} else {
	    		Log.d(MAIN_DRIVER, "Read " + uuidToString(descriptor.getCharacteristic().getUuid()) + " Descriptor=" + descriptor.getUuid().toString() + "=" + value + "; ");
	    	}
	    }
	    
	    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
	    	
	    	if(descriptor.getUuid().equals(UUID_CLIENT_CHARACTERISTICS)) {
	    		int value = descriptor.getValue()[0] + descriptor.getValue()[1] * 256;
	    		Log.d(MAIN_DRIVER, "Cliente Characteristics from " + uuidToString(descriptor.getCharacteristic().getUuid()) + " Read=" + value + " ("+ status + "); ");
	    	}
	    	NextOperation();
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
		for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
			Log.d(MAIN_DRIVER, "Found Service " + uuidToString(bluetoothGattService.getUuid()) + "(" + bluetoothGattService.getType() +")");
    			
			for(BluetoothGattCharacteristic bluetoothGattCharacteristic: bluetoothGattService.getCharacteristics()) {
				if(bluetoothGattCharacteristic.getDescriptors().isEmpty()) {
					Log.d(MAIN_DRIVER, "Characteristic:" + uuidToString(bluetoothGattCharacteristic.getUuid()) + "(" + bluetoothGattCharacteristic.getPermissions() + ")");
					//if ((update) && ((bluetoothGattCharacteristic.getPermissions() & BluetoothGattCharacteristic.PERMISSION_READ) == BluetoothGattCharacteristic.PERMISSION_READ)) {
					if (update) {
						operations.add(new ProcessIO(gatt, bluetoothGattCharacteristic, null, OPERATION.READ));
					}
				} else {
					// Dont work for CSC but test for scale
					operations.add(new ProcessIO(gatt, bluetoothGattCharacteristic, null, OPERATION.READ));
    				for(BluetoothGattDescriptor bluetoothGattDescriptor: bluetoothGattCharacteristic.getDescriptors()) {
    					Log.d(MAIN_DRIVER, "Characteristic:" + uuidToString(bluetoothGattCharacteristic.getUuid()) + " Descriptor:" + uuidToString(bluetoothGattDescriptor.getUuid())  + "(" + bluetoothGattDescriptor.getPermissions() + ")");
    					//if ((update) && ((bluetoothGattDescriptor.getPermissions() & BluetoothGattDescriptor.PERMISSION_READ) == BluetoothGattDescriptor.PERMISSION_READ)) {
    					if (update) {
    						operations.add(new ProcessIO(gatt, bluetoothGattCharacteristic, bluetoothGattDescriptor, OPERATION.READ));
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
					operations.add(new ProcessIO(gatt, bluetoothGattCharacteristic, null, OPERATION.SET_NOT));
				}
			}
		}
		NextOperation();
    }
	

	public void NextOperation() {
		mHandler.removeCallbacks(ReadWriteTimeout);
		Log.d(MAIN_DRIVER, "Issuing next operation");
        if (!operations.isEmpty())
        {
        	ProcessIO newProcess = operations.poll();
        	if (newProcess.operation == OPERATION.READ) {
        		if (newProcess.descriptor != null) {
        			newProcess.gatt.readDescriptor(newProcess.descriptor);
        		} else {
        			newProcess.gatt.readCharacteristic(newProcess.characteristic);
        		}
        	} else if (newProcess.operation == OPERATION.WRITE){
        		if (newProcess.descriptor != null) {
        			newProcess.gatt.writeDescriptor(newProcess.descriptor);
        		} else {
        			newProcess.gatt.writeCharacteristic(newProcess.characteristic);
        		}
        	} else if (newProcess.operation == OPERATION.SET_NOT) {
        		setNotification(newProcess.gatt.getDevice().getAddress(), newProcess.characteristic);
        	}
            mHandler.postDelayed(ReadWriteTimeout, 3000);
        }   
	}
	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
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
		} else if(uuid.equals(UUID_MODEL)) {
			return "Model Number";
	   	} else {
	   		return uuid.toString();
	   	}
	}
	
}
