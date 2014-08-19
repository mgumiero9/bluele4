#include "btDevice.h"
#include <time.h>
static volatile int signal_received = 0;

static void sigint_handler(int sig)
{
	signal_received = sig;
}


btDevice::btDevice()
{
    opened = false;
}

btDevice::~btDevice()
{
    if (opened) {
        hci_close_dev(DevSock);
    }
    //dtor
}

void btDevice::init()
{
    int dev_id;
    // Em sistemas moveis não haverá dois adaptadores e o comando
    // abaixo atenderá
    // dev_id = hci_get_route(NULL);
    // Em PCs, quando o adaptador interno não atender será necessário
    // outro adaptador resultando em mais de uma adaptador ativo.
    // Neste caso é necessário indicador o adapatador ativo ou
    // listar (ver for_each_dev em hci.c).
    dev_id = hci_devid("00:15:83:6A:1E:8F");

    if(dev_id <= 0) {
        perror("bluetooth device not found");
        return;
    }

    DevSock = hci_open_dev(dev_id);
    if (DevSock < 0) {
        perror("opening socket error");
        return;
    }
    opened = true;
}

int btDevice::findDevices()
{
    int err;

    int max_rsp, num_rsp;
    int len, flags;
    char addr[19] = { 0 };
    char name[248] = { 0 };
    uint16_t interval = htobs(0x0012);
    uint16_t window = htobs(0x0012);
    /*
    printf("le_set_scan_parameters %d\n",
	hci_le_set_scan_parameters(sock, 0x00, htobs(0x0010),
		htobs(0x0010), 0x01, 0x00, 1000));
*/
    if(!opened) {
        perror("ScanLE: device not opened");
        return 0;
    }

    err = hci_le_set_scan_parameters(DevSock, 0x01, interval, window, 0x00, 0x00, 1000);
    if(err)
    {
        printf("le_set_scan_parameters %d\n", err);
        //return 0;
    }

    err = hci_le_set_scan_enable(DevSock, 0x01, 0x01, 1000);
    if(err)
    {
        printf("le_set_scan %d\n", err);
        return 0;
    }

    printf("\nLE SCAN\n");


	unsigned char buf[HCI_MAX_EVENT_SIZE], *ptr;
	struct hci_filter nf, of;
	struct sigaction sa;
	socklen_t olen;
	//int len;

    olen = sizeof(of);
    if (getsockopt(DevSock, SOL_HCI, HCI_FILTER, &of, &olen) < 0) {
        printf("Could not get socket options\n");
        return -1;
    }
	hci_filter_clear(&nf);
	hci_filter_set_ptype(HCI_EVENT_PKT, &nf);
	hci_filter_set_event(EVT_LE_META_EVENT, &nf);

	if (setsockopt(DevSock, SOL_HCI, HCI_FILTER, &nf, sizeof(nf)) < 0) {
		printf("Could not set socket options\n");
		return -1;
	}

	memset(&sa, 0, sizeof(sa));
	sa.sa_flags = SA_NOCLDSTOP;
	sa.sa_handler = sigint_handler;
	sigaction(SIGINT, &sa, NULL);
    le_advertising_info *info;
    le_advertising_info a;

    time_t end_time = time(NULL) + 30;
    InfoLen = 0;

	while (end_time - time(NULL) > 0) {

		evt_le_meta_event *meta;
		char addr[18];
		int j, f_ok = 1, found;

		while (( end_time - time(NULL) > 0) && ((len = read(DevSock, buf, sizeof(buf))) < 0)) {
			if (signal_received == SIGINT) {
				len = 0;
				f_ok = 0;
				break;
			}
			f_ok = 0;
			break;
		}

		ptr = buf + (1 + HCI_EVENT_HDR_SIZE);
		// len -= (1 + HCI_EVENT_HDR_SIZE);
        if(f_ok) {
            printf("\n (%03d) (%d) <<<", len, end_time - time(NULL));
            for(j=0; j<len; j++)
            {
                printf(" %02X", buf[j]);
            }
            printf("\n");

            meta = (evt_le_meta_event*) ptr;

            if (meta->subevent != 0x02)
                break;

            // Ignoring multiple reports
            info = (le_advertising_info *) (meta->data + 1);
            if (info->evt_type == 0) {
                char name[30];

                memset(name, 0, sizeof(name));
                ba2str(&info->bdaddr, addr);
                eir_parse_name(info->data, info->length, name, sizeof(name) - 1);
                printf("EvtType %d \t %02X %02X\n", info->evt_type, info->data[0], info->data[1]);
                printf("%s %s %d\n", addr, name, info->bdaddr_type);
                found = 0;
                for(int i=0; i<InfoLen; i++)
                {
                    printf("Resultado Comparação %d\n", bacmp(&InfoDevice[i], &info->bdaddr));
                    if (bacmp(&InfoDevice[i], &info->bdaddr) == 0)
                    {
                        found = 1;
                        break;
                    }
                }
                if(!found)
                {
                    bacpy(&InfoDevice[InfoLen++], &info->bdaddr);
                    printf("New Device %s (Index %d)\n", name, InfoLen);
                }

            }
        }
        else {
            break;
        }
	}
    printf("Disable Le %d\n", hci_le_set_scan_enable(DevSock, 0x00, 0x00, 1000));
	setsockopt(DevSock, SOL_HCI, HCI_FILTER, &of, sizeof(of));
	return InfoLen;

}

