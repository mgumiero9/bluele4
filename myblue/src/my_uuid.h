#ifndef MY_UUID_H_INCLUDED
#define MY_UUID_H_INCLUDED

 /* GATT UUIDs section */
#define GATT_PRIM_SVC_UUID				0x2800
#define GATT_SND_SVC_UUID				0x2801
#define GATT_INCLUDE_UUID				0x2802
#define GATT_CHARAC_UUID				0x2803

/* GATT Characteristic Types */
#define GATT_CHARAC_DEVICE_NAME				0x2A00
#define GATT_CHARAC_APPEARANCE				0x2A01
#define GATT_CHARAC_PERIPHERAL_PRIV_FLAG		0x2A02
#define GATT_CHARAC_RECONNECTION_ADDRESS		0x2A03
#define GATT_CHARAC_PERIPHERAL_PREF_CONN		0x2A04
#define GATT_CHARAC_SERVICE_CHANGED			0x2A05
#define GATT_CHARAC_SYSTEM_ID				0x2A23
#define GATT_CHARAC_MODEL_NUMBER_STRING			0x2A24
#define GATT_CHARAC_SERIAL_NUMBER_STRING		0x2A25
#define GATT_CHARAC_FIRMWARE_REVISION_STRING		0x2A26
#define GATT_CHARAC_HARDWARE_REVISION_STRING		0x2A27
#define GATT_CHARAC_SOFTWARE_REVISION_STRING		0x2A28
#define GATT_CHARAC_MANUFACTURER_NAME_STRING		0x2A29

/* GATT Characteristic Descriptors */
#define GATT_CHARAC_EXT_PROPER_UUID			0x2900
#define GATT_CHARAC_USER_DESC_UUID			0x2901
#define GATT_CLIENT_CHARAC_CFG_UUID			0x2902
#define GATT_SERVER_CHARAC_CFG_UUID			0x2903
#define GATT_CHARAC_FMT_UUID				0x2904
#define GATT_CHARAC_AGREG_FMT_UUID			0x2905
#define GATT_CHARAC_VALID_RANGE_UUID			0x2906
#define GATT_EXTERNAL_REPORT_REFERENCE			0x2907
#define GATT_REPORT_REFERENCE				0x2908

#include "lib/uuid.h"

int bt_uuid_strcmp(const void *a, const void *b);

int bt_uuid16_create(bt_uuid_t *btuuid, uint16_t value);
int bt_uuid32_create(bt_uuid_t *btuuid, uint32_t value);
int bt_uuid128_create(bt_uuid_t *btuuid, uint128_t value);

int bt_uuid_cmp(const bt_uuid_t *uuid1, const bt_uuid_t *uuid2);
void bt_uuid_to_uuid128(const bt_uuid_t *src, bt_uuid_t *dst);

#define MAX_LEN_UUID_STR 37

int bt_uuid_to_string(const bt_uuid_t *uuid, char *str, size_t n);
int bt_string_to_uuid(bt_uuid_t *uuid, const char *string);



static inline int bt_uuid_len(const bt_uuid_t *uuid)
{
	return uuid->type / 8;
}

#endif // MY_UUID_H_INCLUDED
