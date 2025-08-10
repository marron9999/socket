#include "stdafx.h"
#include "bthlib.h"
#include "bthapp.h"

ULONG StringToBthAddr(
		LPSTR remoteAddr,
        PSOCKADDR_BTH pRemoteBtAddr)
{
    ULONG       ulRetCode = CXN_SUCCESS;
	int iAddrLen = sizeof(SOCKADDR_BTH);
	ulRetCode = WSAStringToAddress(remoteAddr, AF_BTH, NULL, (LPSOCKADDR) pRemoteBtAddr, &iAddrLen);
	if ( CXN_SUCCESS != ulRetCode )
	{
		DPRINTF("-FATAL- | Unable to get address of the remote radio having formated address-string %s", remoteAddr);
	}
   return ulRetCode;
}

ULONG BthAddrToString(
        PSOCKADDR_BTH pRemoteBtAddr,
		LPSTR remoteAddr)
{
    ULONG       ulRetCode = CXN_SUCCESS;
	DWORD iAddrLen = 256;
	char addr[256] = "";
	ulRetCode = WSAAddressToString((LPSOCKADDR) pRemoteBtAddr, sizeof(SOCKADDR_BTH), NULL, addr, &iAddrLen);
	if ( CXN_SUCCESS != ulRetCode )
	{
		DPRINTF("-FATAL- | Unable to get address of the remote radio having formated address-string %s", remoteAddr);
	} else {
		lstrcpy(remoteAddr, addr);
	}
   return ulRetCode;
}

ULONG NameToBthAddr(
        LPSTR pszRemoteName,
        PSOCKADDR_BTH pRemoteBtAddr)
{
    INT             iResult = CXN_SUCCESS;
    BOOL            bContinueLookup = FALSE;
	BOOL            bRemoteDeviceFound = FALSE;
    ULONG           ulFlags = 0;
    HANDLE          hLookup = NULL;
    PWSAQUERYSET    pWSAQuerySet = 0;
    DWORD           lnWSAQuerySet = sizeof(WSAQUERYSET);

    if (pszRemoteName != NULL && *pszRemoteName != 0)
    {
        if (pRemoteBtAddr != NULL)
            memset(pRemoteBtAddr, 0, sizeof(SOCKADDR_BTH));

		char value[256] = "";
		DefProfile(pszRemoteName);
        GetProfile("Detect", "mac", value, sizeof(value));
        if(value[0] != 0)
		{
            DPRINTF("Detect %s %s", pszRemoteName, value);
            StringToBthAddr(value, pRemoteBtAddr);
			if(pRemoteBtAddr->addressFamily == AF_BTH)
			{
		        return  CXN_SUCCESS;
			}
		}
    }

	pWSAQuerySet = (PWSAQUERYSET) new BYTE[lnWSAQuerySet];
    memset(pWSAQuerySet, 0, lnWSAQuerySet);

    if (CXN_SUCCESS == iResult) {

        {
            ulFlags = LUP_CONTAINERS;
            ulFlags |= LUP_FLUSHCACHE;
            ulFlags |= LUP_RETURN_NAME;
            ulFlags |= LUP_RETURN_ADDR;
            iResult = CXN_SUCCESS;
            hLookup = 0;
            bContinueLookup = FALSE;
            pWSAQuerySet->dwNameSpace = NS_BTH;
            pWSAQuerySet->dwSize = lnWSAQuerySet;
            DPRINTF("WSALookupServiceBegin()");
            iResult = WSALookupServiceBegin(pWSAQuerySet, ulFlags, &hLookup);
            if ((NO_ERROR == iResult) && (NULL != hLookup))
            {
                bContinueLookup = TRUE;
            }
            else
            {
                iResult = WSAGetLastError();
	            DPRINTF("WSALookupServiceBegin *ERROR* WSAGetLastError %d", iResult);
            }

            while (bContinueLookup)
            {
                //printf("WSALookupServiceNext()");
                if (NO_ERROR == WSALookupServiceNext(hLookup,
                    ulFlags,
                    &lnWSAQuerySet,
                    pWSAQuerySet))
                {
                    if((pWSAQuerySet->lpszServiceInstanceName != NULL)
	                && (pWSAQuerySet->lpszServiceInstanceName[0] != 0))
                    {
						char value[256] = "";
						BthAddrToString((PSOCKADDR_BTH)pWSAQuerySet->lpcsaBuffer->RemoteAddr.lpSockaddr, value);
						DefProfile(pWSAQuerySet->lpszServiceInstanceName);
                        SetProfile("Detect", "mac", value);
                        DPRINTF("Regist %s %s", pWSAQuerySet->lpszServiceInstanceName, value);
                        //if (bthdetect != NULL)
                        //{
                        //    bthdetect(pWSAQuerySet->lpszServiceInstanceName);
                        //}
                        if(pszRemoteName != NULL 
                        && *pszRemoteName != 0
                        && pRemoteBtAddr != NULL
                        && 0 == lstrcmpi(pWSAQuerySet->lpszServiceInstanceName, pszRemoteName))
                        {
                            lstrcpy(pszRemoteName, pWSAQuerySet->lpszServiceInstanceName);
                            memcpy(pRemoteBtAddr,
                                (void*)pWSAQuerySet->lpcsaBuffer->RemoteAddr.lpSockaddr,
                                sizeof(SOCKADDR_BTH));
                            bRemoteDeviceFound = TRUE;
                        }
                    }
                }
                else
                {
                    iResult = WSAGetLastError();
                    if (WSA_E_NO_MORE == iResult)
                    {
                        bContinueLookup = FALSE;
                    }
                    else if (WSAEFAULT == iResult)
                    {
                        delete[](BYTE*) pWSAQuerySet;
                        pWSAQuerySet = (PWSAQUERYSET) new BYTE[lnWSAQuerySet];
                        memset(pWSAQuerySet, 0, lnWSAQuerySet);
                    }
                    else
                    {
						DPRINTF("WSALookupServiceNext *ERROR* WSAGetLastError %d", iResult);
                        bContinueLookup = FALSE;
                    }
                }
            }

            DPRINTF("WSALookupServiceEnd()");
            WSALookupServiceEnd(hLookup);
        }
    }

    if (pWSAQuerySet != 0)
    {
        delete[](BYTE*) pWSAQuerySet;
        pWSAQuerySet = 0;
    }
    if (bRemoteDeviceFound) {
        iResult = CXN_SUCCESS;
    }

	return iResult;
}

