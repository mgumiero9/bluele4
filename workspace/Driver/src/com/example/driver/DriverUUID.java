package com.example.driver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.text.GetChars;

public class DriverUUID {
	
	/**
	 * 
	 * @author asantos
	 *
	 */
	public static enum FORMAT {UTF8S, UINT8, UINT16, UINT8_16 };

	/**
	 * Just used for HashMap storage of the each characteristic details 
	 * @author asantos
	 *
	 */
	public static final class Details {
		public FORMAT format;
		public int description;
		Details(int description, FORMAT format) {
			this.format = format;
			this.description = description;
		}
	}
	
	/**
	 * Map of each service 	
	 */
	@SuppressWarnings("serial")
	public static final Map<UUID, Details> UUID_SERVICES = new HashMap<UUID, Details>() {{
		put(UUID.fromString("00001800-0000-1000-8000-00805f9b34fb"), new Details(R.string.SERVICE_GENERIC_ACCESS, FORMAT.UTF8S));
		put(UUID.fromString("00001801-0000-1000-8000-00805f9b34fb"), new Details(R.string.SERVICE_GENERIC_ATTRIBUTE, FORMAT.UTF8S));
		put(UUID.fromString("00001802-0000-1000-8000-00805f9b34fb"), new Details(R.string.SERVICE_ALERT, FORMAT.UTF8S));
		put(UUID.fromString("00001809-0000-1000-8000-00805f9b34fb"), new Details(R.string.SERVICE_HEALTH, FORMAT.UTF8S));
		put(UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb"), new Details(R.string.SERVICE_DEVICE_INFORMATION, FORMAT.UTF8S));
		put(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"), new Details(R.string.SERVICE_HEART_MEASUREMENT, FORMAT.UTF8S));
		put(UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb"), new Details(R.string.SERVICE_BATTERY, FORMAT.UTF8S));
		put(UUID.fromString("00001814-0000-1000-8000-00805f9b34fb"), new Details(R.string.SERVICE_RUNNING_SPEED, FORMAT.UTF8S));
		put(UUID.fromString("00001816-0000-1000-8000-00805f9b34fb"), new Details(R.string.SERVICE_CYCLING_SPEED, FORMAT.UINT16));
	}};
		
	/**
	 * Map of each characteristic
	 */
	@SuppressWarnings("serial")
	public static final Map<UUID, Details> UUID_CHARACTERISTICS = new HashMap<UUID, Details>() {{
		// Characteristics - Read
		put(UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_DEVICE_NAME, FORMAT.UTF8S));
		put(UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_APPEARENCE, FORMAT.UTF8S));
		put(UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_PREFERRED_PARAMETERS, FORMAT.UTF8S)); 
		put(UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_SERIAL_NUMBER, FORMAT.UTF8S));
		put(UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_HARDWARE_REV, FORMAT.UTF8S));
		put(UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_FIRMWARE_REV, FORMAT.UTF8S));
		put(UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_SOFTWARE_REV, FORMAT.UTF8S));
		put(UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_MANUFACTURER, FORMAT.UTF8S));
		put(UUID.fromString("00002a50-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_PNP_ID, FORMAT.UINT8_16));
		
		put(UUID.fromString("00002a54-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_RUNNING_FEATURE, FORMAT.UINT16));
		put(UUID.fromString("00002a53-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_RUNNING_SPEED, FORMAT.UINT16));
		// SERVICE BATTERY
		put(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_BATTERY_LEVEL, FORMAT.UINT8));
		
		
		put(UUID.fromString("00002a5b-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_CSC_MEASUREMENT, FORMAT.UINT16));
				
		put(UUID.fromString("00002a5c-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_CSC_FEATURE, FORMAT.UINT16));
		// CSC_FEATURE: Bit 0 - Wheel Revolution Data Supported
		// CSC_FEATURE: Bit 1 - Crank Revolution Data Supported 	
		// CSC_FEATURE: Bit 2 - Multiple Sensor Locations Supported
		put(UUID.fromString("00002a5d-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_SENSOR_LOCATION, FORMAT.UINT8));
		put(UUID.fromString("00002a55-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_CONTROL_POINT, FORMAT.UINT16));

		put(UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_BODY_LOCATION, FORMAT.UINT8));
		put(UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_HEART_CP, FORMAT.UINT8));
		put(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_HEART_MEASUREMENT, FORMAT.UINT8));	
			
		put(UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_SYSTEM_ID, FORMAT.UINT8));
		put(UUID.fromString("00002a2a-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_CERTIFICATION, FORMAT.UINT8));
		put(UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_MODEL, FORMAT.UINT8));
		
		put(UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_SERVICE_CHANGED, FORMAT.UINT8));
		
		put(UUID.fromString("00002a02-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_PRIVACY_FLAG, FORMAT.UINT8));
		put(UUID.fromString("00002a03-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_RECONNECTION_ADDRESS, FORMAT.UINT8));

		put(UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_ALERT_LEVEL, FORMAT.UINT8));
		
		
		put(UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_TEMPERATURE_MEASUREMENT, FORMAT.UINT8));
		put(UUID.fromString("00002a1e-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_TEMPERATURE_INTERMEDIARE, FORMAT.UINT8));

		
		put(UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455"), new Details(R.string.UNKNOWN_NAME, FORMAT.UINT8));
		put(UUID.fromString("49535343-6daa-4d02-abf6-19569aca69fe"), new Details(R.string.UNKNOWN_NAME, FORMAT.UINT8));
		put(UUID.fromString("49535343-aca3-481c-91ce-d85e28a60318"), new Details(R.string.UNKNOWN_NAME, FORMAT.UINT8));
		put(UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb"), new Details(R.string.UNKNOWN_NAME, FORMAT.UINT8));

		put(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"), new Details(R.string.UNKNOWN_NAME, FORMAT.UINT8));
		put(UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb"), new Details(R.string.UNKNOWN_NAME, FORMAT.UINT8));
		put(UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"), new Details(R.string.UNKNOWN_NAME, FORMAT.UINT8));
		
	}};

	/**
	 * Map of each descriptor 	
	 */
	@SuppressWarnings("serial")
	public static final Map<UUID, Details> UUID_DESCRIPTORS = new HashMap<UUID, Details>() {{
		put(UUID.fromString("00002901-0000-1000-8000-00805f9b34fb"), new Details(R.string.DESC_USER_DESCRIPTION, FORMAT.UTF8S));
		put(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"), new Details(R.string.DESC_CLIENT_CHARACTERISTICS, FORMAT.UINT8));
	}};
	
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public static int uuidToString(UUID uuid) {
		if (UUID_CHARACTERISTICS.containsKey(uuid)) {
			return UUID_CHARACTERISTICS.get(uuid).description;
		}
		if (UUID_SERVICES.containsKey(uuid)) {
			return UUID_SERVICES.get(uuid).description;
		}
		if (UUID_DESCRIPTORS.containsKey(uuid)) {
			return UUID_DESCRIPTORS.get(uuid).description;
		}
		return R.string.UNKNOWN_NAME;
	}

	public static String getCharacteristicValue(BluetoothGattCharacteristic characteristic) {
		if (UUID_CHARACTERISTICS.containsKey(characteristic.getUuid())) {
			switch(UUID_CHARACTERISTICS.get(characteristic.getUuid()).format) {
			case UTF8S:
				return characteristic.getStringValue(0);
			case UINT8:
				return String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
			case UINT16:
				return String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
			case UINT8_16:
				if (characteristic.getValue().length == 2)
					return String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
				else
					return String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
			}
		}
		return "?";
	}
	
}
