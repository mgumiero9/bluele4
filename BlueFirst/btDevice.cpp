#include "btDevice.h"
#include <time.h>
#include <errno.h>

static volatile int signal_received = 0;

LOCAL_DATA* btDevice::First = NULL;
LOCAL_DATA* btDevice::Last = NULL;
int btDevice::DevSock = NULL;
bool btDevice::loop = true;

BTSTATE btDevice::state = BTSTATE_CLOSED;

void *btDevice::pMain(void *arg)
{
    int len, f_ok = 0;
    time_t reg_time = time(NULL);
    LOCAL_DATA *ptr;
    uint8_t buf[256];

    while(loop)
    {
        if (DevSock != NULL)
        {
            if ((len = read(DevSock, buf, sizeof(buf))) < 0) {
                if((errno == EAGAIN))
                {
                    printf("Trying again (%d)\n", time(NULL));
                    if (!loop)
                        break;
                    continue;

                }
                printf("Received Error (%d %d)\n", len, errno);
                printf("%s\n", strerror(errno));
                f_ok = 0;
                continue;
            }
            if(len > 0)
            {
                printf("New Packet\n");
                ptr = (LOCAL_DATA*) malloc(sizeof(LOCAL_DATA));
                ptr->buf = (uint8_t*) malloc(len);
                memcpy(ptr->buf, buf, len);
                ptr->len = len;
                ptr->next = NULL;

                if(Last != NULL)
                {
                    Last->next = ptr;
                }
                else
                {
                    Last = ptr;
                }
                if (NULL == First)
                    First = Last;
            }
        }
    }

    // Cleaning memory
    printf("Freeing Memory\n");
    ptr = First;
    while (NULL != ptr)
    {
        free(ptr->buf);
        First = (LOCAL_DATA *) ptr->next;
        free(ptr);
        ptr = First;
    }
    loop = false;
}

static void sigint_handler(int sig)
{
	signal_received = sig;
}

btDevice::btDevice()
{
    InfoLen = 0;
    opened = false;
    thMain = NULL;
}

btDevice::~btDevice()
{
    if (opened) {
        hci_close_dev(DevSock);
        opened = false;
    }
    loop = false;
    if(thMain != NULL)
    {
        pthread_join(thMain, NULL);
    }
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

    //DevFlags = fcntl(DevSock ,F_GETFL, 0);
    //fcntl(DevSock, F_SETFL, DevFlags | O_NONBLOCK);

    timeval timeout;
    timeout.tv_sec = 2;
    timeout.tv_usec = 0;

	if (setsockopt(DevSock, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout)) < 0) {
		printf("Could not set socket timeout\n");
		return;
	}

    pthread_create(&thMain, NULL, &btDevice::pMain, NULL);
    printf("Thread started.\n");
    opened = true;
}

int btDevice::findDevices()
{
    int err;

    int max_rsp, num_rsp;
    int len, flags;
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
        //return 0;
    }

    printf("\nLE SCAN\n");


	unsigned char buf[HCI_MAX_EVENT_SIZE];
	struct hci_filter nf, of;
	struct sigaction sa;
	socklen_t olen;
	//int len;

    olen = sizeof(of);
    if (getsockopt(DevSock, SOL_HCI, HCI_FILTER, &of, &olen) < 0) {
    //if (getsockopt(DevSock, SOL_SOCKET, HCI_FILTER, &of, &olen) < 0) {

        printf("Could not get socket options\n");
        return -1;
    }
    printf("OPT %08X %08X %08X %04X\n", of.type_mask, of.event_mask[0], of.event_mask[1], of.opcode);


	hci_filter_clear(&nf);
    hci_filter_all_ptypes(&nf);
	hci_filter_all_events(&nf);

//	hci_filter_set_ptype(HCI_EVENT_PKT, &nf);
//	hci_filter_set_event(EVT_LE_META_EVENT, &nf);

	if (setsockopt(DevSock, SOL_HCI, HCI_FILTER, &nf, sizeof(nf)) < 0) {
		printf("Could not set socket options\n");
		return -1;
	}
