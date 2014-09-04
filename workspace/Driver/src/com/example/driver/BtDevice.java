package com.example.driver;

import java.util.LinkedList;
import java.util.UUID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

public class BtDevice {
	public BluetoothGatt gatt = null;
	public int state = 0;
	public int init_state = 0;
	public LinkedList<ProcessIO> operations = new LinkedList<ProcessIO>();


	public enum OPERATION { READ, WRITE, SET_NOT};
	public class ProcessIO {
		public ProcessIO( BluetoothGattCharacteristic characteristic,
				BluetoothGattDescriptor descriptor, OPERATION operation) {
			this.characteristic = characteristic;
			this.descriptor = descriptor;
			this.operation = operation;
		}
		public BluetoothGattCharacteristic characteristic;
		public BluetoothGattDescriptor descriptor;
		public OPERATION operation;
	};

	public Runnable ReadWriteTimeout = new Runnable() {
		@Override
		public void run() {
			Log.e("BT DEVICE", "Read/Write Operation Timeout on address " + gatt.getDevice().getAddress());
			execNextOperation();
		}
	};
	
	/**
	 * States connecting or disconnecting has time limit
	 */
	public Runnable ConnectingTime = new Runnable() {
		@Override
		public void run() {
			Log.e("BT DEVICE", "Connecting Operation Timeout on address " + gatt.getDevice().getAddress());
			if (state == MainDriver.STATE_CONNECTING) {
				state = MainDriver.STATE_DISCONNECTED;
				gatt.disconnect();
			}
			if (state == MainDriver.STATE_DISCONNECTING) {
				state = MainDriver.STATE_CONNECTED;
				
			}
		}
	};


	
	public void addOperation(BluetoothGattCharacteristic characteristic,
				BluetoothGattDescriptor descriptor, OPERATION operation) {
		operations.add(new ProcessIO(characteristic, descriptor, operation));
	}
	
	final UUID UUID_CLIENT_CHARACTERISTICS = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");	// Descriptor
	
    public void setNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
		Log.d("BT DEVICE", "Setting Notification");
		gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
		BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(UUID_CLIENT_CHARACTERISTICS);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		addOperation(bluetoothGattCharacteristic, descriptor, BtDevice.OPERATION.WRITE);
    }
	
	
	public boolean execNextOperation() {
        if (!operations.isEmpty())
        {
	    	ProcessIO newProcess = operations.poll();
	    	if (newProcess.operation == OPERATION.READ) {
	    		if (newProcess.descriptor != null) {
	    			gatt.readDescriptor(newProcess.descriptor);
	    		} else {
	    			gatt.readCharacteristic(newProcess.characteristic);
	    		}
	    	} else if (newProcess.operation == OPERATION.WRITE){
	    		if (newProcess.descriptor != null) {
	    			gatt.writeDescriptor(newProcess.descriptor);
	    		} else {
	    			gatt.writeCharacteristic(newProcess.characteristic);
	    		}
	    	} else if (newProcess.operation == OPERATION.SET_NOT) {
	    		setNotification(newProcess.characteristic);
	    	}
	    	return true;
        }
        return false;
	}
	
	public BtDevice(int state) {
		this.state = state;
	}
	
}
