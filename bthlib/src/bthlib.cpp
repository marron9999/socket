#include "stdafx.h"
#include "bthapp.h"
#include "bthlib.h"

int WINAPI BthSend(/*SOCKET*/UINT_PTR socket, LPCSTR pszDataBuffer, int length)
{
    int rlen = length;
    while (rlen > 0) {
        int len = send(socket, pszDataBuffer, rlen, 0);
        if (SOCKET_ERROR == len)
        {
            int error = WSAGetLastError();
            DPRINTF("send *ERROR* WSAGetLastError %d", error);
            return -1;
        }
        rlen -= len;
        pszDataBuffer += len;
    }
    //DPRINTF("send %d bytes", length);
    return length;
}

int WINAPI BthRecv(/*SOCKET*/UINT_PTR socket, LPSTR pszDataBuffer, int pos, int length)
{
    pszDataBuffer += pos;
    int len = recv(socket, pszDataBuffer, length, 0);
    if (0 == len)
    {
        return -1;
    }
    if (SOCKET_ERROR == len)
    {
        int error = WSAGetLastError();
        DPRINTF("recv *ERROR* WSAGetLastError %d", error);
        return -1;
    }
    //DPRINTF("recv %d bytes", len);
    return len;
}

int WINAPI BthClose(/*SOCKET*/UINT_PTR socket)
{
    DPRINTF("BthClose()");
    try
    {
        closesocket(socket);
    }
    catch (...)
    {
        // NONE
    }
   // DPRINTF("Ended BthClose()");
    return 0;
}
