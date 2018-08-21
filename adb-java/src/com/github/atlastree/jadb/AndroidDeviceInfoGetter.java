package com.github.atlastree.jadb;

public abstract class AndroidDeviceInfoGetter {

    public abstract String getSerialNumber();
    public abstract String getProductName();
    public abstract String getSdkVersion();
    public abstract String getSystemVersion();
    public abstract String getManufacturer();
    public abstract String getProcessorAbi();
    public abstract String getMemTotal();
    public abstract String getMemFree();
    public abstract String getScreenResolution();
    public abstract boolean isScreenPowerOn();
    public abstract String getBase64Screenshot();
    public abstract String getBatteryLevel();
    public abstract String getPowerType();
    public abstract String getAdbHost();
    public abstract int getAdbPort();
    public abstract String getDeviceStatus();
    public abstract String toXml();

}
