package devicemanager;

import utils.OSUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tsenov on 2/13/17.
 */
public class AndroidManager {

    public List<String> getInstalledApps(String deviceId) {
        String rowData = OSUtils.runProcess(true, 3, "adb -s " + deviceId + " shell pm list packages -3");
        String trimData = rowData.replace("package:", "");
        String[] list = trimData.split("\\r?\\n");
        return Arrays.asList(list);
    }

    public boolean uninstallApps(String deviceId, String appId) {
        List<String> installedApps = this.getInstalledApps(deviceId);
        boolean appFound = false;
        for (String app : installedApps) {
            if (app.contains(appId)) {
                this.uninstallApp(deviceId, app);
            }
        }
        return appFound;
    }

    public void uninstallApp(String deviceId, String appId) {
        this.stopApp(deviceId, appId);
        String uninstallResult = OSUtils.runProcess(true, 3, "adb -s " + deviceId + " shell pm uninstall -k " + appId);
    }

    protected void stopApp(String deviceId, String appId) {
        String stopCommand = OSUtils.runProcess(true, 3, "adb -s " + deviceId + " shell am force-stop " + appId);
        OSUtils.runProcess(stopCommand);
    }

    public List<String> findConnectedDeviceViaUsb() {
        String listAllDeviceCommand = OSUtils.runProcess(true, 3, "adb devices -l");
        String[] list = listAllDeviceCommand.split("\\r?\\n");
        List<String> devicesIds = new ArrayList<>();
        for (String device : list) {
            if (device.contains("usb") && device.contains("device")) {
                devicesIds.add(device.substring(0, device.indexOf(" ")).trim());
            }
        }

        return devicesIds;
    }

    public List<Device> findAllDevices() {
        String listAllDeviceCommand = OSUtils.runProcess(true, 3, "adb devices -l");
        String[] list = listAllDeviceCommand.split("\\r?\\n");
        List<Device> devices = new ArrayList<>();
        for (String device : list) {
            if (device.contains("device ")) {

                String uidid = device.substring(0, device.indexOf(" ")).trim();
                String type = "emulator";
                if (device.contains("usb")) {
                    type = "real";
                }

                String deviceName = device.substring(device.indexOf("model:"));

                Device d = new Device(uidid, deviceName, DeviceType.Emulator);

                devices.add(d);
            }
        }

        return devices;
    }
}
