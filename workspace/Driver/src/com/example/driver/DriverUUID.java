package com.example.driver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

public class DriverUUID {
	
	/**
	 * 
	 * @author asantos
	 *
	 */
	public static enum FORMAT {_UTF8S, _UINT8, _UINT16, _UINT8_16, _UINT8_P, BODY_LOCATION, SENSOR_LOCATION, TEMP, 
		CONN_PARMS, RUN_FEATURE, CODE, EN_DISA, ADDRESS, CSC, RSC, HEART, SCALE, KOR_DATE };

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

	final static String[] TXT_BODY_SENSOR_LOCATION  = {"Outra", "Peito", "Pulso", "Dedo", "Mão", "Ouvido", "Pé"};
	final static String[] TXT_SENSOR_LOCATION = {"Outra", "Em Cima do Tênis", "Dentro do Tênis", "Hip", "Roda da Frente", "Manivela Esquerdo",  
		"Manivela Direita", "Pedal Esquerdo", "Pedal Direito", "Fron Hub", "Rear Dropout", "Corrente", "Roda Traseira", "Read Hub", "Peito"};
	final static String[] TXT_TEMP_TYPE = {"Não Usado", "Axila", "Corpo", "Ouvido", "Dedo Mão", "Gastro-intestinal", "Boca", "Reto", "Dedo Pé", "Tímpano"};

	final static String TXT_NOT_DEFINED = "Não Definido";
	final static String TXT_ENABLED = "Habilitado";
	final static String TXT_DISABLED = "Desabilitado";

