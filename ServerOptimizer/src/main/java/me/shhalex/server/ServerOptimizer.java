package me.shhalex.server;

import me.shhalex.server.chunk.ChunkOptimizer;
import me.shhalex.server.config.ConfigManager;
import me.shhalex.server.cpu.CpuOptimizer;
import me.shhalex.server.memory.MemoryOptimizer;
import me.shhalex.server.world.WorldBorderEnforcer;

import org.bukkit.plugin.java.JavaPlugin;

public final class ServerOptimizer extends JavaPlugin {

    private static ServerOptimizer instance;

    private ConfigManager configManager;
    private MemoryOptimizer memoryOptimizer;
    private WorldBorderEnforcer worldBorderEnforcer;
    private CpuOptimizer cpuOptimizer;
    private ChunkOptimizer chunkOptimizer;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        configManager.loadConfig();

        this.memoryOptimizer = new MemoryOptimizer(this, configManager);
        memoryOptimizer.enableOptimization();

        this.worldBorderEnforcer = new WorldBorderEnforcer(this, configManager);
        worldBorderEnforcer.enableEnforcement();

        this.cpuOptimizer = new CpuOptimizer(this, configManager);
        cpuOptimizer.enableOptimization();

        this.chunkOptimizer = new ChunkOptimizer(this, configManager);
        chunkOptimizer.enableOptimization();

        getLogger().info("ServerOptimizer activated.");
    }

    @Override
    public void onDisable() {
        if (chunkOptimizer != null) chunkOptimizer.disableOptimization();
        if (cpuOptimizer != null) cpuOptimizer.disableOptimization();
        if (worldBorderEnforcer != null) worldBorderEnforcer.disableEnforcement();
        if (memoryOptimizer != null) memoryOptimizer.disableOptimization();

        getLogger().info("ServerOptimizer stopped.");
    }

    public static ServerOptimizer getInstance() {
        return instance;
    }
}