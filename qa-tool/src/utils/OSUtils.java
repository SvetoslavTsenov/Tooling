package utils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Utils for host operating system.
 */
public class OSUtils {

    public static final String[] WIN_RUNTIME = {"cmd.exe", "/C"};
    public static final String[] OS_LINUX_RUNTIME = {"/bin/bash", "-l", "-c"};

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static String[] concat(String[] first, String[] second) {
        String[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


    private static OSType getOSType() {
        String osTypeString = System.getProperty("os.name", "generic").toLowerCase();
        if ((osTypeString.contains("mac")) || (osTypeString.contains("darwin"))) {
            return OSType.MacOS;
        } else if (osTypeString.contains("win")) {
            return OSType.Windows;
        } else if (osTypeString.contains("nux")) {
            return OSType.Linux;
        }

        return null;
    }

    /**
     * Run command (start process).
     *
     * @param waitFor Wait for process to finish.
     * @param timeOut Timeout for process.
     * @param command Command to be executed.
     * @return Output of command execution.
     */
    public static String runProcess(boolean waitFor, int timeOut, String... command) {
        String[] allCommand = null;

        String finalCommand = "";
        for (String s : command) {
            finalCommand = finalCommand + s;
        }

        try {
            if (OSUtils.getOSType() == OSType.Windows) {
                allCommand = concat(WIN_RUNTIME, command);
            } else {
                allCommand = concat(OS_LINUX_RUNTIME, command);
            }
            ProcessBuilder pb = new ProcessBuilder(allCommand);
            Process p = pb.start();

            if (waitFor) {
                StringBuffer output = new StringBuffer();

                // Note: No idea why reader should be before p.waitFor(),
                //       but when it is after p.waitFor() execution of
                //       some adb command freeze on Windows
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    output.append(line + "\n");
                }

                p.waitFor(timeOut, TimeUnit.SECONDS);
                return output.toString();
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Execute command (start process).
     *
     * @param command Command.
     * @return Output of command execution.
     */
    public static String runProcess(String... command) {
        return runProcess(true, 10 * 60, command); // Might be we should .trim()
    }

    /**
     * Execute command (start process).
     *
     * @param timeOut Timeout for command execution in secconds.
     * @param command Command.
     * @return Output of command execution.
     */
    public static String runProcess(int timeOut, String... command) {
        return runProcess(true, timeOut, command);
    }

    /**
     * Stop process.
     *
     * @param name Name of running process.
     */
    public static void stopProcess(String name) {
        try {
            if (OSUtils.getOSType() == OSType.Windows) {
                String command = "taskkill /F /IM " + name;
                runProcess(command);
            } else {
                String stopCommand = "ps -A | grep '" + name + "'";

                String processes = runProcess(stopCommand);
                String lines[] = processes.split("(\r\n|\n)");

                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].trim();
                    String procId = line.split("\\s+")[0];
                    runProcess("kill -9 " + procId);
                }
            }
        } catch (Exception e) {
        }
    }

    public static void openFileExplorer(String imagesTempFolder) {
        try {
            Desktop.getDesktop().open(new File(imagesTempFolder));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
