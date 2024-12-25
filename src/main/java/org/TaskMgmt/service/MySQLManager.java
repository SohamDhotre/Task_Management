package org.TaskMgmt.service;

import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class MySQLManager {
    public static void startMySQL() throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        Process process;
        if (os.contains("win")) {
            System.out.println("Starting MySQL on Windows...");
            process = new ProcessBuilder("cmd", "/c", "net start mysql").start();
        } else if (os.contains("mac")) {
            System.out.println("Starting MySQL on macOS...");
            process = new ProcessBuilder("brew", "services", "start", "mysql").start();
        } else if (os.contains("nux")) {
            System.out.println("Starting MySQL on Linux...");
            process = new ProcessBuilder("sudo", "systemctl", "start", "mysql").start();
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }
        process.waitFor();
    }

    public static void stopMySQL() throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        Process process;
        if (os.contains("win")) {
            System.out.println("Stopping MySQL on Windows...");
            process = new ProcessBuilder("cmd", "/c", "net stop mysql").start();
        } else if (os.contains("mac")) {
            System.out.println("Stopping MySQL on macOS...");
            process = new ProcessBuilder("brew", "services", "stop", "mysql").start();
        } else if (os.contains("nux")) {
            System.out.println("Stopping MySQL on Linux...");
            process = new ProcessBuilder("sudo", "systemctl", "stop", "mysql").start();
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }
        process.waitFor();
    }
}
