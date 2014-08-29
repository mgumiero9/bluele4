package org.bluez;

import org.bluez.dbus.DBusProperties.DBusProperty;
import org.bluez.dbus.DBusProperties.DBusPropertyAccessType;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.bluez.dbus.DBusProperties;
import org.bluez.*;

@DBusInterfaceName("org.bluez.Manager")
public interface Manager extends DBusInterface, DBusProperties.PropertiesAccess {
	
public static enum Properties implements DBusProperties.PropertyEnum {
/**
* List of adapter object paths.
*/
@DBusProperty(type = Path[].class, access = DBusPropertyAccessType.READONLY)
Adapters
}
/**
* Returns object path for the default adapter.
*
* @return returns Object Path
*/
Path DefaultAdapter() throws Error.InvalidArguments, Error.NoSuchAdapter;
/**
* Returns object path for the specified adapter.
*
* @param pattern
* "hci0" or "00:11:22:33:44:55"
* @return returns Object Path
*/
Path FindAdapter(String pattern) throws Error.InvalidArguments, Error.NoSuchAdapter;
/**
* Returns list of adapter object paths under /org/bluez
*
* @return returns Path[]
*/
Path[] ListAdapters() throws Error.InvalidArguments, Error.Failed, Error.OutOfMemory;
/**
* This signal indicates a changed value of the given property.
*/
@DBusInterfaceName("org.bluez.Manager.AdapterAdded")
public class PropertyChanged extends DBusSignal {
public PropertyChanged(String path, String name, Variant<Object> value) throws DBusException {
super(path);
}
}
/**
* Parameter is object path of added adapter.
*/
@DBusInterfaceName("org.bluez.Manager.AdapterAdded")
public class AdapterAdded extends DBusSignal {
public AdapterAdded(String path, Path adapter) throws DBusException {
super(path, adapter);
}
}
/**
* Parameter is object path of removed adapter.
*/
@DBusInterfaceName("org.bluez.Manager.AdapterAdded")
public class AdapterRemoved extends DBusSignal {
public AdapterRemoved(String path, Path adapter) throws DBusException {
super(path, adapter);
}
}
/**
* Parameter is object path of the new default adapter.
*
* In case all adapters are removed this signal will not be emitted. The
* AdapterRemoved signal has to be used to detect that no default adapter is
* selected or available anymore.
*/
@DBusInterfaceName("org.bluez.Manager.AdapterAdded")
public class DefaultAdapterChanged extends DBusSignal {
public DefaultAdapterChanged(String path, Path adapter) throws DBusException {
super(path, adapter);
}
}
}