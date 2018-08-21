package test;

import com.github.atlastree.jadb.JadbConnection;
import com.github.atlastree.jadb.JadbDevice;
import com.github.atlastree.jadb.JadbException;
import com.github.atlastree.jadb.RemoteFile;
import com.github.atlastree.jadb.domain.AndroidDevice;
import com.github.atlastree.jadb.managers.Package;
import com.github.atlastree.jadb.managers.PackageManager;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.List;
import java.util.Scanner;

public class testJadb {

    /*
     *测试JadbConnection连接
     */
    private static void testJConnection(String hostIp,int adbPort){
        JadbConnection jadbConnection = new JadbConnection(hostIp,adbPort);
        try {
            List<JadbDevice> jadbDevices = jadbConnection.getDevices();
            System.out.println("HOST:"+hostIp+":"+adbPort+"\nConnected Device(s) Number:"+jadbDevices.size());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
    }

    /*
     *测试获取设备并初始化设备属性，转换为AndroidDevice对象
     */
    private static void testJadbdevice(){
        JadbConnection jadbConnection = new JadbConnection("172.7.20.160",5037);
        try {
            List<JadbDevice> jadbDevices = jadbConnection.getDevices();
            for(JadbDevice device:jadbDevices){
                AndroidDevice ad = device.toAndroidDevice();
                System.out.println("Device details:"+ad);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
    }

    private static void testPushFileToRemoteDevice(String filepath){
        JadbConnection jadbConnection = new JadbConnection("172.7.20.160",5037);
        String filename = filepath.split("\\\\")[filepath.split("\\\\").length-1];
        try {
            List<JadbDevice> jadbDevices = jadbConnection.getDevices();
            for(JadbDevice device:jadbDevices){
                device.push(new File(filepath),new RemoteFile("/data/local/tmp/"+filename));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
    }

    private static void testListFile(String path){
        JadbConnection jadbConnection = new JadbConnection("172.7.20.160",5037);
        try {
            List<JadbDevice> jadbDevices = jadbConnection.getDevices();
            for(JadbDevice device:jadbDevices){
                List<RemoteFile> remoteFiles = device.list(path);
                for(RemoteFile remoteFile:remoteFiles){
                    System.out.println(remoteFile.getPath());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
    }

    private static void testInstallApk(String path){
        JadbConnection jadbConnection = new JadbConnection("172.7.20.160",5037);
        try {
            List<JadbDevice> jadbDevices = jadbConnection.getDevices();
            for(JadbDevice device:jadbDevices){
                new PackageManager(device).install(new File(path));
                List<Package> packages = new PackageManager(device).getPackages();
                System.out.println(""+packages);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
    }

    private static void testUninstallApk(String packagename){
        JadbConnection jadbConnection = new JadbConnection("172.7.20.160",5037);
        try {
            List<JadbDevice> jadbDevices = jadbConnection.getDevices();
            for(JadbDevice device:jadbDevices){
                new PackageManager(device).uninstall(new Package(packagename));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
    }

    public static void testExecuteShell(){
        String cmd = "LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P 1080x1920@1080x1920/0";
        JadbConnection jadbConnection = new JadbConnection("172.7.20.160",5037);
        try {
            List<JadbDevice> jadbDevices = jadbConnection.getDevices();
            for(JadbDevice device:jadbDevices){
                device.makeScreenOn();
                InputStream is = device.executeShell(cmd);
                jadbConnection.createAbstractForward(device,9047,"minicap");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                jadbConnection.killForward(device,9047);
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bf = new BufferedReader(isr);
                String line = bf.readLine();
                while(line!=null && line!=""){
                    System.out.println(line);
                    line = bf.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
    }

    public static void testGetScreenshot(){
        JadbConnection jadbConnection = new JadbConnection("172.7.20.160",5037);
        try {
            List<JadbDevice> jadbDevices = jadbConnection.getDevices();
            for(JadbDevice device:jadbDevices){
                device.makeScreenOn();
                String html = "data:text/html,<img src=\"data:image/png;base64,"+device.getBase64Screenshot()+"\">";
                File file = new File(System.getProperty("user.dir")+File.separator+"screenshot.html");
                if(file.exists()){
                    file.delete();
                }else {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(html.getBytes());
                fos.close();
                System.out.println("screenshot html generated,file path:"+System.getProperty("user.dir")+File.separator+"screenshot.html");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
    }

    public static void testLog(){
        JadbConnection jadbConnection = new JadbConnection("172.7.20.160",5037);
        try {
            List<JadbDevice> jadbDevices = jadbConnection.getDevices();
            for(JadbDevice device:jadbDevices){
                InputStream is = device.executeShell("logcat");
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bf = new BufferedReader(isr);
                String line = bf.readLine();
                while (line!=null && line!=""){
                    System.out.println(line);
                    line = bf.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        testJConnection("172.7.20.160",5037);
        //testJadbdevice();
        //testPushFileToRemoteDevice("C:\\Users\\atlas\\Desktop\\test.txt");
        //testListFile("/data/local/tmp");
        //testUninstallApk("com.hik.laputa");
        //testInstallApk("C:\\Users\\crs\\Desktop\\HikCentral Cloud_V1.0.0_Build20180718.apk");
        //testExecuteShell();
        testGetScreenshot();
        testLog();
    }

}
