#include "stdafx.h"
#include "bthlib.h"
#include "bthapp.h"

#define NTSTATUS LONG
#include <tlhelp32.h>
#include <netioapi.h>

#pragma comment(lib, "iphlpapi.lib")
#pragma comment(lib, "ws2_32.lib")

// {43DE0A91-A50A-46F4-B26B-AA3A37943C35}
GUID ServiceClassGuid = { 0x43de0a91, 0xa50a, 0x46f4, { 0xb2, 0x6b, 0xaa, 0x3a, 0x37, 0x94, 0x3c, 0x35 } };

WSADATA WSAData = { 0 };
static ULONG BthStartup();
static ULONG BthCleanup();

static HMODULE hModule;
static char szIniPath[MAX_PATH] = "";
void WINAPI DefProfile(LPCSTR name)
{
    GetModuleFileName(hModule, szIniPath, sizeof(szIniPath));
    char* p = szIniPath + lstrlen(szIniPath) - 4;
    lstrcpy(p, ".");
    lstrcat(p, name);
    while (*p != 0)
    {
        *p = tolower(*p);
        p = CharNext(p);
    }
    lstrcat(p, ".ini");
}
int WINAPI GetProfile(LPCSTR sec, LPCSTR key, LPSTR buf, int leng)
{
    GetPrivateProfileString(sec, key, "", buf, leng, szIniPath);
    return lstrlen(buf);
}
int WINAPI SetProfile(LPCSTR sec, LPCSTR key, LPCSTR buf)
{
    WritePrivateProfileString(sec, key, buf, szIniPath);
    return lstrlen(buf);
}

BOOL APIENTRY DllMain( HMODULE hInstance,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
                     )
{
    switch (ul_reason_for_call)
    {
    case DLL_PROCESS_ATTACH:
        hModule = hInstance;
        BthStartup();
        break;
	case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
        break;
    case DLL_PROCESS_DETACH:
        BthCleanup();
        break;
    }
    return TRUE;
}

ULONG BthStartup()
{
    ULONG ulRetCode = 0;
    if (WSAData.wVersion == 0)
    {
        DPRINTF("WSAStartup()\n");
        ulRetCode = WSAStartup(MAKEWORD(2, 2), &WSAData);
        if (CXN_SUCCESS != ulRetCode)
        {
            DPRINTF("*ERROR* Unable to initialize Winsock version 2.2");
            return 1;
        }
    }
    return 0;
}

ULONG BthCleanup()
{
    if (WSAData.wVersion != 0)
    {
        WSACleanup();
        memset(&WSAData, 0, sizeof(WSAData));
    }
    return 0;
}

int (WINAPI* BthNotify)(LPCSTR message) = NULL;
void WINAPI BthCallback(int (WINAPI* notify)(LPCSTR message))
{
    BthNotify = notify;
}
void DPRINTF(LPCSTR data, ...) {
    char message[512] = "";
    va_list args;
    va_start(args, data);
    vsprintf(message, data, args);
    va_end(args);
    if(BthNotify != NULL)
        BthNotify(message);
}
