#ifndef BTDEVICE_H
#define BTDEVICE_H

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include <signal.h>

#define EIR_FLAGS                   0x01  /* flags */
#define EIR_UUID16_SOME             0x02  /* 16-bit UUID, more available */
#define EIR_UUID16_ALL              0x03  /* 16-bit UUID, all listed */
#define EIR_UUID32_SOME             0x04  /* 32-bit UUID, more available */
#define EIR_UUID32_ALL              0x05  /* 32-bit UUID, all listed */
#define EIR_UUID128_SOME            0x06  /* 128-bit UUID, more available */
#define EIR_UUID128_ALL             0x07  /* 128-bit UUID, all listed */
#define EIR_NAME_SHORT              0x08  /* shortened local name */
#define EIR_NAME_COMPLETE           0x09  /* complete local name */
#define EIR_TX_POWER                0x0A  /* transmit power level */
#define EIR_DEVICE_ID               0x10  /* device ID */


class btDevice
{
    public:
        btDevice();
        virtual ~btDevice();
        void init(void);
        int findDevices(void);
        int getDevicesFound();
        void openDevice(int index);
    protected:
        bdaddr_t InfoDevice[255];
        uint8_t InfoLen;
    private:
        bool opened;

        int DevSock;
        void readWhiteList(void);

        void eir_parse_name(uint8_t *eir, size_t eir_len, char *buf, size_t buf_len);
};

#endif // BTDEVICE_H
