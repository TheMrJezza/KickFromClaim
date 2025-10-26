package dev.themrjezza.kickfromclaim;

import dev.themrjezza.kickfromclaim.api.KickFromClaimConfig;
import dev.themrjezza.kickfromclaim.api.ClickKickManager;
import dev.themrjezza.kickfromclaim.config.MessageCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClickKickEngine implements Listener, ClickKickManager {

    private final KickFromClaimConfig config;
    private final MessageCache messageCache;
    private final ConcurrentHashMap<UUID, Long> lastPunchTimes = new ConcurrentHashMap<>();
    private boolean enabled = false;

    public ClickKickEngine(
            @NotNull MessageCache messageCache,
            @NotNull KickFromClaimConfig config) {
        this.messageCache = messageCache;
        this.config = config;
    }

    public void start(@NotNull Plugin plugin) {
        if (!enabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateLoop, 0, 5);
            enabled = true;
        }
    }

    private void updateLoop() {
        final var currentTime = System.currentTimeMillis();
        lastPunchTimes.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() >= config.getClickKickDurationMs()) {
                final var player = Bukkit.getPlayer(entry.getKey());
                if (player != null) notifyStopped(player);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean startClickKick(@NotNull Player player) {
        final var now = System.currentTimeMillis();
        final var start = lastPunchTimes.put(player.getUniqueId(), now);
        if (start == null || System.currentTimeMillis() - start >= config.getClickKickDurationMs()) {
            messageCache.sendInfo(player, MessageCache.Message.CLICK_KICK_STARTED);
            return true;
        }
        return false;
    }

    @Override
    public boolean stopClickKick(@NotNull Player player) {
        notifyStopped(player);
        return lastPunchTimes.remove(player.getUniqueId()) != null;
    }

    @Override
    public boolean isClickKickEnabled(@NotNull Player player) {
        final var start = lastPunchTimes.get(player.getUniqueId());
        return start != null && System.currentTimeMillis() - start < config.getClickKickDurationMs();
    }

    private void notifyStopped(@NotNull Player player) {
        if (lastPunchTimes.containsKey(player.getUniqueId())) {
            messageCache.sendInfo(player, MessageCache.Message.CLICK_KICK_STOPPED);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        switch (event.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK, RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK ->
                    handleEntityClick(event, event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        handleEntityClick(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerAttackEntity(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            handleEntityClick(event, player);
        }
    }

    private void handleEntityClick(@NotNull Cancellable event, @NotNull Player player) {
        if (isClickKickEnabled(player)) {
            startClickKick(player);
            player.performCommand("kickfromclaim");
            event.setCancelled(true);
        }
    }
}
