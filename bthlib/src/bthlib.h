#ifndef __BTHLIB_H__
#define __BTHLIB_H__

#define _WINSOCK_DEPRECATED_NO_WARNINGS

#include <winsock2.h>
#include <ws2bth.h>

extern UINT_PTR WINAPI BthConnect(LPCSTR RemoteName);
extern UINT_PTR WINAPI BthAccept(/*SOCKET*/UINT_PTR socket);
extern int WINAPI BthSend(/*SOCKET*/UINT_PTR socket, LPCSTR pszDataBuffer, int length);
extern int WINAPI BthRecv(/*SOCKET*/UINT_PTR socket, LPSTR pszDataBuffer, int pos, int length);
extern int WINAPI BthClose(/*SOCKET*/UINT_PTR socket);

extern void DPRINTF(LPCSTR data, ...);
extern int (WINAPI* BthNotify)(LPCSTR message);
extern UINT_PTR WINAPI BthSocket();
extern void WINAPI BthCallback(int (WINAPI* notify)(LPCSTR message));

#endif
