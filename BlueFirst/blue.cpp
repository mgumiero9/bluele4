


#include "btDevice.h"
#include <stdio.h>

int err;



int main(int argc, char **argv)
{
    int len = 0;

    btDevice bt;

    bt.init();

    printf("Find %d devices\n", len = bt.findDevices());

    for(int i=0; i<bt.getDevicesFound(); i++)
    {
        bt.openDevice(i);
    }
    printf("Finishing\n");


/*    num_rsp = hci_inquiry(dev_id, len, max_rsp, NULL, &ii, flags);
    if( num_rsp < 0 ) perror("hci_inquiry");

    for (i = 0; i < num_rsp; i++) {
        ba2str(&(ii+i)->bdaddr, addr);
        memset(name, 0, sizeof(name));
        if (hci_read_remote_name(sock, &(ii+i)->bdaddr, sizeof(name),
            name, 0) < 0)
        strcpy(name, "[unknown]");
        printf("%s  %s\n", addr, name);
    }
*/


    return 0;
}
