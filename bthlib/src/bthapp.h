#ifndef __BTHAPP_H__
#define __BTHAPP_H__

#define CXN_BDADDR_STR_LEN                17   // 6 two-digit hex values plus 5 colons
#define CXN_MAX_INQUIRY_RETRY             3
#define CXN_DELAY_NEXT_INQUIRY            15
#define CXN_SUCCESS                       0
#define CXN_ERROR                         1
#define CXN_DEFAULT_LISTEN_BACKLOG        4

extern GUID ServiceClassGuid;

extern void WINAPI DefProfile(LPCSTR name);
extern int WINAPI GetProfile(LPCSTR sec, LPCSTR key, LPSTR buf, int leng);
extern int WINAPI SetProfile(LPCSTR sec, LPCSTR key, LPCSTR buf);

#endif
