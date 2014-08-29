package org.freedesktop.dbus;

import java.util.List;
import java.util.Map;

@DBusInterfaceName("org.freedesktop.DBus.ObjectManager")
public interface ObjectManager extends DBusInterface {

	@DBusMemberName("GetManagedObjects")
	// a{oa{sa{sv}}} }
	public Map<Path,Map<String,Map<String,Variant>>> GetManagedObjects();
	
	

}