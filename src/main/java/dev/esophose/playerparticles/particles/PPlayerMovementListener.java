package dev.esophose.playerparticles.particles;

import dev.esophose.playerparticles.PlayerParticles;
import dev.esophose.playerparticles.manager.ConfigurationManager.Setting;
import dev.esophose.playerparticles.manager.DataManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PPlayerMovementListener implements Listener {
    
    private static final int CHECK_INTERVAL = 3;
    private Map<UUID, Integer> timeSinceLastMovement = new HashMap<>();
    
    public PPlayerMovementListener() {
        DataManager dataManager = PlayerParticles.getInstance().getManager(DataManager.class);

        Bukkit.getScheduler().runTaskTimer(PlayerParticles.getInstance(), () -> {
            if (!Setting.TOGGLE_ON_MOVE.getBoolean())
                return;

            List<UUID> toRemove = new ArrayList<>();

            for (UUID uuid : this.timeSinceLastMovement.keySet()) {
                PPlayer pplayer = dataManager.getPPlayer(uuid);
                if (pplayer == null) {
                    toRemove.add(uuid);
                } else {
                    int standingTime = this.timeSinceLastMovement.get(uuid);
                    pplayer.setMoving(standingTime < Setting.TOGGLE_ON_MOVE_DELAY.getInt());
                    if (standingTime < Setting.TOGGLE_ON_MOVE_DELAY.getInt())
                        this.timeSinceLastMovement.replace(uuid, standingTime + CHECK_INTERVAL);
                }
            }

            for (UUID uuid : toRemove)
                this.timeSinceLastMovement.remove(uuid);
        }, 0, CHECK_INTERVAL);
    }

    /**
     * Used to detect if the player is moving
     * 
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!Setting.TOGGLE_ON_MOVE.getBoolean())
            return;

        if (event.getTo() != null && event.getTo().getBlock() == event.getFrom().getBlock())
            return;
        
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (!this.timeSinceLastMovement.containsKey(playerUUID)) {
            this.timeSinceLastMovement.put(playerUUID, 0);
        } else {
            this.timeSinceLastMovement.replace(playerUUID, 0);
        }
    }

}
