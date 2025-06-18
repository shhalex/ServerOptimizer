package me.shhalex.server.chunk;

import me.shhalex.server.ServerOptimizer;
import me.shhalex.server.config.ConfigManager;
import me.shhalex.server.ErrorInfo;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Collection;

public class ChunkOptimizer {

    private final ServerOptimizer plugin;
    private final ConfigManager configManager;
    private BukkitTask chunkOptimizationTask;

    public ChunkOptimizer(ServerOptimizer plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void enableOptimization() {
        if (!configManager.isChunkOptimizationEnabled()) return;

        int intervalSeconds = configManager.getChunkOptimizationIntervalSeconds();
        long intervalTicks = intervalSeconds * 20L;

        chunkOptimizationTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::optimizeChunksTask, intervalTicks, intervalTicks);
        plugin.getLogger().info("Chunk optimization enabled. Checking every " + intervalSeconds + " seconds.");
    }

    public void disableOptimization() {
        if (chunkOptimizationTask != null) {
            chunkOptimizationTask.cancel();
            chunkOptimizationTask = null;
            plugin.getLogger().info("Chunk optimization disabled.");
        }
    }

    private void optimizeChunksTask() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                optimizeChunksLogic();
            } catch (Exception e) {
                ErrorInfo.logError(e, ChunkOptimizer.class);
                plugin.getLogger().severe("Error in ChunkOptimizer: " + e.getMessage());
                if (configManager.isDebugModeEnabled()) e.printStackTrace();
            }
        });
    }

    private void optimizeChunksLogic() {
        boolean debugMode = configManager.isDebugModeEnabled();
        int unloadRadius = configManager.getChunkOptimizationUnloadRadius();
        boolean safeUnload = configManager.isChunkOptimizationSafeUnload();
        int chunksUnloaded = 0;

        for (World world : Bukkit.getWorlds()) {
            Collection<? extends Player> players = world.getPlayers();
            for (Chunk chunk : world.getLoadedChunks()) {
                boolean hasPlayerNear = false;
                for (Player player : players) {
                    if (player.getWorld().equals(world) &&
                            Math.abs(chunk.getX() * 16 - player.getLocation().getX()) <= unloadRadius * 16 &&
                            Math.abs(chunk.getZ() * 16 - player.getLocation().getZ()) <= unloadRadius * 16) {
                        hasPlayerNear = true;
                        break;
                    }
                }

                if (!hasPlayerNear) {
                    if (safeUnload && chunk.getEntities().length > 0) {
                        // Если "безопасная" выгрузка и есть сущности, не выгружаем.
                        // Или можно добавить более сложную проверку на "важные" сущности.
                        continue;
                    }

                    // Попытка выгрузить чанк
                    if (chunk.unload(true)) { // true = сохраняем перед выгрузкой
                        chunksUnloaded++;
                    }
                }
            }
        }

        if (chunksUnloaded > 0 && debugMode) {
            plugin.getLogger().info("Optimized " + chunksUnloaded + " chunks.");
        }
    }
}