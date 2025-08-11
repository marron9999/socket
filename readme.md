<h2>Socket</h2>

Controlling the Command Prompt on a Windows PC.

<table border=0><tr>
<th>Client (Windows 11) : Connect (BLE socket) to VS2022</th>
<th> Server (VS2022 : Windows 10)</th>
</tr><tr>
<td valign=top><img src=img/client.png /></td>
<td valign=top><img src=img/server.png /></td>
</tr></table>

<h3>Execute : Server mode</h3>

cd {socket project folder}<br>
set LOG=..\logs<br>
java -Djdk.attach.allowAttachSelf=true -cp bin;jna\\* socket.server \[port-number\]

- jna folder: Copy jna jar files, swt jar file, and bthlib.dll (build bthlib with VS2022)
- \[port-number\]: (defualt) 9999

<h3>Execute : Client mode (socket)</h3>

cd {socket project folder}<br>
set LOG=..\logs<br>
java -Djdk.attach.allowAttachSelf=true -cp bin;jna\\* socket.client \{server-name\} \[port-number\]

- jna folder: Copy jna jar files, swt jar file, and bthlib.dll (build bthlib with VS2022)
- \{server-name\}: Host-Name or IP-Address (ex. vs2022)
- \[port-number\]: 9999 (defualt) 

<h3>Execute : Client mode (BLE socket)</h3>

cd {socket project folder}<br>
set LOG=..\logs<br>
java -Djdk.attach.allowAttachSelf=true -cp bin;jna\\* socket.client BLE {ble-name}

- jna folder: Copy jna jar files, swt jar file, and bthlib.dll (build bthlib with VS2022)
- {ble-name}: Bluetoot-Name or IP-Address (ex. vs2022)

<ul>

Setting: jna\bthlib.\{ble-name\}.ini

<table><tr><td>
[Detect]<br>
mac=(mac-address)
</td></tr></table>

mac-address must set example format and value of "Ethernet adapter Bluetooth - Physical Address"

EX: jna\bthlib.vs2022.ini

<table><tr><td>
[Detect]<br>
mac=(00:1B:DC:06:3E:BD)
</td></tr></table>

IMSPECT: ipconfig -all

<table><tr><td>
Ethernet adapter Bluetooth xxxxx:<br>
&nbsp; &nbsp; Physical Address. . . . . . . . . : 00-1B-DC-06-3E-BD
</td></tr></table>
</ul>

<h3>Built-in operations : Client mode</h3>

(1) #download \[ folder-path \]

Display/Specify download folder

EX: #download c:\\temp

(2) @download file-mask

Download server side current folder files wihich matched file-mask to client side download folder

EX: @download *.log

<table border=0><tr>
<td valign=top><img src=img/download.png /></td>
</tr></table>

(3) @upload file-mask

Upload client side files which matched file-mask to server side current folder

EX: @upload c:\\temp\\*.dat

<table border=0><tr>
<td valign=top><img src=img/upload.png /></td>
</tr></table>

(4) @sysmon \[ on | off | true | false \]

Enable/Disable system monitor

EX: @sysmon on

<table border=0><tr>
<td valign=top><img src=img/sysmon.png /></td>
</tr></table>

(5) #appmon \[ {add|del} app-mask \]

Add/Delete app-mask for application monitor

EX: #appmon add java*

(6) @appmon \[ on | off | true | false \]

Enable/Disable application monitor

EX: @appmon on

<table border=0><tr>
<td valign=top><img src=img/appmon.png /></td>
</tr></table>

(7) @screen \[ on | off | true | false \]

Show/Hide server side command prompt window

EX: @screen on

NOTE: When execute as Server mode with /screen switch, show server side command prompt window at connect from Client mode

<table border=0><tr>
<td valign=top><img src=img/screen.png /></td>
</tr></table>

(8) @print \[ screen-number \]

Download screen shot of screen number 0 or more

EX: @print 0

(9) @{file-path}

Execute command list in text file

NOTE: Command list must described by code page MS932

EX: @c:\\temp\\cmd.txt

<table><tr>
<td>chcp 437<br>
cd /d c:\\temp<br>
dir /w<br>
exit</td>
</tr></table>

<table border=0><tr>
<td valign=top><img src=img/cmdlist.png /></td>
</tr></table>

<h3>Require Software</h3>

- OpenJDK11U-jdk_x64_windows_hotspot_11.0.27_6.zip
- jna-5.14.0.zip
- swt-4.24-win32-win32-x86_64.zip
- Visual Studio 2022

