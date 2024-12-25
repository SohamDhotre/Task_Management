package org.TaskMgmt.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController {

    @Autowired
    private MeterRegistry meterRegistry;

    @GetMapping("/metrics-summary")
    public String getMetricsSummary() {
        StringBuilder metricsSummary = new StringBuilder();

        // Application Startup Metrics
        long startedTime = (long) meterRegistry.get("application.started.time").gauge().value();
        long readyTime = (long) meterRegistry.get("application.ready.time").gauge().value();
        long startupTimeInMillis = readyTime - startedTime;
        long startupTimeInSeconds = startupTimeInMillis / 1000;

        metricsSummary.append("=== Application Startup Metrics ===<br>");
        metricsSummary.append("Application Startup Time: ").append(startupTimeInSeconds).append(" seconds (").append(startupTimeInMillis).append(" ms)<br>");

        // System Metrics
        double diskFree = meterRegistry.get("disk.free").gauge().value();
        double diskTotal = meterRegistry.get("disk.total").gauge().value();
        double cpuCount = meterRegistry.get("system.cpu.count").gauge().value();
        double cpuUsage = meterRegistry.get("system.cpu.usage").gauge().value();

        metricsSummary.append("<br>=== System Metrics ===<br>");
        metricsSummary.append("Free Disk Space: ").append(diskFree).append(" bytes<br>");
        metricsSummary.append("Total Disk Space: ").append(diskTotal).append(" bytes<br>");
        metricsSummary.append("CPU Count: ").append(cpuCount).append("<br>");
        metricsSummary.append("CPU Usage: ").append(cpuUsage).append(" %<br>");

        // JVM Metrics
        double jvmMemoryCommitted = meterRegistry.get("jvm.memory.committed").gauge().value();
        double jvmMemoryMax = meterRegistry.get("jvm.memory.max").gauge().value();
        double jvmMemoryUsed = meterRegistry.get("jvm.memory.used").gauge().value();

        metricsSummary.append("<br>=== JVM Metrics ===<br>");
        metricsSummary.append("JVM Memory Committed: ").append(jvmMemoryCommitted).append(" bytes<br>");
        metricsSummary.append("JVM Memory Max: ").append(jvmMemoryMax).append(" bytes<br>");
        metricsSummary.append("JVM Memory Used: ").append(jvmMemoryUsed).append(" bytes<br>");

        // Thread Metrics
        double jvmThreadsLive = meterRegistry.get("jvm.threads.live").gauge().value();
        double jvmThreadsDaemon = meterRegistry.get("jvm.threads.daemon").gauge().value();
        double jvmThreadsPeak = meterRegistry.get("jvm.threads.peak").gauge().value();

        metricsSummary.append("<br>=== Thread Metrics ===<br>");
        metricsSummary.append("Live Threads: ").append(jvmThreadsLive).append("<br>");
        metricsSummary.append("Daemon Threads: ").append(jvmThreadsDaemon).append("<br>");
        metricsSummary.append("Peak Threads: ").append(jvmThreadsPeak).append("<br>");

        // Database Connection Metrics (HikariCP)
        double hikariConnectionsActive = meterRegistry.get("hikaricp.connections.active").gauge().value();
        double hikariConnectionsIdle = meterRegistry.get("hikaricp.connections.idle").gauge().value();
        double hikariConnectionsMax = meterRegistry.get("hikaricp.connections.max").gauge().value();

        metricsSummary.append("<br>=== Database Connection Metrics ===<br>");
        metricsSummary.append("Active Connections: ").append(hikariConnectionsActive).append("<br>");
        metricsSummary.append("Idle Connections: ").append(hikariConnectionsIdle).append("<br>");
        metricsSummary.append("Max Connections: ").append(hikariConnectionsMax).append("<br>");

        return metricsSummary.toString();
    }
}
