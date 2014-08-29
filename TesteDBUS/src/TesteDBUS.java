import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bluez.Adapter1;
import org.bluez.Device1;
import org.bluez.HeartRateManager1;
import org.freedesktop.DBus;
import org.freedesktop.DBus.Introspectable;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.ObjectManager;
import org.freedesktop.dbus.ObjectPath;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.viewer.DBusEntry;


public class TesteDBUS {
	
	private static List<String> bInterfaces;
	public static void main(String [] args)
	{
		DBusConnection conn;
		try {
			conn = DBusConnection.getConnection(DBusConnection.SYSTEM);

			DBusEntry e = new DBusEntry();
			e.setName("org.bluez");
			e.setPath("/");
			Introspectable introspectable = conn.getRemoteObject("org.bluez", "/", Introspectable.class);
			e.setIntrospectable(introspectable);

			String introspectData = e.getIntrospectable().Introspect();
			
				
/*		    Document document = builder.parse(new InputSource(new StringReader(introspectData.replace(DOC_TYPE, ""))));
		    Element root = document.getDocumentElement();
				
		    NodeList children = root.getChildNodes();
		    for (int i=0;i<children.getLength();i++) {
		    	Node node = children.item(i);
		        if (Node.ELEMENT_NODE != node.getNodeType()) {
		        	continue;
		        }
		        if ("node".equals(node.getNodeName())) {
			        Node nameNode = node.getAttributes().getNamedItem("name");
			        if (nameNode!=null) {
			        	try {
				        	if (path.endsWith("/")) {
				        		visitNode(name, path + nameNode.getNodeValue());
				        	} else {
				        		visitNode(name, path + '/' + nameNode.getNodeValue());
				        	}
			        	} catch (DBusException ex) {
			        		ex.printStackTrace();
			        	}
			        }
		        }
		    	
		    }
*/
			System.out.println("Lista de Dispositivos");
			
			System.out.println(introspectData);
			Adapter1 adp = null;
			try
			{
				adp = conn.getRemoteObject("org.bluez", "/org/bluez/hci1", Adapter1.class);
				
			} catch (Exception De) {  
				System.out.println("Interface Error");
				System.out.println(De);
		         System.exit(1);  
			}
			
			
			try {
				ObjectManager objManager = conn.getRemoteObject("org.bluez", "/", ObjectManager.class);

				Map<Path,Map<String,Map<String,Variant>>> l = objManager.GetManagedObjects();

			    for (Map.Entry<Path,Map<String,Map<String,Variant>>> entry : l.entrySet()) {
			    	Path key = entry.getKey();
			    	System.out.println("\n");
			    	System.out.println(key);
			    	Map<String,Map<String,Variant>> value = entry.getValue();
			    	for (Map.Entry<String,Map<String,Variant>> entry1 : value.entrySet()) {
			    		String key1 = entry1.getKey();
			    		System.out.println(key1);
			    		Map<String,Variant> value1 = entry1.getValue();
			    		for (Map.Entry<String,Variant> entry2 : value1.entrySet()) {
			    			String key2 = entry2.getKey();
			    			Variant value3 = entry2.getValue();
			    			System.out.print(key2 + "\t" +value3 + "\t");
			    		}
			    	}
			    }
				
			
			} catch (DBusException e3) {
				// TODO Auto-generated catch block
				System.out.print("ObjectManager");
				e3.printStackTrace();
			}
			
			System.out.println("Get Adapter OK ");
				//adp.StartDiscovery();
				//adp.StopDiscovery();
				//device.Connect();
				
				//HeartRateManager1 hrm = null;
				//hrm.RegisterWatcher(d);
				
				//device.Connect();
				
				
				
				
				
				
				//base.GetManagedObjects();
				
				/*
				System.out.println("Stopping Discovery");
				try
				{
					adp.StopDiscovery();
				}catch (DBusExecutionException Dee) {
					System.out.println("Execution Error");
					System.out.println(Dee);
				}catch (Exception De) {  
					System.out.println("Error StartDiscovery");
					System.out.println(De);
					System.exit(1);  
				}
				*/
				conn.disconnect();
			} catch (DBusException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		
			/*
			Device1 device = null;
			
			try {
				device = conn.getRemoteObject("org.bluez", "/org/bluez/hci1/dev_00_18_8C_30_E6_3C", Device1.class);
			} catch (DBusException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			*/
			//System.out.println("Device : "+ device.toString());		
			
		
		
	}
	public static void test()
	{
		System.out.println("Chegou\n");
	}
}


