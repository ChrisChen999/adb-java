package com.github.atlastree.jadb.domain;

import com.github.atlastree.jadb.AndroidDeviceInfoGetter;
import com.github.atlastree.jadb.utils.RealTime;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndroidDevice extends AndroidDeviceInfoGetter {

    private long id;
    private String serialNumber;
    private String productName;
    private String sdkVersion;
    private String systemVersion;
    private String manufacturer;
    private String processorAbi;
    private String memTotal;
    private String memFree;
    private String screenResolution;
    private boolean screenPowerOn;
    private String base64Screenshot;
    private String batteryLevel;
    private String powerType;
    private String adbHost;
    private int adbPort;
    private String deviceStatus;
    private String runningStatus;
    private String owner;
    private Timestamp addTime;
    private Timestamp lastConnectedTime;
    private int appiumPort = -1;
/*
4723,4725,4727,4729,4731,4733,4735,4737,4739,4741,4743,4745,4747,4749,4751,4753,4755,4757,4759,4761
*/

    public static List appiumPorts = new ArrayList(Arrays.asList(4723,4725,4727,4729,4731,4733,4735,4737,4739,4741,4743,4745,4747,4749,4751,4753,4755,4757,4759,4761));
    public AndroidDevice(){}

    public AndroidDevice(String serialNumber, String productName, String sdkVersion, String systemVersion, String manufacturer,
                         String processorAbi, String memTotal, String memFree, String screenResolution, boolean screenPowerOn, String base64Screenshot,
                         String batteryLevel, String powerType, String adbHost, int adbPort, String deviceStatus, String owner){
        this(serialNumber,productName,sdkVersion,systemVersion,manufacturer,processorAbi,memTotal,memFree,
                screenResolution,screenPowerOn,base64Screenshot,batteryLevel,powerType,adbHost,adbPort,
                deviceStatus,"free",owner,Timestamp.valueOf(RealTime.getTimeStampString()),
                Timestamp.valueOf(RealTime.getTimeStampString()));
    }

    public AndroidDevice(String serialNumber, String productName, String sdkVersion, String systemVersion, String manufacturer,
                         String processorAbi, String memTotal, String memFree, String screenResolution, boolean screenPowerOn, String base64Screenshot,
                         String batteryLevel, String powerType, String adbHost, int adbPort, String deviceStatus, String runningStatus, String owner, Timestamp addTime, Timestamp lastConnectedTime){
        this.serialNumber = serialNumber;
        this.productName = productName;
        this.sdkVersion = sdkVersion;
        this.systemVersion = systemVersion;
        this.manufacturer = manufacturer;
        this.processorAbi = processorAbi;
        this.memTotal = memTotal;
        this.memFree = memFree;
        this.screenResolution = screenResolution;
        this.screenPowerOn = screenPowerOn;
        this.base64Screenshot = base64Screenshot;
        this.batteryLevel = batteryLevel;
        this.powerType = powerType;
        this.adbHost = adbHost;
        this.adbPort = adbPort;
        this.deviceStatus = deviceStatus;
        this.runningStatus = runningStatus;
        this.owner = owner;
        this.addTime = addTime;
        this.lastConnectedTime = lastConnectedTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getProcessorAbi() {
        return processorAbi;
    }

    public void setProcessorAbi(String processorAbi) {
        this.processorAbi = processorAbi;
    }

    public String getMemTotal() {
        return memTotal;
    }

    public void setMemTotal(String memTotal) {
        this.memTotal = memTotal;
    }

    public String getMemFree() {
        return memFree;
    }

    public void setMemFree(String memFree) {
        this.memFree = memFree;
    }

    public String getScreenResolution() {
        return screenResolution;
    }

    public void setScreenResolution(String screenResolution) {
        this.screenResolution = screenResolution;
    }

    public boolean isScreenPowerOn() {
        return screenPowerOn;
    }

    public void setScreenPowerOn(boolean screenPowerOn) {
        this.screenPowerOn = screenPowerOn;
    }

    public String getBase64Screenshot() {
        return base64Screenshot;
    }

    public void setBase64Screenshot(String base64Screenshot) {
        this.base64Screenshot = base64Screenshot;
    }

    public String getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(String batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getPowerType() {
        return powerType;
    }

    public void setPowerType(String powerType) {
        this.powerType = powerType;
    }

    public String getAdbHost() {
        return adbHost;
    }

    public void setAdbHost(String adbHost) {
        this.adbHost = adbHost;
    }

    public int getAdbPort() {
        return adbPort;
    }

    public void setAdbPort(int adbPort) {
        this.adbPort = adbPort;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Timestamp getAddTime() {
        return addTime;
    }

    public void setAddTime(Timestamp addTime) {
        this.addTime = addTime;
    }

    public Timestamp getLastConnectedTime() {
        return lastConnectedTime;
    }

    public void setLastConnectedTime(Timestamp lastConnectedTime) {
        this.lastConnectedTime = lastConnectedTime;
    }

    public int getAppiumPort() {
        return appiumPort;
    }

    public void setAppiumPort(int appiumPort) {
        this.appiumPort = appiumPort;
    }

    public String getRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(String runningStatus) {
        this.runningStatus = runningStatus;
    }

    @Override
    public String toXml() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<device name=\"").append(productName).append("\"").
                append(" udid=\"").append(serialNumber).append("\"").
                append(" systemVersion=\"").append(systemVersion).append("\"").
                append(" SDK=\"").append(sdkVersion).append("\"").
                append(" manufacturer=\"").append(manufacturer).append("\"").
                append(" alias=\"").append(manufacturer+"@"+productName).append("\"").
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
                append("<Status>").append(runningStatus).append("</Status>\n").
                append("<runningStatus>").append("running").append("</runningStatus>\n").
                append("<modelImagePath/>\n").
                append("<runtimeBase64Image>").append(base64Screenshot).append("</runtimeBase64Image>\n").
                append("</device>\n");
        return stringBuilder.toString();
    }

    /**
     * 区别于toXml方法，降低对于用户来说的冗余字段带来的带宽消耗
     * @return
     */
    public String toProductXml() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<device name=\"").append(productName).append("\"").
                append(" udid=\"").append(serialNumber).append("\"").
                append(" systemVersion=\"").append(systemVersion).append("\"").
                append(" SDK=\"").append(sdkVersion).append("\"").
                append(" manufacturer=\"").append(manufacturer).append("\"").
                append(" alias=\"").append(manufacturer+productName).append("\"").
                append(" screenResolution=\"").append(screenResolution).append("\"").
                append(" RAM=\"").append(Integer.parseInt(memTotal)/1024).append("\"").
                append(" cpu=\"").append("null").append("\"").
                append(" processor=\"").append(processorAbi).append("\">\n").
                append("<powerType>").append(powerType).append("</powerType>\n").
                append("<memTotal>").append(memTotal).append("</memTotal>\n").
                append("<memFree>").append(memFree).append("</memFree>\n").
                append("<screenPowerOn>").append(screenPowerOn).append("</screenPowerOn>\n").
                append("<onlineStatus>").append(deviceStatus).append("</onlineStatus>\n").
                append("<Status>").append(runningStatus).append("</Status>\n").
                append("<runningStatus>").append("running").append("</runningStatus>\n").
                append("<modelImagePath/>\n").
                append("</device>\n");
        return stringBuilder.toString();
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", serialNumber=").append(serialNumber);
        sb.append(", productName=").append(productName);
        sb.append(", sdkVersion=").append(sdkVersion);
        sb.append(", systemVersion=").append(systemVersion);
        sb.append(", manufacturer=").append(manufacturer);
        sb.append(", processorAbi=").append(processorAbi);
        sb.append(", memTotal=").append(memTotal);
        sb.append(", memFree=").append(memFree);
        sb.append(", screenResolution=").append(screenResolution);
        sb.append(", screenPowerOn=").append(screenPowerOn);
        sb.append(", batteryLevel=").append(batteryLevel);
        sb.append(", powerType=").append(powerType);
        sb.append(", adbHost=").append(adbHost);
        sb.append(", adbPort=").append(adbPort);
        sb.append(", deviceStatus=").append(deviceStatus);
        sb.append(", runningStatus=").append(runningStatus);
        sb.append(", owner=").append(owner);
        sb.append(", addTime=").append(addTime);
        sb.append(", lastConnectedTime=").append(lastConnectedTime);
        sb.append("]");
        return sb.toString();
    }
}
