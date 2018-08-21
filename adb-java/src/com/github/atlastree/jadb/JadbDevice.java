package com.github.atlastree.jadb;
import com.github.atlastree.jadb.domain.AndroidDevice;
import com.github.atlastree.jadb.managers.Bash;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JadbDevice extends AndroidDeviceInfoGetter{

    private boolean hasInit = false;
    private static final String systemVersionPrefix = "Android ";
    private static final String SDK_COMMAND = "ro.build.version.sdk";
    private static final String SYSTEM_COMMAND = "ro.build.version.release";
    private static final String NAME_COMMAND = "ro.product.model";
    private static final String MANUFACTURER_COMMAND ="ro.product.manufacturer";
    private static final String PROCESSOR_ABI = "ro.product.cpu.abi";
    private static final String SCREEN_RESOLUTION_COMMAND = "wm size";
    private static final String DUMP_BATTERY_COMMAND = "dumpsys battery";
    private static final String DUMP_WINDOW_POLICY_COMMAND = "dumpsys window policy";
    private static final String SLEEP_WINDOW_COMMAND = "input keyevent 26";
    private static final String CAT_MEMORY_COMMAND = "cat /proc/meminfo";
    private static final String CAT_CPU_COMMAND = "cat /proc/cpuinfo";
    private static final String WORK_DIR = System.getProperty("user.dir");

    private final String serial;
    private String productName;
    private String sdkVersion;
    private String systemVersion;
    private String manufacturer;
    private String processorAbi;
    private String memTotal;
    private String memFree;
    private String screenResolution;
    private boolean screenPowerOn;
    private String batteryLevel;
    private String powerType;
    private String adbHost;
    private String adbPort;
    private String deviceStatus;
    private String base64Screenshot;
    private final ITransportFactory transportFactory;
    private String owner;

    /**
     * 初始化全部属性
     * 获取任何属性前如果没有初始化过会自动初始化
     */
    private void initProperty(){
        productName = getProp("getprop " + NAME_COMMAND);
        sdkVersion = getProp("getprop " + SDK_COMMAND);
        systemVersion = systemVersionPrefix + getProp("getprop " + SYSTEM_COMMAND);
        manufacturer = translateManufacturerToChinese(getProp("getprop " + MANUFACTURER_COMMAND));
        processorAbi = getProp("getprop " + PROCESSOR_ABI);
        String temp_sreen_resolution = getProp(SCREEN_RESOLUTION_COMMAND);
        if(temp_sreen_resolution.contains("Override size")){
            //some Samsung Devices have both Physical Size and Override Size,we note Override size as display size
            screenResolution = temp_sreen_resolution.split(":")[2].trim();
        }else {
            screenResolution = temp_sreen_resolution.split(":")[1].trim();
        }

        updateScreenshot();
        updateProperty();
        deviceStatus = "online";
        hasInit = true;
    }

    /**
     * 调用更新电池，内存，屏幕是否点亮这三个属性的方法
     */
    public void updateProperty(){
        updateBattery();
        updateMem();
        updateScreenPowerOn();
    }

    private String translateManufacturerToChinese(String manufacturer){
        if(manufacturer.equals("HUAWEI")||manufacturer.equals("huawei")||manufacturer.equals("Huawei")){
            return "华为";
        }else if(manufacturer.equals("Xiaomi")||manufacturer.equals("XIAOMI")||manufacturer.equals("xiaomi")){
            return "小米";
        }else if(manufacturer.equals("Samsung")||manufacturer.equals("SAMSUNG")||manufacturer.equals("samsung")){
            return "三星";
        }else if(manufacturer.equals("Google")||manufacturer.equals("google")||manufacturer.equals("GOOGLE")){
            return "谷歌";
        }else {
            return manufacturer;
        }
    }

    /**
     * 执行shellCommand并等待shell进程结束
     * 返回shell结果
     * @param shellCommmad
     * @return
     */
    private String getProp(String shellCommmad){
        InputStream is = null;
        try {
            is = executeShell(shellCommmad);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            StringBuilder resultBuilder = new StringBuilder();
            String readLine = bufferedReader.readLine();
            while(readLine!=null){
                resultBuilder.append(readLine);
                readLine = bufferedReader.readLine();
            }
            return resultBuilder.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
        return "getPropFailed";
    }

    /**
     * 更新屏幕状态
     * true为亮，false为灭
     */
    private boolean updateScreenPowerOn(){
        String res = getProp(DUMP_WINDOW_POLICY_COMMAND);
        //System.out.println(res);
        Pattern pattern = Pattern.compile("mScreenOnFully=false");
        Matcher matcher = pattern.matcher(res);
        while (matcher.find()){
            screenPowerOn = false;
            return false;
        }
        pattern = Pattern.compile("mScreenOnFully=true");
        matcher = pattern.matcher(res);
        while (matcher.find()){
            screenPowerOn = true;
            return true;
        }
        return false;
    }

    /**
     * 关闭屏幕并更新屏幕状态属性
     * 返回为是否执行关闭操作
     * @return
     */
    public boolean makeScreenOff(){
        if(updateScreenPowerOn()){
            getProp(SLEEP_WINDOW_COMMAND);
            updateScreenPowerOn();
            return true;
        }
        return false;
    }

    /**
     * 开启屏幕并更新屏幕状态属性
     * 返回为是否执行开启操作
     * @return
     */
    public boolean makeScreenOn(){
        if(!updateScreenPowerOn()){
            getProp(SLEEP_WINDOW_COMMAND);
            updateScreenPowerOn();
            return true;
        }
        return false;
    }

    /**
     * 获取截图后进行base64编码
     * 存储在base64Screenshot里
     * 如果设备熄屏，则自动亮屏--->修改为不会自动亮屏
     * 除初始化会运行外，需手动运行
     */
    public void updateScreenshot(){
        if(base64Screenshot == null){
            base64Screenshot = "null";
        }
        //makeScreenOn();
        try {
            BufferedImage bufferedImage = ImageIO.read(executeShell("/system/bin/screencap -p"));
            String fileName = "screenshot-"+ UUID.randomUUID().toString()+".png";
            File file = new File(WORK_DIR+File.separator+fileName);
            //System.out.println("FILE:"+fileName);
            //System.out.println("WORK_DIR:"+WORK_DIR);
            if(bufferedImage == null){
                getProp("/system/bin/screencap -p /data/local/tmp/"+fileName);
                pull(new RemoteFile("/data/local/tmp/"+fileName),file);
                bufferedImage = ImageIO.read(file);
                file.delete();
                getProp("rm /data/local/tmp/"+fileName);
            }
            //ImageIO.write(bufferedImage,"png",new File(WORK_DIR+File.separator+fileName));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage,"png",byteArrayOutputStream);
            BASE64Encoder encoder = new BASE64Encoder();
            base64Screenshot = encoder.encode(byteArrayOutputStream.toByteArray());
            //System.out.println(base64Screenshot.length());
            //System.out.println("<img src= \"data:image/png;base64," + base64Img + "\"/>");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新电池信息
     */
    private void updateBattery(){
        String batteryProps = getProp(DUMP_BATTERY_COMMAND);
        Pattern pattern = Pattern.compile("level: [0-9]{1,3}");
        Matcher matcher = pattern.matcher(batteryProps);
        if(batteryLevel == null){
            batteryLevel = "null";
        }
        while (matcher.find()){
            batteryLevel = matcher.group().split(":")[1].trim();
        }

        pattern = Pattern.compile("AC powered: true");
        matcher = pattern.matcher(getProp(DUMP_BATTERY_COMMAND));
        if(powerType == null){
            powerType = "null";
        }
        while (matcher.find()){
            powerType = matcher.group().split(":")[0].trim();
        }
        pattern = Pattern.compile("USB powered: true");
        matcher = pattern.matcher(getProp(DUMP_BATTERY_COMMAND));
        while (matcher.find()){
            powerType = matcher.group().split(":")[0].trim();
        }
    }

    /**
     * 更新内存信息
     */
    private void updateMem(){
        Pattern pattern = Pattern.compile("MemTotal:[0-9]{3,8}kB");
        Matcher matcher = pattern.matcher(getProp(CAT_MEMORY_COMMAND).replace(" ",""));
        if(memTotal == null){
            memTotal = "null";
        }
        while (matcher.find()){
            memTotal = matcher.group().split(":")[1].split("kB")[0].trim();
        }

        pattern = Pattern.compile("MemFree:[0-9]{3,8}kB");
        matcher = pattern.matcher(getProp(CAT_MEMORY_COMMAND).replace(" ",""));
        if(memFree == null){
            memFree = "null";
        }
        while (matcher.find()){
            memFree = matcher.group().split(":")[1].split("kB")[0].trim();
        }
    }

    /**
     * 获取序列号
     * @return
     */
    @Override
    public String getSerialNumber() {
        if (!hasInit){
            initProperty();
        }
        return serial;
    }

    /**
     * 获取产品名称
     * @return
     */
    @Override
    public String getProductName() {
        if (!hasInit){
            initProperty();
        }
        return productName;
    }

    /**
     * 获取SDK版本
     * @return
     */
    @Override
    public String getSdkVersion() {
        if (!hasInit){
            initProperty();
        }
        return sdkVersion;
    }

    /**
     * 获取系统版本
     * @return
     */
    @Override
    public String getSystemVersion() {
        if (!hasInit){
            initProperty();
        }
        return systemVersion;
    }

    /**
     * 获取制造商
     * @return
     */
    @Override
    public String getManufacturer() {
        if (!hasInit){
            initProperty();
        }
        return manufacturer;
    }

    /**
     * 获取CPU架构
     * @return
     */
    @Override
    public String getProcessorAbi() {
        if (!hasInit){
            initProperty();
        }
        return processorAbi;
    }

    /**
     * 获取总内存，单位KB
     * @return
     */
    @Override
    public String getMemTotal() {
        if (!hasInit){
            initProperty();
        }
        return memTotal;
    }

    /**
     * 获取剩余内存，单位KB
     * @return
     */
    @Override
    public String getMemFree() {
        if (!hasInit){
            initProperty();
        }
        return memFree;
    }

    /**
     * 获取分辨率
     * @return
     */
    @Override
    public String getScreenResolution() {
        if (!hasInit){
            initProperty();
        }
        return screenResolution;
    }

    @Override
    public boolean isScreenPowerOn() {
        if(!hasInit){
            initProperty();
        }
        return screenPowerOn;
    }

    /**
     * 获取设备截图
     * 返回Base64编码格式的字符串
     * @return
     */
    @Override
    public String getBase64Screenshot() {
        if (!hasInit){
            initProperty();
        }
        return base64Screenshot;
    }

    /**
     * 获取当前电池电量
     * @return
     */
    @Override
    public String getBatteryLevel() {
        if (!hasInit){
            initProperty();
        }
        return batteryLevel;
    }

    /**
     * 获取电源种类
     * @return
     */
    @Override
    public String getPowerType() {
        if (!hasInit){
            initProperty();
        }
        return powerType;
    }

    /**
     * 获取设备节点主机IP
     * @return
     */
    @Override
    public String getAdbHost() {
        if (!hasInit){
            initProperty();
        }
        return adbHost;
    }

    /**
     * 获取设备节点主机端口
     * @return
     */
    @Override
    public int getAdbPort() {
        if (!hasInit){
            initProperty();
        }
        return Integer.parseInt(adbPort);
    }

    /**
     * 获取设备状态
     * @return
     */
    @Override
    public String getDeviceStatus() {
        if (!hasInit){
            initProperty();
        }
        return deviceStatus;
    }

    /**
     * toXML方法
     * 返回包含设备全部信息的XML格式
     * @return
     */
    @Override
    public String toXml() {
        if(!hasInit){
            initProperty();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<device name=\"").append(productName).append("\"").
                append(" udid=\"").append(serial).append("\"").
                append(" systemVersion=\"").append(systemVersion).append("\"").
                append(" SDK=\"").append(sdkVersion).append("\"").
                append(" manufacturer=\"").append(manufacturer).append("\"").
                append(" alias=\"").append(" ").append("\"").
                append(" screenResolution=\"").append(screenResolution).append("\"").
                append(" RAM=\"").append(Integer.parseInt(memTotal)/1024).append("\"").
                append(" cpu=\"").append("null").append("\"").
                append(" processor=\"").append(processorAbi).append("\">\n").
                append("<powerType>").append(powerType).append("</powerType>\n").
                append("<host>").append(adbHost).append("</host>\n").
                append("<port>").append(adbPort).append("</port>\n").
                append("<memTotal>").append(memTotal).append("</memTotal>\n").
                append("<memFree>").append(memFree).append("</memFree>\n").
                append("<screenPowerOn>").append(screenPowerOn).append("</screenPowerOn>\n").
                append("<onlineStatus>").append(deviceStatus).append("</onlineStatus>\n").
                append("<Status>").append("free").append("</Status>\n").
                append("<runningStatus>").append("running").append("</runningStatus>\n").
                append("<modelImagePath/>\n").
                append("<runtimeBase64Image>").append(base64Screenshot).append("</runtimeBase64Image>\n").
                append("</device>\n");
        return stringBuilder.toString();
    }

    public AndroidDevice toAndroidDevice(){
        if(!hasInit){
            initProperty();
        }
        return new AndroidDevice(serial,productName,sdkVersion,systemVersion,manufacturer,processorAbi,memTotal,memFree,screenResolution,screenPowerOn,base64Screenshot,batteryLevel,powerType,adbHost,Integer.parseInt(adbPort),deviceStatus,owner);
    }

    public enum State {
        Unknown,
        Offline,
        Device,
        Recovery,
        BootLoader
    };

    JadbDevice(String serial, String type, ITransportFactory tFactory) {
        this.serial = serial;
        this.transportFactory = tFactory;
    }

    JadbDevice(String serial, String type, ITransportFactory tFactory, String adbHost, String adbPort,String owner) {
        this.serial = serial;
        this.transportFactory = tFactory;
        this.adbHost = adbHost;
        this.adbPort = adbPort;
        this.owner = owner;
    }

    static JadbDevice createAny(JadbConnection connection) {
        return new JadbDevice(connection);
    }

    private JadbDevice(ITransportFactory tFactory) {
        serial = null;
        this.transportFactory = tFactory;
    }

    private State convertState(String type) {
        switch (type) {
            case "device":     return State.Device;
            case "offline":    return State.Offline;
            case "bootloader": return State.BootLoader;
            case "recovery":   return State.Recovery;
            default:           return State.Unknown;
        }
    }

    private Transport getTransport() throws IOException, JadbException {
        Transport transport = transportFactory.createTransport();
        if (serial == null) {
            transport.send("host:transport-any");
            transport.verifyResponse();
        } else {
            transport.send("host:transport:" + serial);
            transport.verifyResponse();
        }
        return transport;
    }

    public String getSerial() {
        return serial;
    }

    public State getState() throws IOException, JadbException {
        Transport transport = transportFactory.createTransport();
        if (serial == null) {
            transport.send("host:get-state");
            transport.verifyResponse();
        } else {
            transport.send("host-serial:" + serial + ":get-state");
            transport.verifyResponse();
        }

        State state = convertState(transport.readString());
        transport.close();
        return state;
    }

    /** <p>Execute a shell command.</p>
     *
     * <p>For Lollipop and later see: {@link #execute(String, String...)}</p>
     *
     * @param command main command to run. E.g. "ls"
     * @param args arguments to the command.
     * @return combined stdout/stderr stream.
     * @throws IOException
     * @throws JadbException
     */
    public InputStream executeShell(String command, String... args) throws IOException, JadbException {
        Transport transport = getTransport();
        StringBuilder shellLine = buildCmdLine(command, args);
        send(transport, "shell:" + shellLine.toString());
        return new AdbFilterInputStream(new BufferedInputStream(transport.getInputStream()));
    }

    /**
     *
     * @deprecated Use InputStream executeShell(String command, String... args) method instead. Together with
     * Stream.copy(in, out), it is possible to achieve the same effect.
     */
    @Deprecated
    public void executeShell(OutputStream output, String command, String... args) throws IOException, JadbException {
        Transport transport = getTransport();
        StringBuilder shellLine = buildCmdLine(command, args);
        send(transport, "shell:" + shellLine.toString());
        if (output != null) {
        	AdbFilterOutputStream out = new AdbFilterOutputStream(output);
        	try {
        		transport.readResponseTo(out);
        	} finally {
        		out.close();
        	}
        }
    }

    /** <p>Execute a command with raw binary output.</p>
     *
     * <p>Support for this command was added in Lollipop (Android 5.0), and is the recommended way to transmit binary
     * data with that version or later. For earlier versions of Android, use
     * {@link #executeShell(String, String...)}.</p>
     *
     * @param command main command to run, e.g. "screencap"
     * @param args arguments to the command, e.g. "-p".
     * @return combined stdout/stderr stream.
     * @throws IOException
     * @throws JadbException
     */
    public InputStream execute(String command, String... args) throws IOException, JadbException {
        Transport transport = getTransport();
        StringBuilder shellLine = buildCmdLine(command, args);
        send(transport, "exec:" + shellLine.toString());
        return new BufferedInputStream(transport.getInputStream());
    }

    /**
     * Builds a command line string from the command and its arguments.
     *
     * @param command the command.
     * @param args the list of arguments.
     * @return the command line.
     */
    private StringBuilder buildCmdLine(String command, String... args) {
        StringBuilder shellLine = new StringBuilder(command);
        for (String arg : args) {
            shellLine.append(" ");
            shellLine.append(Bash.quote(arg));
        }
        return shellLine;
    }

    public List<RemoteFile> list(String remotePath) throws IOException, JadbException {
        Transport transport = getTransport();
        SyncTransport sync = transport.startSync();
        sync.send("LIST", remotePath);

        List<RemoteFile> result = new ArrayList<RemoteFile>();
        for (RemoteFileRecord dent = sync.readDirectoryEntry(); dent != RemoteFileRecord.DONE; dent = sync.readDirectoryEntry()) {
            result.add(dent);
        }
        return result;
    }

    private int getMode(File file) {
        //noinspection OctalInteger
        return 0664;
    }

    public void push(InputStream source, long lastModified, int mode, RemoteFile remote) throws IOException, JadbException {
        Transport transport = getTransport();
        SyncTransport sync = transport.startSync();
        sync.send("SEND", remote.getPath() + "," + Integer.toString(mode));

        sync.sendStream(source);

        sync.sendStatus("DONE", (int) lastModified);
        sync.verifyStatus();
    }

    public void push(File local, RemoteFile remote) throws IOException, JadbException {
        FileInputStream fileStream = new FileInputStream(local);
        push(fileStream, local.lastModified(), getMode(local), remote);
        fileStream.close();
    }

    public void pull(RemoteFile remote, OutputStream destination) throws IOException, JadbException {
        Transport transport = getTransport();
        SyncTransport sync = transport.startSync();
        sync.send("RECV", remote.getPath());

        sync.readChunksTo(destination);
    }

    public void pull(RemoteFile remote, File local) throws IOException, JadbException {
        FileOutputStream fileStream = new FileOutputStream(local);
        pull(remote, fileStream);
        fileStream.close();
    }

    private void send(Transport transport, String command) throws IOException, JadbException {
        transport.send(command);
        transport.verifyResponse();
    }

    @Override
    public String toString() {
        return "Android Device with serial " + serial;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serial == null) ? 0 : serial.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JadbDevice other = (JadbDevice) obj;
        if (serial == null) {
            if (other.serial != null)
                return false;
        } else if (!serial.equals(other.serial))
            return false;
        return true;
    }
}
