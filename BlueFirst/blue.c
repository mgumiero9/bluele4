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

int sock;
int err;


void read_list()
{
    uint8_t size;
    int to = 1000;

    err = hci_le_read_white_list_size(sock, &size, to);
    if (err < 0)
    {
        printf("Read White List Error (%d)\n", err);
        return;
    }


}


static volatile int signal_received = 0;

static void sigint_handler(int sig)
{
	signal_received = sig;
}

void eir_parse_name(uint8_t *eir, size_t eir_len,
						char *buf, size_t buf_len)
{
	size_t offset;

	offset = 0;
	while (offset < eir_len) {
		uint8_t field_len = eir[0];
		size_t name_len;

		/* Check for the end of EIR */
		if (field_len == 0)
			break;

		if (offset + field_len > eir_len)
			goto failed;

		switch (eir[1]) {
		case EIR_NAME_SHORT:
		case EIR_NAME_COMPLETE:
			name_len = field_len - 1;
			if (name_len > buf_len)
				goto failed;

			memcpy(buf, &eir[2], name_len);
			return;
		}

		offset += field_len + 1;
		eir += field_len + 1;
	}

failed:
	snprintf(buf, buf_len, "(unknown)");
}


int main(int argc, char **argv)
{
    int max_rsp, num_rsp;
    int dev_id, len, flags;
    int i;
    char addr[19] = { 0 };
    char name[248] = { 0 };
    uint16_t interval = htobs(0x0012);
    uint16_t window = htobs(0x0012);

    //dev_id = hci_get_route(NULL);
    dev_id = hci_devid("00:15:83:6A:1E:8F");

    //hci_close_dev(dev_id);
    sock = hci_open_dev(dev_id);
    if (dev_id < 0 || sock < 0) {
        perror("opening socket");
        exit(1);
    }

/*
    printf("le_set_scan_parameters %d\n",
	hci_le_set_scan_parameters(sock, 0x00, htobs(0x0010),
		htobs(0x0010), 0x01, 0x00, 1000));
*/
    printf("le_set_scan_parameters %d\n",
	hci_le_set_scan_parameters(sock, 0x01, interval, window, 0x00, 0x00, 1000));
    printf("le_set_scan %d\n",
	hci_le_set_scan_enable(sock, 0x01, 0x01, 1000));

    printf("\nLE SCAN\n");


	unsigned char buf[HCI_MAX_EVENT_SIZE], *ptr;
	struct hci_filter nf, of;
	struct sigaction sa;
	socklen_t olen;
	//int len;

    olen = sizeof(of);
    if (getsockopt(sock, SOL_HCI, HCI_FILTER, &of, &olen) < 0) {
        printf("Could not get socket options\n");
        return -1;
    }
	hci_filter_clear(&nf);
	hci_filter_set_ptype(HCI_EVENT_PKT, &nf);
	hci_filter_set_event(EVT_LE_META_EVENT, &nf);

	if (setsockopt(sock, SOL_HCI, HCI_FILTER, &nf, sizeof(nf)) < 0) {
		printf("Could not set socket options\n");
		return -1;
	}

	memset(&sa, 0, sizeof(sa));
	sa.sa_flags = SA_NOCLDSTOP;
	sa.sa_handler = sigint_handler;
	sigaction(SIGINT, &sa, NULL);
    int x = 5, found = 0;
    le_advertising_info *info;
    le_advertising_info a;


	while (x--) {
		evt_le_meta_event *meta;
		char addr[18];

		while ((len = read(sock, buf, sizeof(buf))) < 0) {
			if (signal_received == SIGINT) {
				len = 0;
				goto done;
			}

			goto done;
		}

		ptr = buf + (1 + HCI_EVENT_HDR_SIZE);
		len -= (1 + HCI_EVENT_HDR_SIZE);

		meta = (void *) ptr;

		if (meta->subevent != 0x02)
			goto done;

		// Ignoring multiple reports
		info = (le_advertising_info *) (meta->data + 1);
		if (info->evt_type == 0) {
			char name[30];

			memset(name, 0, sizeof(name));
            found = 1;
			ba2str(&info->bdaddr, addr);
			eir_parse_name(info->data, info->length, name, sizeof(name) - 1);
			printf("EvtType %d \t %02X %02X\n", info->evt_type, info->data[0], info->data[1]);
			printf("%s %s %d\n", addr, name, info->bdaddr_type);
		}
	}

done:
	printf("Disable Le %d\n", hci_le_set_scan_enable(sock, 0x00, 0x00, 1000));
	//setsockopt(sock, SOL_HCI, HCI_FILTER, &of, sizeof(of));

	//own_bdaddr_type = 0x00;
    //info = &a;
    interval = htobs(0x0060);
	window = htobs(0x0030);

    //The connection handle is invalid. The valid range is 0x0000..0x0eff
    //The maximum interval is invalid. The valid range is 0x0002..0xfffe
    //The minimum interval is invalid. The valid range is 0x0002..maxinterval.

	uint16_t min_interval = htobs(0x0028);
	uint16_t max_interval = htobs(0x0038);
	uint16_t latency = htobs(0x0000);
	uint16_t supervision_timeout = htobs(0x002A);
	uint16_t min_ce_length = htobs(0x0000);
	uint16_t max_ce_length = htobs(0x0000);
    uint8_t initiator_filter = FLT_CLEAR_ALL;       // FLT_INQ_RESULT or FLT_CONN_SETUP
    uint8_t own_bdaddr_type = LE_PUBLIC_ADDRESS;    // or LE_RANDOM_ADDRESS
    uint16_t handle = 0;

    //info->bdaddr.b[0] = 0x00;
    //info->bdaddr.b[1] = 0x18;
    //info->bdaddr.b[2] = 0x8C;
    //info->bdaddr.b[3] = 0x32;
    //info->bdaddr.b[4] = 0x34;
    //info->bdaddr.b[5] = 0x20;
    //info->bdaddr_type = 0;

    if(1)
    {
        int err = hci_le_create_conn(sock, interval, window, initiator_filter, info->bdaddr_type, info->bdaddr, own_bdaddr_type, min_interval,
                                      	max_interval, latency, supervision_timeout, min_ce_length, max_ce_length, &handle, 25000);
        printf("Conn %d\n", err);
        printf("Connection handle %d\n", handle);
        if(handle > 0)
        {
            struct hci_version ver;
            err = hci_read_remote_version(sock, handle, &ver, 2500);
            if(err >= 0)
            {
                printf("Ver: %04X %04X %04X %04X %04X\n", ver.hci_rev, ver.hci_ver, ver.lmp_subver, ver.lmp_ver, ver.manufacturer);
            }
            else
                printf("Version Error\n");
            uint8_t features[100];
            err = hci_read_remote_features(sock, handle, features, 2500);
            if(err >= 0)
            {
                printf("Features: %04X\n", features[0]);
            }
            else
                printf("Rem Features Error\n");
            uint8_t page = 1, max_page[100];
            err = hci_read_remote_ext_features(sock, handle, page, max_page, features, 2500);
            if(err >= 0)
            {
                printf("Ex Features: %04X %04X %04X\n", page, max_page, features);
            }
            else
                printf("Ext Features Error\n");



        }

    }


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

	hci_close_dev(sock);

    return 0;
}
