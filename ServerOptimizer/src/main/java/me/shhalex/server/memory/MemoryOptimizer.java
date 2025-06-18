package me.shhalex.server.memory;

import me.shhalex.server.ServerOptimizer;
import me.shhalex.server.config.ConfigManager;
import me.shhalex.server.ErrorInfo;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class MemoryOptimizer {

    private final ServerOptimizer plugin;
    private final ConfigManager configManager;
    private BukkitTask memoryOptimizationTask;

    public MemoryOptimizer(ServerOptimizer plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void enableOptimization() {
        if (!configManager.isMemoryOptimizationEnabled()) return;

        int intervalSeconds = configManager.getMemoryOptimizationIntervalSeconds();
        long intervalTicks = intervalSeconds * 20L;

        memoryOptimizationTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::optimizeMemoryTask, intervalTicks, intervalTicks);
        plugin.getLogger().info("Memory optimization enabled. Checking every " + intervalSeconds + " seconds.");
    }

    public void disableOptimization() {
        if (memoryOptimizationTask != null) {
            memoryOptimizationTask.cancel();
            memoryOptimizationTask = null;
            plugin.getLogger().info("Memory optimization disabled.");
        }
    }

    private void optimizeMemoryTask() {
        try {
            optimizeMemoryLogic();
        } catch (Exception e) {
            ErrorInfo.logError(e, MemoryOptimizer.class);
            plugin.getLogger().severe("Error in MemoryOptimizer: " + e.getMessage());
            if (configManager.isDebugModeEnabled()) e.printStackTrace();
        }
    }

    private void optimizeMemoryLogic() {
        boolean debugMode = configManager.isDebugModeEnabled();
        long initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        System.gc();

        long finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long optimizedBytes = initialMemory - finalMemory;

        if (optimizedBytes > 0 && debugMode) {
            plugin.getLogger().info("Memory optimized: " + (optimizedBytes / 1024 / 1024) + "MB freed.");
        } else if (debugMode) {
            plugin.getLogger().info("Memory optimization performed, but no significant freeing occurred.");
        }
    }
}