package com.example.teste1;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;

public class Bluetooth_CDCDevice implements BluetoothProfile {

	@Override
	public List<BluetoothDevice> getConnectedDevices() {
		
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
	
}
