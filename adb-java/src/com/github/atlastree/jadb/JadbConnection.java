package com.github.atlastree.jadb;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class JadbConnection implements ITransportFactory {

    private String host;
    private int port;
    private String owner;

    private static final int DEFAULTPORT = 5037;

    public JadbConnection() {
        this("localhost", DEFAULTPORT);
    }

    public JadbConnection(String host,int port){
        this(host,port,"mtp");
    }

    public JadbConnection(String host,int port,String owner){
            this.host = host;
            this.port = port;
            this.owner = owner;
    }

    public Transport createTransport() throws IOException{
        return new Transport(new Socket(host, port));
    }

    public String getHostVersion() throws IOException, JadbException {
        Transport main = createTransport();
        main.send("host:version");
        main.verifyResponse();
        String version = main.readString();
        main.close();
        return version;
    }

    public InetSocketAddress connectToTcpDevice(InetSocketAddress inetSocketAddress)
            throws IOException, JadbException, ConnectionToRemoteDeviceException {
        Transport transport = createTransport();
        try {
            return new HostConnectToRemoteTcpDevice(transport).connect(inetSocketAddress);
        } finally {
            transport.close();
        }
    }

    public InetSocketAddress disconnectFromTcpDevice(InetSocketAddress tcpAddressEntity)
            throws IOException, JadbException, ConnectionToRemoteDeviceException {
        Transport transport = createTransport();
        try {
            return new HostDisconnectFromRemoteTcpDevice(transport).disconnect(tcpAddressEntity);
        } finally {
            transport.close();
        }
    }

    public List<JadbDevice> getDevices() throws IOException, JadbException {
        Transport devices = createTransport();
        devices.send("host:devices");
        devices.verifyResponse();
        String body = devices.readString();
        devices.close();
        return parseDevices(body);
    }

    /**
     * 创建端口转发的实现方法，转发失败则抛异常
     * 成功执行则转发成功
     * 转发类型可以是tcp,localabstract,filesystem
     * @param device
     * @param type
     * @param localPort
     * @param param
     * @return
     * @throws IOException
     * @throws JadbException
     */
    private boolean createForward(JadbDevice device, String type, int localPort, String param) throws IOException, JadbException {
        Transport forward = createTransport();
        String cmd = String.format("host-serial:%1$s:forward:tcp:%2$s;type:%3$s".replace("type",type), device.getSerial(),String.valueOf(localPort),param);
        forward.send(cmd);
        forward.verifyResponse();
        forward.close();
        return true;
    }

    /**
     * 关闭一个端口转发，createForward结束使用后关闭
     * @param device
     * @param localPort
     * @return
     * @throws IOException
     * @throws JadbException
     */
    public boolean killForward(JadbDevice device,int localPort) throws IOException, JadbException {
        Transport kill = createTransport();
        String cmd = String.format("host-serial:%1$s:killforward:tcp:%2$s", device.getSerial(),String.valueOf(localPort));
        kill.send(cmd);
        kill.verifyResponse();
        kill.close();
        return true;
    }

    public void createAbstractForward(JadbDevice device,int localPort,String abstractName) throws IOException, JadbException {
        createForward(device,"localabstract",localPort,abstractName);
    }

    public void createFilesystemForward(JadbDevice device,int localPort,String fileName) throws IOException, JadbException {
        createForward(device,"filesystem",localPort,fileName);
    }

    public void createForward(JadbDevice device,int localPort,int remotePort) throws IOException, JadbException {
        createForward(device,"tcp",localPort,String.valueOf(remotePort));
    }

    public DeviceWatcher createDeviceWatcher(DeviceDetectionListener listener) throws IOException, JadbException {
        Transport transport = createTransport();
        transport.send("host:track-devices");
        transport.verifyResponse();
        return new DeviceWatcher(transport, listener, this);
    }

    public List<JadbDevice> parseDevices(String body) {
        String[] lines = body.split("\n");
        ArrayList<JadbDevice> devices = new ArrayList<JadbDevice>(lines.length);
        for (String line : lines) {
            String[] parts = line.split("\t");
            if (parts.length > 1) {
                devices.add(new JadbDevice(parts[0], parts[1], this,host,Integer.toString(port),owner));
            }
        }
        return devices;
    }

    public JadbDevice getAnyDevice() {
        return JadbDevice.createAny(this);
    }
}
