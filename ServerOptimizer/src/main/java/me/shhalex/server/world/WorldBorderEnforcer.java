package me.shhalex.server.world;

import me.shhalex.server.ServerOptimizer;
import me.shhalex.server.config.ConfigManager;
import me.shhalex.server.ErrorInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class WorldBorderEnforcer {

    private final ServerOptimizer plugin;
    private final ConfigManager configManager;
    private BukkitTask enforcementTask;

    public WorldBorderEnforcer(ServerOptimizer plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void enableEnforcement() {
        if (!configManager.isWorldBorderEnforcementEnabled()) return;

        int intervalSeconds = configManager.getWorldBorderEnforcementIntervalSeconds();
        long intervalTicks = intervalSeconds * 20L;

        enforcementTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::enforceWorldBorderTask, intervalTicks, intervalTicks);
        plugin.getLogger().info("World border enforcement enabled. Checking every " + intervalSeconds + " seconds.");
    }

    public void disableEnforcement() {
        if (enforcementTask != null) {
            enforcementTask.cancel();
            enforcementTask = null;
            plugin.getLogger().info("World border enforcement disabled.");
        }
    }

    private void enforceWorldBorderTask() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                enforceWorldBorderLogic();
            } catch (Exception e) {
                ErrorInfo.logError(e, WorldBorderEnforcer.class);
                plugin.getLogger().severe("Error in WorldBorderEnforcer: " + e.getMessage());
                if (configManager.isDebugModeEnabled()) e.printStackTrace();
            }
        });
    }

    private void enforceWorldBorderLogic() {
        boolean debugMode = configManager.isDebugModeEnabled();
        int borderSize = configManager.getWorldBorderEnforcementSize();
        String teleportWorldName = configManager.getWorldBorderEnforcementTeleportWorld();
        Location teleportLocation = configManager.getWorldBorderEnforcementTeleportLocation();

        World teleportWorld = Bukkit.getWorld(teleportWorldName);
        if (teleportWorld == null) {
            if (debugMode) {
                plugin.getLogger().warning("Teleport world '" + teleportWorldName + "' not found for WorldBorderEnforcer.");
            }
            return;
        }

        int playersTeleported = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location playerLoc = player.getLocation();
            if (Math.abs(playerLoc.getX()) > borderSize || Math.abs(playerLoc.getZ()) > borderSize) {
                player.teleport(teleportLocation);
                playersTeleported++;
                if (debugMode) {
                    plugin.getLogger().info("Player " + player.getName() + " was teleported due to leaving world border.");
                }
            }
        }

        if (playersTeleported > 0 && debugMode) {
            plugin.getLogger().info("Teleported " + playersTeleported + " players due to leaving world border.");
        }
    }
}