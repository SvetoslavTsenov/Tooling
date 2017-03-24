package devicemanager;

import utils.OSUtils;

import java.util.ArrayList;
import java.util.List;

public class IOSManager {

    public void uninstallApp(String appId) {
        String uninstallResult = OSUtils.runProcess("ideviceinstaller -U " + appId + " -U " + appId);
        String commandSecondCheck = String.format("ios-deploy --list -1 %s", appId);
        String resultOfSecondCheck = OSUtils.runProcess(commandSecondCheck);
        String uninstallAppCommand = String.format("ios-deploy --uninstall_only -1 %s", appId);
        String result = OSUtils.runProcess(uninstallAppCommand);
    }

    public List<Device> getAllDevices() {
        List<Device> allDevices = new ArrayList<>();
        allDevices.addAll(this.getRealDevices());
        allDevices.addAll(this.getRunningSimulators());

        return allDevices;
    }

    public List<Device> getRealDevices() {
        String commandGetAvailableDevices = "ios-deploy -c";
        String[] devices = OSUtils.runProcess(commandGetAvailableDevices).trim().split("\\r?\\n");
        List<Device> allDevices = new ArrayList<>();
        for (String device : devices) {
            if (device.contains("Found")) {
                String uidId = device.substring(device.lastIndexOf("(") + 1, device.lastIndexOf(")"));
                String name = device.substring(device.lastIndexOf("Found") + 5, device.lastIndexOf("("));
                Device d = new Device(uidId, name, DeviceType.iOS);
                allDevices.add(d);
            }
        }

        return allDevices;
    }

    public String[] getDeviceApps(String deviceId) {
        // Try to list all apps to verify if device works properly
        String fileContent = OSUtils.runProcess(5000, "ideviceinstaller -u " + deviceId + " -l");

        return fileContent.trim().split("\\r?\\n");
    }

    public List<Device> getRunningSimulators() {
        String rowDevices = OSUtils.runProcess("xcrun simctl list devices | grep 'Booted'");
        String[] deviceList = rowDevices.split("\\r?\\n");
        List<Device> sumulators = new ArrayList<>();

        for (String device : deviceList) {
            String udid = device.substring(device.indexOf('(') + 1, device.indexOf(')'));
            String name = device.substring(0, device.indexOf(')'));
            Device s = new Device(udid, name,DeviceType.Simulator);

            sumulators.add(s);
        }
        return sumulators;
    }

    public void eraseData() {
//        Simctl.LOGGER_BASE.warn("Erase data from simulator");
//        String command = "xcrun simctl erase " + this.settings.deviceId;
//        Simctl.LOGGER_BASE.warn(command);
//        OSUtils.runProcess(command);
    }

//    protected void resetSimulatorSettings() {
//        if (this.settings.debug) {
//            LOGGER_BASE.info("[Debug mode] Do not reset sim settings.");
//        } else {
//            String path = this.settings.screenshotResDir + File.separator + this.settings.testAppImageFolder;
//            if (FileSystem.exist(path)) {
//                LOGGER_BASE.info("This test run will compare images. Reset simulator zoom.");
//                try {
//                    FileSystem.deletePath(System.getProperty("user.home") + "/Library/Preferences/com.apple.iphonesimulator.plist");
//                    Wait.sleep(1000);
//                    OSUtils.runProcess("defaults write ~/Library/Preferences/com.apple.iphonesimulator SimulatorWindowLastScale \"1\"");
//                    Wait.sleep(1000);
//                    LOGGER_BASE.info("Global simulator settings restarted");
//                } catch (IOException e) {
//                    LOGGER_BASE.error("Failed to restart global simulator settings.");
//                }
//            } else {
//                LOGGER_BASE.info("No need to restart simulator settings.");
//            }
//        }
//    }

    protected List<String> getSimulatorUdidsByName(String name) {
        String command = "xcrun simctl list devices | grep " + name.replaceAll("\\s", "\\\\ ");
        String output = OSUtils.runProcess(command);
        String[] lines = output.split("\\r?\\n");
        List<String> list = new ArrayList<>();

        for (String line : lines) {
            // TODO(vchimev): Rethink! If the simulator is unavailable, it means that:
            // - runtime profile not found => test execution should exit earlier,
            // - the simulator is broken and should be recreated.
            if (!line.contains("unavailable") && !line.isEmpty()) {
                String udid = line.substring(line.indexOf('(') + 1, line.indexOf(')'));
                list.add(udid);
            }
        }

        return list;
    }
}
