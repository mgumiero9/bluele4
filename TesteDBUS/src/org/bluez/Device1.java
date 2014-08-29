package org.bluez;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;
import org.freedesktop.dbus.DBusMemberName;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.UInt32;
import org.bluez.dbus.DBusProperties;
import org.bluez.dbus.DBusProperties.DBusProperty;
import org.bluez.dbus.DBusProperties.DBusPropertyAccessType;

@DBusInterfaceName("Device1")
public interface Device1 extends DBusInterface
{
	@DBusMemberName("Connect")
	public void Connect();
	
	public void Disconnect();
	public void ConnectProfile(String uuid);
	public void DisconnectProfile(String uuid);
	public void Pair();
	public void CancelPairing();

  
  
}