	final static long crank_time = 0;
	static long crank_last = 0;
	final static long wheel_time = 0;
	static long wheel_last = 0;

	
	/**
	 * Map of each service 	
	 */
	@SuppressWarnings("serial")
	public static final Map<UUID, Integer> UUID_SERVICES = new HashMap<UUID, Integer>() {{
		put(UUID.fromString("00001800-0000-1000-8000-00805f9b34fb"), R.string.SERVICE_GENERIC_ACCESS);
		put(UUID.fromString("00001801-0000-1000-8000-00805f9b34fb"), R.string.SERVICE_GENERIC_ATTRIBUTE);
		put(UUID.fromString("00001802-0000-1000-8000-00805f9b34fb"), R.string.SERVICE_ALERT);
		put(UUID.fromString("00001809-0000-1000-8000-00805f9b34fb"), R.string.SERVICE_HEALTH);
		put(UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb"), R.string.SERVICE_DEVICE_INFORMATION);
		put(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"), R.string.SERVICE_HEART_MEASUREMENT);
		put(UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb"), R.string.SERVICE_BATTERY);
		put(UUID.fromString("00001814-0000-1000-8000-00805f9b34fb"), R.string.SERVICE_RUNNING_SPEED);
		put(UUID.fromString("00001816-0000-1000-8000-00805f9b34fb"), R.string.SERVICE_CYCLING_SPEED);
		put(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"), R.string.SERVICE_SCALE_COMM);
		put(UUID.fromString("f000ff00-0451-4000-b000-000000000000"), R.string.SERVICE_KOREX);
		
	}};
		
	/**
	 * Map of each characteristic
	 */
	@SuppressWarnings("serial")
	public static final Map<UUID, Details> UUID_CHARACTERISTICS = new HashMap<UUID, Details>() {{
		
		// Characteristics - Read
		put(UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_DEVICE_NAME, FORMAT._UTF8S));
		put(UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_APPEARENCE, FORMAT.CODE));
		put(UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_PREFERRED_PARAMETERS, FORMAT.CONN_PARMS)); 
		put(UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_SERIAL_NUMBER, FORMAT._UTF8S));
		put(UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_HARDWARE_REV, FORMAT._UTF8S));
		put(UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_FIRMWARE_REV, FORMAT._UTF8S));
		put(UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_SOFTWARE_REV, FORMAT._UTF8S));
		put(UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_MANUFACTURER, FORMAT._UTF8S));
		put(UUID.fromString("00002a50-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_PNP_ID, FORMAT._UINT8_16));
		
		put(UUID.fromString("00002a54-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_RUNNING_FEATURE, FORMAT.RUN_FEATURE));
		put(UUID.fromString("00002a53-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_RUNNING_SPEED, FORMAT.RSC));
		// SERVICE BATTERY
		put(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_BATTERY_LEVEL, FORMAT._UINT8_P));
		
		
		put(UUID.fromString("00002a5b-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_CSC_MEASUREMENT, FORMAT.CSC));
				
		put(UUID.fromString("00002a5c-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_CSC_FEATURE, FORMAT._UINT16));
		// CSC_FEATURE: Bit 0 - Wheel Revolution Data Supported
		// CSC_FEATURE: Bit 1 - Crank Revolution Data Supported 	
		// CSC_FEATURE: Bit 2 - Multiple Sensor Locations Supported
		put(UUID.fromString("00002a5d-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_SENSOR_LOCATION, FORMAT.SENSOR_LOCATION));
		put(UUID.fromString("00002a55-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_CONTROL_POINT, FORMAT._UINT16));

		put(UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_BODY_LOCATION, FORMAT.BODY_LOCATION));
		put(UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_HEART_CP, FORMAT._UINT8));
		put(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_HEART_MEASUREMENT, FORMAT.HEART));	
			
		put(UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_SYSTEM_ID, FORMAT._UINT8));
		put(UUID.fromString("00002a2a-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_CERTIFICATION, FORMAT._UINT8));
		put(UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_MODEL, FORMAT._UINT8));
		
		put(UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_SERVICE_CHANGED, FORMAT._UINT8));
		
		put(UUID.fromString("00002a02-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_PRIVACY_FLAG, FORMAT.EN_DISA));
		put(UUID.fromString("00002a03-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_RECONNECTION_ADDRESS, FORMAT.ADDRESS));

		put(UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_ALERT_LEVEL, FORMAT._UINT8));
		
		
		put(UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_TEMPERATURE_MEASUREMENT, FORMAT.TEMP));
		put(UUID.fromString("00002a1e-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_TEMPERATURE_INTERMEDIARE, FORMAT.TEMP));

		
		put(UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455"), new Details(R.string.UNKNOWN_NAME, FORMAT._UINT8));
		put(UUID.fromString("49535343-6daa-4d02-abf6-19569aca69fe"), new Details(R.string.UNKNOWN_NAME, FORMAT._UINT8));
		put(UUID.fromString("49535343-aca3-481c-91ce-d85e28a60318"), new Details(R.string.UNKNOWN_NAME, FORMAT._UINT8));
		put(UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb"), new Details(R.string.UNKNOWN_NAME, FORMAT._UINT8));

		
		put(UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_SCALE_READ, FORMAT.SCALE));
		put(UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"), new Details(R.string.CHAR_SCALE_WRITE, FORMAT._UINT8));
		
		put(UUID.fromString("f000ff01-0451-4000-b000-000000000000"), new Details(R.string.CHAR_KOREX_DATE, FORMAT.KOR_DATE));
		put(UUID.fromString("f000ff02-0451-4000-b000-000000000000"), new Details(R.string.CHAR_KOREX_DATE, FORMAT.KOR_DATE));
		put(UUID.fromString("f000ff03-0451-4000-b000-000000000000"), new Details(R.string.CHAR_KOREX_DATE, FORMAT.KOR_DATE));
		put(UUID.fromString("f000ff04-0451-4000-b000-000000000000"), new Details(R.string.CHAR_KOREX_DATE, FORMAT.KOR_DATE));
		put(UUID.fromString("f000ff05-0451-4000-b000-000000000000"), new Details(R.string.CHAR_KOREX_DATE, FORMAT.KOR_DATE));
		
	}};

	/**
	 * Map of each descriptor 	
	 */
	@SuppressWarnings("serial")
	public static final Map<UUID, Details> UUID_DESCRIPTORS = new HashMap<UUID, Details>() {{
		put(UUID.fromString("00002901-0000-1000-8000-00805f9b34fb"), new Details(R.string.DESC_USER_DESCRIPTION, FORMAT._UTF8S));
		put(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"), new Details(R.string.DESC_CLIENT_CHARACTERISTICS, FORMAT._UINT8));
	}};
	
	public static String ServiceName(Context context, UUID uuid) {
		if (UUID_SERVICES.containsKey(uuid)) {
			return context.getString(UUID_SERVICES.get(uuid));
		}
		return uuid.toString();
	}

	public static String CharacteristicName(Context context, UUID uuid) {
		if (UUID_CHARACTERISTICS.containsKey(uuid)) {
			return context.getString(UUID_CHARACTERISTICS.get(uuid).description);
		}
		return uuid.toString();
	}

	public static String DescriptorName(Context context, UUID uuid) {
		if (UUID_DESCRIPTORS.containsKey(uuid)) {
			return context.getString(UUID_DESCRIPTORS.get(uuid).description);
		}
		return uuid.toString();
	}
	
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public static int uuidToString(UUID uuid) {
		// Multiple values parameters
		if (UUID_CHARACTERISTICS.containsKey(uuid)) {
			return UUID_CHARACTERISTICS.get(uuid).description;
		}
		
		// Single value parameters
		if (UUID_SERVICES.containsKey(uuid)) {
			return UUID_SERVICES.get(uuid);
		}
		if (UUID_DESCRIPTORS.containsKey(uuid)) {
			return UUID_DESCRIPTORS.get(uuid).description;
		}
		
		return R.string.UNKNOWN_NAME;
	}

	/**
	 * 
	 * @param characteristic
	 * @return
	 */
	public static String[] getCharacteristicValue(Context context, BluetoothGattCharacteristic characteristic) {
		
		if (UUID_CHARACTERISTICS.containsKey(characteristic.getUuid())) {
			switch(UUID_CHARACTERISTICS.get(characteristic.getUuid()).format) {
			case _UTF8S:
				return new String[] {context.getString(uuidToString(characteristic.getUuid())),
										characteristic.getStringValue(0)};
			case _UINT8:
				return new String[] {context.getString(uuidToString(characteristic.getUuid())),
										String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0))};
			case _UINT8_P:
				return new String[] {context.getString(uuidToString(characteristic.getUuid())),
						String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)) + " %"};
			case _UINT16:
				return new String[] {context.getString(uuidToString(characteristic.getUuid())),
						String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0))};
			case _UINT8_16:
				if (characteristic.getValue().length == 2)
					return new String[] {context.getString(uuidToString(characteristic.getUuid())),
						String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0))};
				else
					return new String[] {context.getString(uuidToString(characteristic.getUuid())),
						String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0))};
			case BODY_LOCATION:
				int body = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
	    		if (body < TXT_BODY_SENSOR_LOCATION.length)
					return new String[] {context.getString(uuidToString(characteristic.getUuid())),
											TXT_BODY_SENSOR_LOCATION[body]};
	    		else
					return new String[] {context.getString(uuidToString(characteristic.getUuid())),
											TXT_NOT_DEFINED};
			case SENSOR_LOCATION:
				int sensor = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
	    		if (sensor < TXT_BODY_SENSOR_LOCATION.length)
					return new String[] {context.getString(uuidToString(characteristic.getUuid())),
	    									TXT_BODY_SENSOR_LOCATION[sensor]};
	    		else
					return new String[] {context.getString(uuidToString(characteristic.getUuid())),
						TXT_NOT_DEFINED};
			case CONN_PARMS:
				String conn_params = "";
	    		if (characteristic.getValue().length >= 2) {
	    			float minInterval = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0) * 1.25);
	    			conn_params += context.getString(R.string.CONN_MIN_INTERVAL) + ";" + String.valueOf(minInterval) + " ms;";
	    		}
	    		else if (characteristic.getValue().length >= 4) {
	    			float maxInterval = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2) * 1.25);
	    			conn_params += context.getString(R.string.CONN_MAX_INTERVAL) + ";" + String.valueOf(maxInterval) + " ms;";
	    		}
	    		else if (characteristic.getValue().length >= 6) {
		    		int slaveLatency = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 4);
		    		conn_params += context.getString(R.string.CONN_LATENCY) + ";" + String.valueOf(slaveLatency) + ";";
	    		}
	    		else if (characteristic.getValue().length >= 8) {
		    		int supervTimeout = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 6);
		    		conn_params += context.getString(R.string.CONN_MULT_TIMEOUT) + ";" + String.valueOf(supervTimeout);
	    		}
	    		return conn_params.split(";");
			case RUN_FEATURE:
	    		String msg = "";
	    		if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x01) == 0x01) {
	    			msg = msg + "Medição Passo, ";
	    		}
	    		if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x02) == 0x02) {
	    			msg = msg + "Medição Distância Total, ";
	    		}
	    		if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x04) == 0x04) {
	    			msg = msg + "Indicação Corrida ou Caminhada, ";
	    		}
	    		if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x08) == 0x08) {
	    			msg = msg + "Calibração, ";
	    		}
	    		if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x10) == 0x10) {
	    			msg = msg + "Multiplas Localizações, ";
	    		}
	    		if (msg.length() > 3)
	    			msg = msg.substring(0, msg.length() - 2);
	    		return new String[] {context.getString(uuidToString(characteristic.getUuid())), msg};
			case CODE:
				return new String[] {context.getString(uuidToString(characteristic.getUuid())),
						"Código " + String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0))};
			case EN_DISA:
				if (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) > 0)
					return new String[] {context.getString(uuidToString(characteristic.getUuid())), TXT_ENABLED};
				else
					return new String[] {context.getString(uuidToString(characteristic.getUuid())), TXT_DISABLED};
			case ADDRESS:
		    	return new String[] {context.getString(uuidToString(characteristic.getUuid())),
		    				String.format("%02x:%02x:%02x:%02x:%02x:%02x", 
		    				characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0),
		    				characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1),
		    				characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2),
		    				characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3),
		    				characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 4),
		    				characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 5))};
			case CSC:
				String csc_msg = "";
				int csc_pos = 1;
				int csc_flag = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
				if ((csc_flag & 0x01) == 0x01) {
					int wheel_count = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, csc_pos);
					float wheel_speed = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, csc_pos + 4) - crank_time;
					if (wheel_speed < 0)
						wheel_speed += 65536;
					wheel_speed = (wheel_count - wheel_last) / wheel_speed / 1024;
					csc_msg += context.getString(R.string.CSC_WHEEL_REV) + ";" + String.valueOf(wheel_count) + ";" +
							context.getString(R.string.CSC_WHEEL_SPEED) +";" + String.valueOf(wheel_speed) + ";";
					csc_pos = csc_pos + 6;
					wheel_last = wheel_count;
				}
				if (( csc_flag & 0x02) == 0x02) {
					int crank_count = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, csc_pos);
					float crank_speed = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, csc_pos + 2) - crank_time;
					if (crank_speed < 0)
						crank_speed += 65536;
					if (crank_count - crank_last < 0)
						crank_last = crank_last - 65536;
					crank_speed = (crank_count - crank_last) / crank_speed / 1024;
					csc_msg += context.getString(R.string.CSC_CRANK_REV) + ";" + String.valueOf(crank_count) + ";" +
							context.getString(R.string.CSC_CRANK_SPPED) +";" + String.valueOf(crank_speed) + ";";
					crank_last = crank_count;
				}
				return csc_msg.split(";");
			case RSC:
				float instantaneousSpeed = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1) / 256;
				float instantaneousCadence = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3);
				int rsc_pos = 4;
				String rsc_msg = context.getString(R.string.RSC_SPEED) + ";" + String.valueOf(instantaneousSpeed) + ";" + 
						context.getString(R.string.RSC_CADENCE) + ";" + String.valueOf(instantaneousCadence) + ";";
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x01) == 0x01) {
					rsc_msg += context.getString(R.string.RSC_STRIDE_LEN) + ";" + 
							String.valueOf((float)characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, rsc_pos)/100.0) + ";";
					rsc_pos = rsc_pos + 2;
				}
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x02) == 0x02) {
					rsc_msg += context.getString(R.string.RSC_TOTAL_LEN) + ";" + 
							String.valueOf((float)characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, rsc_pos)/10.0) + ";";
				}
				return rsc_msg.split(";");
			case HEART:
				int heart_pos = 1;
				int heart_flag = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
				String heart_msg = "";
				switch ((heart_flag/ 2) & 0x3) {
				case 2:
					heart_msg = "Contato Sensor Cardíaco; Não Detectado;";
				case 3:
					heart_msg = "Contato Sensor Cardíaco; Detectado;";	
				}
				if ((heart_flag & 0x01) == 0x01) {
					heart_msg += context.getString(R.string.HEART_BEATS) + ";" + 
							String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, heart_pos) + ";");
					heart_pos = heart_pos + 2;
				} else {
					heart_msg += context.getString(R.string.HEART_BEATS) + ";" + 
							String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, heart_pos) + ";");
					heart_pos++;
				}
				
				if ((heart_flag & 0x08) == 0x08) {
					heart_msg += context.getString(R.string.HEART_ENERGY) + ";" + 
							String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, heart_pos) + ";");
					heart_pos = heart_pos + 2;
				}
				if ((characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) & 0x10) == 0x10) {
					heart_msg += context.getString(R.string.HEART_RR) + ";" + 
							String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, heart_pos) + ";");
					heart_pos = heart_pos + 2;
				}
				return heart_msg.split(";");

			case TEMP:
				int temp_flag = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
				String temp_msg = "";
				if ((temp_flag & 0x01) == 0x01)
					temp_msg += context.getString(R.string.TEMP_CELSIUS) + ";" + 
							String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1) + ";");
				else
					temp_msg += context.getString(R.string.TEMP_FAHREN) + ";" + 
							String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1) + ";");
				int temp_pos = 5;
				if ((temp_flag & 0x02) == 0x02) {
					int year = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, temp_pos);
					int mon = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, temp_pos + 2);
					int day = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, temp_pos + 3);
					int hour = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, temp_pos + 4);
					int min = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, temp_pos + 5);
					int sec = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, temp_pos + 6);
					String clock = day + "/" + mon + "/" + year + " " + hour + ":" + min + ":" + sec;
					temp_msg += context.getString(R.string.TEMP_TIME) + ";" + clock + ";";
					temp_pos = temp_pos + 7; 
				}
				if ((temp_flag & 0x04) == 0x04) {
					
					if(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, temp_pos) < TXT_TEMP_TYPE.length)
						temp_msg += context.getString(R.string.TEMP_TYPE) + ";" + 
								TXT_TEMP_TYPE[characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, temp_pos)] + ";";
					else
						temp_msg += context.getString(R.string.TEMP_TYPE) + ";" + 
								TXT_NOT_DEFINED + ";";
				}				
				return temp_msg.split(";");
			case SCALE:
				String msg_scale = "";
				if (characteristic.getValue().length != 16) {
					Log.e("DRIVER", "Invalid length on scale receiving");
					return new String[] {};
				}
				int scale_type = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
				msg_scale += context.getString(R.string.SCALE_CAT) + ";";
				switch (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1) & 0x30)
				{
				case 0x10:
					msg_scale += "Amador;";
					break;
				case 0x20:
					msg_scale += "Atleta;";
					break;
				default:
					msg_scale += "Normal;";
					break;
				}
				msg_scale += context.getString(R.string.SCALE_GROUP) + ";P" + 
						String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1) & 0xF) + ";";

				msg_scale += context.getString(R.string.SCALE_GEN) + ";";
				if (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2) == 0) {
					msg_scale += "Feminino;";
				}
				else {
					msg_scale += "Masculino;";
				}
				msg_scale += context.getString(R.string.SCALE_HEIGHT) + ";" +
						String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3) + ";");
				
				float weigth = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 4) * 256 + 
						characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 5));
				
				if ((scale_type == 0xCF) || (scale_type == 0xCE))
					weigth = weigth / 10;
				else if (scale_type == 0xCB)
					weigth = weigth / 100;
				else 
					weigth = weigth / 1;
				msg_scale += context.getString(R.string.SCALE_WEIGHT) + ";" + String.valueOf( weigth) + ";";
				int fat_mass = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 6) * 256 + 
						characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 7);
				msg_scale += context.getString(R.string.SCALE_FAT) + ";" + String.valueOf((float) fat_mass / 10) + ";";
				
				msg_scale += context.getString(R.string.SCALE_BONE) + ";" + String.valueOf(
						(float) characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 8) / 10) + ";";

				int muscle_mass = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 9) * 256 + 
						characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 10);
				msg_scale += context.getString(R.string.SCALE_MUSCLE) + ";" + String.valueOf((float) muscle_mass / 10) + ";";

				msg_scale += context.getString(R.string.SCALE_VISC) + ";" + String.valueOf(
						characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 11)) + ";";

				int water = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 12) * 256 + 
						characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 13);
				msg_scale += context.getString(R.string.SCALE_WATER) + ";" + String.valueOf((float) water / 10) + ";";

				int cal = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 14) * 256 + 
						characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 15);
				msg_scale += context.getString(R.string.SCALE_CAL) + ";" + String.valueOf(cal) + ";";

				return msg_scale.split(";");
			case KOR_DATE:
				String kor_date = "";
				for(int i=0; i<characteristic.getValue().length; i++) {
					kor_date += characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, i) + " ";
				}
				Log.d("KOREX", characteristic.getUuid().toString() + " Recv " + characteristic.getValue().length + " -- " + kor_date);
				break;
				
			}
		}
		return new String[] {"?","?"};
	}
	
}


