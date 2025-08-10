#include "stdafx.h"
#include "bthapp.h"
#include "bthlib.h"


//#define CXN_INSTANCE_STRING "Bluetooth Server"
char szThisComputerName[MAX_COMPUTERNAME_LENGTH + 1] = "";

UINT_PTR WINAPI BthSocket()
{
    DWORD dwLenComputerName = sizeof(szThisComputerName);
    if (!GetComputerName(szThisComputerName, &dwLenComputerName))
    {
        int error = WSAGetLastError();
        DPRINTF("GetComputerName *ERROR* WSAGetLastError=%d", error);
        return 0;
    }

    SOCKET localSocket = socket(AF_BTH, SOCK_STREAM, BTHPROTO_RFCOMM);
    if (INVALID_SOCKET == localSocket)
    {
        int error = WSAGetLastError();
        DPRINTF("socket *ERROR* WSAGetLastError=%d", error);
        return 0;
    }

    SOCKADDR_BTH SockAddrBthLocal = { 0 };
    SockAddrBthLocal.addressFamily = AF_BTH;
    SockAddrBthLocal.port = BT_PORT_ANY;
    if (SOCKET_ERROR == bind(localSocket,
        (struct sockaddr*)&SockAddrBthLocal, sizeof(SOCKADDR_BTH)))
    {
        int error = WSAGetLastError();
        DPRINTF("bind *ERROR* WSAGetLastError=%d", error);
        BthClose(localSocket);
        return 0;
    }

    int iAddrLen = sizeof(SOCKADDR_BTH);
    ULONG ulRetCode = getsockname(localSocket,
        (struct sockaddr*)&SockAddrBthLocal, &iAddrLen);
    if (SOCKET_ERROR == ulRetCode)
    {
        int error = WSAGetLastError();
        DPRINTF("getsockname *ERROR* WSAGetLastError=%d", error);
        BthClose(localSocket);
        return 0;
    }

    CSADDR_INFO CSAddrInfo = { 0 };
    CSAddrInfo.LocalAddr.iSockaddrLength = sizeof(SOCKADDR_BTH);
    CSAddrInfo.LocalAddr.lpSockaddr = (LPSOCKADDR)&SockAddrBthLocal;
    CSAddrInfo.RemoteAddr.iSockaddrLength = sizeof(SOCKADDR_BTH);
    CSAddrInfo.RemoteAddr.lpSockaddr = (LPSOCKADDR)&SockAddrBthLocal;
    CSAddrInfo.iSocketType = SOCK_STREAM;
    CSAddrInfo.iProtocol = BTHPROTO_RFCOMM;

    WSAQUERYSET wsaQuerySet = { 0 };
    ZeroMemory(&wsaQuerySet, sizeof(WSAQUERYSET));
    wsaQuerySet.dwSize = sizeof(WSAQUERYSET);
    wsaQuerySet.lpServiceClassId = (LPGUID)&ServiceClassGuid;
    wsaQuerySet.lpszServiceInstanceName = szThisComputerName;
    wsaQuerySet.lpszComment = (char*)"Bluetooth Server Service";
    wsaQuerySet.dwNameSpace = NS_BTH;
    wsaQuerySet.dwNumberOfCsAddrs = 1;      // Must be 1.
    wsaQuerySet.lpcsaBuffer = &CSAddrInfo;  // Req'd.

    if (SOCKET_ERROR == WSASetService(&wsaQuerySet, RNRSERVICE_REGISTER, 0))
    {
        int error = WSAGetLastError();
        DPRINTF("WSASetService *ERROR* WSAGetLastError=%d", error);
        BthClose(localSocket);
        return 0;
    }

    if (SOCKET_ERROR == listen(localSocket, CXN_DEFAULT_LISTEN_BACKLOG))
    {
        int error = WSAGetLastError();
        DPRINTF("listen *ERROR* WSAGetLastError %d", error);
        BthClose(localSocket);
        return 0;
    }

    DPRINTF("BthSocket(%s)=%I64d", szThisComputerName, (INT64)localSocket);
    return localSocket;
}

UINT_PTR WINAPI BthAccept(/*SOCKET*/UINT_PTR localSocket)
{
    SOCKET clientSocket = accept(localSocket, NULL, NULL);
    if (INVALID_SOCKET == clientSocket)
    {
        int error = WSAGetLastError();
        DPRINTF("accept *ERROR* WSAGetLastError=%d", error);
        return 0;
    }

    DPRINTF("BthAccept=%I64d", (INT64) clientSocket);
    return clientSocket;
}
