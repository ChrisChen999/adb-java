# adb-java
Adb(Android Debug Bridge) client implemented by java,based on jadb project

## Description
This project provides all adb-client functions,including getting devices,package management,port forward,device properties,file management and so on.

## Quickstart
- Getting devices
```java
JadbConnection jadb = new JadbConnection();
List<JadbDevice> devices = jadb.getDevices();
```
- Port forward(Abstract)
```java
List<JadbDevice> jadbDevices = jadbConnection.getDevices();
JadbDevice device = jadbDevices.get(0);
device.makeScreenOn();
jadbConnection.createAbstractForward(device,9047,"minicap");
jadbConnection.killForward(device,9047);//do this when finishing port using
```
- Getting properties
```java
JadbConnection jadbConnection = new JadbConnection("localhost",5037);
List<JadbDevice> jadbDevices = jadbConnection.getDevices();
for(JadbDevice device:jadbDevices){
    AndroidDevice ad = device.toAndroidDevice();//AndroidDevice is a bean object
    System.out.println("Device details:"+ad);
}
```
- File management
```java
List<JadbDevice> jadbDevices = jadbConnection.getDevices();
JadbDevice device = jadbDevices.get(0);
device.push(new File(filepath),new RemoteFile("/data/local/tmp/"+filename));//push file
List<RemoteFile> remoteFiles = device.list(path);//list file
for(RemoteFile remoteFile:remoteFiles){
    System.out.println(remoteFile.getPath());
}
```

## License
This project is released under Apache License Version 2.0
