package me.shhalex.server.cpu;

import me.shhalex.server.ServerOptimizer;
import me.shhalex.server.config.ConfigManager;
import me.shhalex.server.ErrorInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.stream.Collectors;

public class CpuOptimizer {

    private final ServerOptimizer plugin;
    private final ConfigManager configManager;
    private BukkitTask optimizationTask;

    public CpuOptimizer(ServerOptimizer plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void enableOptimization() {
        if (!configManager.isCpuOptimizationEnabled()) return;

        int intervalSeconds = configManager.getCpuOptimizationIntervalSeconds();
        long intervalTicks = intervalSeconds * 20L;

        optimizationTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::runOptimizationTask, intervalTicks, intervalTicks);
        plugin.getLogger().info("CPU optimization enabled. Checking every " + intervalSeconds + " seconds.");
    }

    public void disableOptimization() {
        if (optimizationTask != null) {
            optimizationTask.cancel();
            optimizationTask = null;
            plugin.getLogger().info("CPU optimization disabled.");
        }
    }

    private void runOptimizationTask() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                optimizeCpuLogic();
            } catch (Exception e) {
                ErrorInfo.logError(e, CpuOptimizer.class);
                plugin.getLogger().severe("Error in CpuOptimizer: " + e.getMessage());
                if (configManager.isDebugModeEnabled()) e.printStackTrace();
            }
        });
    }

    private void optimizeCpuLogic() {
        int mobDespawnRadius = configManager.getCpuOptimizationMobDespawnRadius();
        double radiusSquared = mobDespawnRadius * mobDespawnRadius;
        boolean resetFarMobTarget = configManager.shouldResetFarMobTarget();
        boolean debugMode = configManager.isDebugModeEnabled();

        int optimizedTargetsCount = 0;

        for (World world : Bukkit.getWorlds()) {
            List<Player> playersInWorld = world.getPlayers();
            List<Mob> mobsInWorld = world.getLivingEntities().stream()
                    .filter(entity -> entity instanceof Mob)
                    .map(entity -> (Mob) entity)
                    .filter(mob -> mob.isValid() && !mob.isDead())
                    .collect(Collectors.toList());

            for (Mob mob : mobsInWorld) {
                Entity target = mob.getTarget();
                if (resetFarMobTarget && target != null && target instanceof Player) {
                    boolean nearPlayer = false;
                    Location mobLocation = mob.getLocation();

                    for (Player player : playersInWorld) {
                        if (player.getWorld().equals(mob.getWorld()) && mobLocation.distanceSquared(player.getLocation()) < radiusSquared) {
                            nearPlayer = true;
                            break;
                        }
                    }

                    if (!nearPlayer) {
                        if (mob.isValid() && !mob.isDead()) {
                            mob.setTarget(null);
                        }
                        optimizedTargetsCount++;
                    }
                }
            }
        }

        if (optimizedTargetsCount > 0 && debugMode) {
            plugin.getLogger().info("Optimized " + optimizedTargetsCount + " mob targets for CPU reduction.");
        }
    }
}