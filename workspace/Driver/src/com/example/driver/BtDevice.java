package com.example.driver;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;

public class BtDevice {
	public BluetoothDevice device;
	public BluetoothGatt gatt = null;
	public int state = 0;
	public int init_state = 0;
	
	public BtDevice(BluetoothDevice device, int state) {
		this.device = device;
		this.state = state;
		// TODO Auto-generated constructor stub
	}
	
}