/*
	memset(&sa, 0, sizeof(sa));
	sa.sa_flags = SA_NOCLDSTOP;
	sa.sa_handler = sigint_handler;
	sigaction(SIGINT, &sa, NULL);
    le_advertising_info *info;
    le_advertising_info a;
*/
    time_t end_time = time(NULL) + 30;
    InfoLen = 0;

    printf("End Time %d\n", end_time);

    int j, f_ok = 1, found;

	while ((end_time - time(NULL) > 0) && (!InfoLen)) {
        if (signal_received == SIGINT) {
            printf("Received SIGINT\n");
            break;
        }
        processPacket();
	}
    printf("Disable Le %d\n", hci_le_set_scan_enable(DevSock, 0x00, 0x00, 1000));
//	setsockopt(DevSock, SOL_HCI, HCI_FILTER, &of, sizeof(of));
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

    err = hci_le_create_conn(DevSock, interval, window, initiator_filter, own_bdaddr_type, InfoDevice[index], own_bdaddr_type, min_interval,
                                    max_interval, latency, supervision_timeout, min_ce_length, max_ce_length, &handle, 25000);
    printf("Conn %d\n", err);

    printf("Connection handle %d\n", handle);

    time_t end_time = time(NULL) + 60;
    InfoLen = 0;
    ssize_t len = 0;

    printf("Starting...\n");
    char buf[255];
    while(end_time > time(NULL))
    {
        if (signal_received == SIGINT) {
            printf("Received SIGINT\n");
            break;
        }
        processPacket();
/*        len = recv(DevSock, buf, sizeof(buf), MSG_DONTWAIT);
        if (len > 0)
        {
            printf("RECEIVED :");
            for(int i=0; i<len; i++)
                printf("%02X ", buf[i]);
            printf("\n");
        }
        else
            printf("Code %d\n",  len);
            */


    }



    /*
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
    */
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


void btDevice::connectDevice(int index)
{
	bdaddr_t sba, dba;
	uint8_t dest_type;
//	GError *tmp_err = NULL;
//	BtIOSecLevel sec;

//	str2ba(dst, &dba);

	/* Local adapter */
    sba.b[5] = 0x00;
    sba.b[4] = 0x15;
    sba.b[3] = 0x83;
    sba.b[2] = 0x6A;
    sba.b[1] = 0x1E;
    sba.b[0] = 0x8F;
    // OR bacpy(&sba, BDADDR_ANY);

	/* Not used for BR/EDR */
	//if (strcmp(dst_type, "random") == 0)
	//	dest_type = BDADDR_LE_RANDOM;
	//else
//		dest_type = BDADDR_LE_PUBLIC;

	//if (strcmp(sec_level, "medium") == 0)
	//	sec = BT_IO_SEC_MEDIUM;
	//else if (strcmp(sec_level, "high") == 0)
	//	sec = BT_IO_SEC_HIGH;
	//else
//		sec = BT_IO_SEC_LOW;
/*
	if (psm == 0)
		chan = bt_io_connect(connect_cb, NULL, NULL, &tmp_err,
				BT_IO_OPT_SOURCE_BDADDR, &sba,
				BT_IO_OPT_SOURCE_TYPE, BDADDR_LE_PUBLIC,
				BT_IO_OPT_DEST_BDADDR, &dba,
				BT_IO_OPT_DEST_TYPE, dest_type,
				BT_IO_OPT_CID, ATT_CID,
				BT_IO_OPT_SEC_LEVEL, sec,
				BT_IO_OPT_INVALID);
	else
		chan = bt_io_connect(connect_cb, NULL, NULL, &tmp_err,
				BT_IO_OPT_SOURCE_BDADDR, &sba,
				BT_IO_OPT_DEST_BDADDR, &dba,
				BT_IO_OPT_PSM, psm,
				BT_IO_OPT_IMTU, mtu,
				BT_IO_OPT_SEC_LEVEL, sec,
				BT_IO_OPT_INVALID);

	if (tmp_err) {
		g_propagate_error(gerr, tmp_err);
		return NULL;
	}

*/
}


void btDevice::readPacket(uint8_t *buf, uint16_t len)
{
    if(len > 0)
    {
        switch(buf[0])
        {
            case HCI_COMMAND_PKT:
                printf("Received Command Packet\n");

            break;
            case HCI_ACLDATA_PKT:
                printf("Received ACL Packet\n");
            break;
            case HCI_SCODATA_PKT:
                printf("Received SCO Packet\n");
            break;
            case HCI_EVENT_PKT:
                printf("Event Packet \n");
                if ((len < 3) || ((len - HCI_EVENT_HDR_SIZE - 1) != buf[HCI_EVENT_HDR_SIZE]))
                {
                    printf("Packet Size Wrong\n");
                }
                readEventPacket(buf[1], &buf[3], len - HCI_EVENT_HDR_SIZE - 1);
            break;
            case HCI_VENDOR_PKT:
                printf("Received Vendor Packet\n");
            break;
        }
    }
}