void btDevice::readWhiteList()
{
    uint8_t size;
    int to = 1000, err;

    err = hci_le_read_white_list_size(DevSock, &size, to);
    if (err < 0)
    {
        printf("Read White List Error (%d)\n", err);
        return;
    }


}

void btDevice::openDevice(int index)
{
	// own_bdaddr_type = 0x00;
    // info = &a;
    uint16_t interval = htobs(0x0060);
	uint16_t window = htobs(0x0030);

    // The connection handle is invalid. The valid range is 0x0000..0x0eff
    // The maximum interval is invalid. The valid range is 0x0002..0xfffe
    // The minimum interval is invalid. The valid range is 0x0002..maxinterval.

	uint16_t min_interval = htobs(0x0028);
	uint16_t max_interval = htobs(0x0038);
	uint16_t latency = htobs(0x0000);
	uint16_t supervision_timeout = htobs(0x002A);
	uint16_t min_ce_length = htobs(0x0000);
	uint16_t max_ce_length = htobs(0x0000);
    uint8_t initiator_filter = FLT_CLEAR_ALL;       // FLT_INQ_RESULT or FLT_CONN_SETUP
    uint8_t own_bdaddr_type = LE_PUBLIC_ADDRESS;    // or LE_RANDOM_ADDRESS

    uint16_t handle = 0;
    int err;

    //info->bdaddr.b[0] = 0x00;
    //info->bdaddr.b[1] = 0x18;
    //info->bdaddr.b[2] = 0x8C;
    //info->bdaddr.b[3] = 0x32;
    //info->bdaddr.b[4] = 0x34;
    //info->bdaddr.b[5] = 0x20;
    //info->bdaddr_type = 0;

    err = hci_le_create_conn(DevSock, interval, window, initiator_filter, own_bdaddr_type, InfoDevice[index], own_bdaddr_type, min_interval,
                                    max_interval, latency, supervision_timeout, min_ce_length, max_ce_length, &handle, 25000);
    printf("Conn %d\n", err);
    printf("Connection handle %d\n", handle);
    if(handle > 0)
    {
        struct hci_version ver;
        err = hci_read_remote_version(DevSock, handle, &ver, 2500);
        if(err >= 0)
        {
            printf("Rev:%04X HciVer:%04X LmpSubVer:%04X LmpVer:%04X Man:%04X\n",
                   ver.hci_rev, ver.hci_ver, ver.lmp_subver, ver.lmp_ver, ver.manufacturer);
        }
        else
            printf("Version Error\n");
        uint8_t features[100];
        err = hci_read_remote_features(DevSock, handle, features, 2500);
        if(err >= 0)
        {
            printf("Features: %04X\n", features[0]);
        }
        else
            printf("Rem Features Error\n");
        uint8_t page = 1, max_page[100];
        err = hci_read_remote_ext_features(DevSock, handle, page, max_page, features, 2500);
        if(err >= 0)
        {
            printf("Ex Features: %04X %04X %04X\n", page, max_page, features);
        }
        else
            printf("Ext Features Error\n");

    }

}

void btDevice::eir_parse_name(uint8_t *eir, size_t eir_len,
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


int btDevice::getDevicesFound()
{
    return InfoLen;
}
