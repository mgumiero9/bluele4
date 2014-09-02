package com.example.driver;

import java.util.List;

import android.bluetooth.BluetoothDevice;

public class BluetoothCDC implements android.bluetooth.BluetoothProfile {

	
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

}
