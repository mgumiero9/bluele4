package com.example.driver;

import java.util.LinkedList;
import java.util.UUID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Handler;
import android.util.Log;

public class BtDevice {
	public BluetoothGatt gatt = null;
	public int state = 0;
	public int init_state = 0;
	private int retries = 0;
	public LinkedList<ProcessIO> operations = new LinkedList<ProcessIO>();
	private ProcessIO lastOperation;
	public boolean updating = false;
	public ReadWriteTimeoutCallback reply;
	

	public enum OPERATION { READ, READ_FINISH, WRITE, SET_NOT};
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
			Log.d("BT DEVICE", "Read/Write Operation Timeout on address " + gatt.getDevice().getAddress());
			// Reinsert the operation
			reply.callbackTimeout();
			execLastOperation();
			
		}
	};
	
	/**
	 * States connecting or disconnecting has time limit
	 */
	public Runnable ConnectingTime = new Runnable() {
		@Override
		public void run() {
		}
	};

	public void clearOperation() {
		operations.clear();
	}
	
	public void addOperation(BluetoothGattCharacteristic characteristic,
				BluetoothGattDescriptor descriptor, OPERATION operation) {
		operations.add(new ProcessIO(characteristic, descriptor, operation));
	}

	public void addOperation(ProcessIO process) {
		if (process != null)
			operations.add(process);
	}

	
	final UUID UUID_CLIENT_CHARACTERISTICS = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");	// Descriptor
	
    public void setNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
		gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
		BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(UUID_CLIENT_CHARACTERISTICS);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		addOperation(bluetoothGattCharacteristic, descriptor, BtDevice.OPERATION.WRITE);
    }
	
    /**
     * 
     * @param process
     * @return
     */
    private boolean execOperation (ProcessIO process) {
    	if (process.operation == OPERATION.READ) {
    		Log.d("OPER", "Reading data");
    		if (process.descriptor != null) {
    			gatt.readDescriptor(process.descriptor);
    		} else {
    			gatt.readCharacteristic(process.characteristic);
    		}
    	} else if (process.operation == OPERATION.WRITE){
    		Log.d("OPER", "Writing data");
    		if (process.descriptor != null) {
    			gatt.writeDescriptor(process.descriptor);
    		} else {
    			gatt.writeCharacteristic(process.characteristic);
    		}
    	} else if (process.operation == OPERATION.SET_NOT) {
    		Log.d("OPER", "Notification data");
    		setNotification(process.characteristic);
    		return false;
    	}
    	return true;
    }
    
    /**
     * 
     * @return
     */
    public boolean execLastOperation() {
    	Log.d("OPER", "Last Operation");
    	if (lastOperation == null)
    		return false;
    	execOperation(lastOperation);
    	//mHandler.postDelayed(ReadWriteTimeout, TIMEOUT_READWRITE);
		return true; 
    }
	
    /**
     * 
     * @return
     */
	public boolean execNextOperation(ReadWriteTimeoutCallback callback) {
		reply = callback;
		Log.d("OPER", "Next Operation");
		//mHandler.removeCallbacks(ReadWriteTimeout);
        if (!operations.isEmpty())
        {
	    	ProcessIO newProcess = operations.poll();
			if (!execOperation(newProcess))
			{
				execNextOperation();
			}
	    	lastOperation = newProcess;
	    	//mHandler.postDelayed(ReadWriteTimeout, TIMEOUT_READWRITE);
	    	return true;
        }
        lastOperation = null;
        return false;
	}
	
	/**
	 * 
	 * @param state
	 */
	public BtDevice(int state) {
		this.state = state;
	}
	
}