int btDevice::readEventPacket(uint8_t event, uint8_t *buf, uint16_t len)
{
    int i, j, err;
    uint8_t key= 0x11;
    if(len == 0)
        return 0;

    switch(event)
    {
    case EVT_LE_META_EVENT:
        printf("Meta Event\n");
        i = 0;
        while(i < len)
        {
            i = i + readEventPacket(buf[i], &buf[i+1], len - i - 1) + 1;
        }
        return i;
        break;
    case EVT_LE_CONN_COMPLETE:
        if(len >= LE_CREATE_CONN_CP_SIZE)
        {
            printf("Conn Complete Event\n");
            readConnComplete((evt_le_connection_complete*) &buf[1]);
            return LE_CREATE_CONN_CP_SIZE;
        }
        break;
    case EVT_LE_ADVERTISING_REPORT:
        if ((len > 0) && (len - 1 >= LE_ADVERTISING_INFO_SIZE * buf[0]))
        {
            printf("Report Qty %d\n", buf[0]);
            printf("Advertizing\n");
            i = 1;
            for(j=0; j<buf[0] && i < len; j++)
            {
                i =i + readAdvertisingEvent((le_advertising_info*) &buf[i], len - i);
            }
            return i;
        }
        else {
            printf("Invalid Advertizing Size\n");
        }
        break;
    case EVT_LE_CONN_UPDATE_COMPLETE:
        //evt_le_connection_update_complete *data = ;
        break;
        case EVT_LE_CONN_UPDATE_COMPLETE_SIZE:
    case EVT_LE_READ_REMOTE_USED_FEATURES_COMPLETE:
        //evt_le_read_remote_used_features_complete *data = ;
        if (len >= EVT_LE_READ_REMOTE_USED_FEATURES_COMPLETE_SIZE)
        {

        }
        printf("Read Remote Features\n");
        return EVT_LE_READ_REMOTE_USED_FEATURES_COMPLETE_SIZE;
        break;
    case EVT_LE_LTK_REQUEST:
        //if(len >= EVT_LE_LTK_REQUEST_SIZE)
        {

            if (InfoLen > 0)
            {
                err = hci_write_stored_link_key(DevSock, &InfoDevice[0], &key, 5000);
                printf("Request Key Response = %d\n", err);
            }
            else
                printf("Request Key Response No Connection\n");
            return EVT_LE_LTK_REQUEST_SIZE;
        }

        break;
    case EVT_CMD_COMPLETE:
        printf("Command Complete\n");
        if(len >= EVT_CMD_COMPLETE_SIZE + 1)
        {
            return readCommandComplete((evt_cmd_complete*) buf, buf[3]);
        }
    default:
        printf("Unknown Event (%02X)\n", event);
        return len;
        break;
    }
    printf("Size Erro\n");
    return len;
}

void btDevice::readConnComplete(evt_le_connection_complete* data)
{

}

int btDevice::readAvaiableData(int index)
{
    uint32_t x = 0x003F;
    int r = hci_send_cmd(DevSock, OGF_HOST_CTL, 0x0001, 4, &x);
    printf("Response HCI: %d\n", r);

    return 0;

}

void btDevice::processPacket()
{
    LOCAL_DATA *ptr;
    if(NULL != First)
    {
        printf("\n\nReading Packet (%d) :", First->len);
        for(int i=0; i<First->len; i++)
            printf(" %02X", First->buf[i]);
        printf("\n");
        readPacket(First->buf, First->len);
        ptr = First;
        if (First == Last)
            Last = NULL;
        First = (LOCAL_DATA*)ptr->next;
        ptr->next = NULL;
        free(ptr->buf);
        free(ptr);
    }
    else
    {
        if (Last != NULL)
            printf("No Packets\n");
    }

}

int btDevice::readCommandComplete(evt_cmd_complete* info, uint8_t result)
{
    printf("Command %02X %04X Result:%02X\n", info->ncmd, info->opcode, result);
    return 4;
}


