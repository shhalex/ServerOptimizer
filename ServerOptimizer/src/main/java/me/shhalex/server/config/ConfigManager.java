package me.shhalex.server.config;

import me.shhalex.server.ServerOptimizer;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final ServerOptimizer plugin;
    private FileConfiguration config;

    public ConfigManager(ServerOptimizer plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        config = plugin.getConfig();
        plugin.reloadConfig(); // Обновляем конфиг на случай изменений
    }

    public boolean isDebugModeEnabled() {
        return config.getBoolean("debug_mode", false);
    }

    // CPU Optimization
    public boolean isCpuOptimizationEnabled() {
        return config.getBoolean("cpu_optimization.enabled", true);
    }
    public int getCpuOptimizationIntervalSeconds() {
        return config.getInt("cpu_optimization.interval_seconds", 60);
    }
    public boolean shouldResetFarMobTarget() {
        return config.getBoolean("cpu_optimization.reset_far_mob_target", true);
    }
    public int getCpuOptimizationMobDespawnRadius() {
        return config.getInt("cpu_optimization.mob_despawn_radius", 64);
    }

    // Memory Optimization
    public boolean isMemoryOptimizationEnabled() {
        return config.getBoolean("memory_optimization.enabled", true);
    }
    public int getMemoryOptimizationIntervalSeconds() {
        return config.getInt("memory_optimization.interval_seconds", 300);
    }

    // Chunk Optimization
    public boolean isChunkOptimizationEnabled() {
        return config.getBoolean("chunk_optimization.enabled", true);
    }
    public int getChunkOptimizationIntervalSeconds() {
        return config.getInt("chunk_optimization.interval_seconds", 300);
    }
    public int getChunkOptimizationUnloadRadius() {
        return config.getInt("chunk_optimization.unload_radius", 8);
    }
    public boolean isChunkOptimizationSafeUnload() {
        return config.getBoolean("chunk_optimization.safe_unload", true);
    }

    // World Border Enforcement
    public boolean isWorldBorderEnforcementEnabled() {
        return config.getBoolean("world_border_enforcement.enabled", true);
    }
    public int getWorldBorderEnforcementIntervalSeconds() {
        return config.getInt("world_border_enforcement.interval_seconds", 30);
    }
    public int getWorldBorderEnforcementSize() {
        return config.getInt("world_border_enforcement.border_size", 10000);
    }
    public String getWorldBorderEnforcementTeleportWorld() {
        return config.getString("world_border_enforcement.teleport_world", "world");
    }
    public Location getWorldBorderEnforcementTeleportLocation() {
        String worldName = config.getString("world_border_enforcement.teleport_world", "world");
        double x = config.getDouble("world_border_enforcement.teleport_location.x", 0.0);
        double y = config.getDouble("world_border_enforcement.teleport_location.y", 100.0);
        double z = config.getDouble("world_border_enforcement.teleport_location.z", 0.0);
        float yaw = (float) config.getDouble("world_border_enforcement.teleport_location.yaw", 0.0);
        float pitch = (float) config.getDouble("world_border_enforcement.teleport_location.pitch", 0.0);
        return new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
    }
}