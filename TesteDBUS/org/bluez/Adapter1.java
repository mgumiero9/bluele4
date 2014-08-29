package org.bluez;
import org.freedesktop.dbus.DBusInterface;
public interface Adapter1 extends DBusInterface
{

  public void StartDiscovery();
  public void StopDiscovery();
  public void RemoveDevice(DBusInterface device);

}