UINT_PTR WINAPI BthConnect(LPCSTR RemoteName)
{

    char szRemoteName[256];
    lstrcpy(szRemoteName, RemoteName);
    SOCKADDR_BTH RemoteAddr = { 0 };
    NameToBthAddr(szRemoteName, &RemoteAddr);
    if (RemoteAddr.addressFamily != AF_BTH)
    {
        DPRINTF("*ERROR Unable to get BlueTooth address (%s)", RemoteName);
        return 0;
    }

    SOCKADDR_BTH SockAddrBthServer = RemoteAddr;
    SockAddrBthServer.addressFamily = AF_BTH;
    SockAddrBthServer.serviceClassId = ServiceClassGuid;
    SockAddrBthServer.port = 0;
    SOCKET localSocket = socket(AF_BTH, SOCK_STREAM, BTHPROTO_RFCOMM);
    if ( INVALID_SOCKET == localSocket )
    {
        int error = WSAGetLastError();
        DPRINTF("socket *ERROR* WSAGetLastError %d", error);
        return 0;
    }

    if (SOCKET_ERROR == connect(localSocket,
            (struct sockaddr*)&SockAddrBthServer,
            sizeof(SOCKADDR_BTH)))
    {
        int error = WSAGetLastError();
        DPRINTF("connect *ERROR* WSAGetLastError %d", error);
        BthClose(localSocket);
        return 0;
    }

    DPRINTF("BthConnect(%s)=%I64d", szRemoteName, (INT64) localSocket);
    return localSocket;
}