int btDevice::readAdvertisingEvent(le_advertising_info *info, int len)
{
    char addr[19] = { 0 };
    char name[248] = { 0 };
    bool found = 0;
    int i;

    memset(name, 0, sizeof(name));
    ba2str(&info->bdaddr, addr);
    eir_parse_name(info->data, info->length, name, sizeof(name) - 1);
    printf("EvtType %d \t %02X %02X ", info->evt_type, info->data[0], info->data[1]);
    printf("%s %s %d\n", addr, name, info->bdaddr_type);
    found = 0;
    for(int i=0; i<InfoLen; i++)
    {
        if (bacmp(&InfoDevice[i], &info->bdaddr) == 0)
        {
            found = 1;
            break;
        }
    }
    if(!found)
    {
        bacpy(&InfoDevice[InfoLen++], &info->bdaddr);
        printf("New Device %s  %02X:%02X:%02X:%02X:%02X:%02X\n", name, info->bdaddr.b[0], info->bdaddr.b[1],
               info->bdaddr.b[2], info->bdaddr.b[3], info->bdaddr.b[4], info->bdaddr.b[5]);
    }
    if(info->length > len - sizeof(le_advertising_info) + 1)
    {
        printf("Len Size Error Types\n");
        return len;
    }

    int size = 0;
    i = 0;

    printf("Parameter Size %d\n", info->length);
    while(i < info->length)
    {
        size = info->data[i++];
        printf("Size %02X Item %02X: ", size, info->data[i]);
        if (size + i > info->length)
        {
            printf("Size Error");
            return len;
        }
        switch(info->data[i])
        {
        case 0x01:
            printf("Flags");
            break;
        case 0x02:
            printf("Incomplete List of 16-bit Service Class UUIDs ");
            break;
        case 0x03:
            printf("Complete List of 16-bit Service Class UUIDs ");
            break;
        case 0x04:
            printf("Incomplete List of 32-bit Service Class UUIDs ");
            break;
        case 0x05:
            printf("Complete List of 32-bit Service Class UUIDs ");
            break;
        case 0x06:
            printf("Incomplete List of 128-bit Service Class UUIDs ");
            break;
        case 0x07:
            printf("Complete List of 128-bit Service Class UUIDs ");
            break;
        case 0x08:
            printf("Shortened Local Name ");
            break;
        case 0x09:
            printf("Complete Local Name ");
            break;
        case 0x0A:
            printf("Tx Power Level ");
            break;
        case 0x0D:
            printf("Class of Device ");
            break;
        case 0x0E:
            printf("Simple Pairing Hash C ");
            break;
        case 0x0F:
            printf("Simple Pairing Randomizer R ");
            break;
        case 0x10:
            printf("Device ID ");
            break;
        case 0x11:
            printf("Security Manager Out of Band Flags ");
            break;
        case 0x12:
            printf("Slave Connection Interval Range ");
            break;
        case 0x14:
            printf("List of 16-bit Service Solicitation UUIDs ");
            break;
        case 0x15:
            printf("List of 128-bit Service Solicitation UUIDs ");
            break;
        case 0x16:
            printf("Service Data - 16-bit ");
            break;
        case 0x20:
            printf("Service Data - 32-bit UUID ");
            break;
        case 0x21:
            printf("Service Data - 128-bit UUID ");
            break;
        case 0x17:
            printf("Public Target Address ");
            break;
        case 0x18:
            printf("Random Target Address ");
            break;
        case 0x19:
            printf("Appearance ");
            break;
        case 0x1A:
            printf("Advertising Interval ");
            break;
        case 0x1B:
            printf("​LE Bluetooth Device ");
            break;
        case 0x1C:
            printf("​LE Role ");
            break;
        case 0x1D:
            printf("​Simple Pairing Hash C-256 ");
            break;
        case 0x1E:
            printf("​Simple Pairing Randomizer R-256 ");
            break;
        case 0x3D:
            printf("3D Information Data ");
            break;
        case 0xFF:
            printf("Manufacturer Specific Data ");
            break;
        default:
            printf("Erro");
            break;
        }
        for(int j=1; j < size; j++)
            printf(" %02X ", info->data[i+j] );
        printf("\t");
        for(int j=0; j < size - 1; j++)
            printf("%c", info->data[++i]);
        i++;
        printf("\n");

    }
    return (sizeof(le_advertising_info) - 1 + info->length);
}
