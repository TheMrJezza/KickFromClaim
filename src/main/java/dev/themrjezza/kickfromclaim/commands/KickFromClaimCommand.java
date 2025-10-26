package dev.themrjezza.kickfromclaim.commands;

import dev.themrjezza.kickfromclaim.api.KickFromClaimConfig;
import dev.themrjezza.kickfromclaim.api.KickRequestResult;
import dev.themrjezza.kickfromclaim.config.MessageCache;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Tameable;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class KickFromClaimCommand extends AbstractCommandBase {

    private final KickFromClaimConfig config;

    public KickFromClaimCommand(
            @NotNull JavaPlugin plugin,
            @NotNull MessageCache messageCache,
            @NotNull KickFromClaimConfig config) {
        super(plugin, messageCache, "kickfromclaim");
        this.config = config;
    }

    @Override
    protected void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player) && args.length == 0) {
            messageCache.sendInfo(sender, MessageCache.Message.CONSOLE_UNSPECIFIED_TARGET);
            return;
        }

        LivingEntity target;
        if (args.length == 0) {
            final var player = (Player) sender;
            final var eye = player.getEyeLocation();

            final var ray = player.getWorld().rayTrace(
                    eye,
                    eye.getDirection(),
                    16,
                    FluidCollisionMode.ALWAYS,
                    true,
                    0.1D,
                    e -> e instanceof LivingEntity && !e.equals(player)
            );

            if (ray == null || ray.getHitBlock() != null || ray.getHitEntity() == null) {
                messageCache.sendInfo(player, MessageCache.Message.PLAYER_NO_TARGETS_IN_SIGHT);
                return;
            }

            target = (LivingEntity) ray.getHitEntity();

            if (target instanceof Tameable tameable) {
                if (!tameable.isTamed()) {
                    messageCache.sendInfo(player, MessageCache.Message.TARGET_IS_UNTAMED_ANIMAL);
                    return;
                }
                if (config.getAnimalsMustBeSitting()) {
                    if (!(target instanceof Sittable sittable) || !sittable.isSitting()) {
                        messageCache.sendInfo(player, MessageCache.Message.TARGET_IS_NOT_SITTING);
                        return;
                    }
                }
            } else if (!(target instanceof Player)) {
                messageCache.sendInfo(player, MessageCache.Message.INVALID_TARGET_SELECTED);
                return;
            }
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                messageCache.sendInfo(sender, MessageCache.Message.UNKNOWN_TARGET_SELECTED);
                return;
            }
        }

        messageCache.sendInfo(sender, MessageCache.Message.KICK_REQUEST_ACKNOWLEDGED);

        final UUID trustID;
        if (target instanceof Tameable tameable && tameable.getOwner() != null) {
            trustID = tameable.getOwner().getUniqueId();
        } else {
            trustID = target.getUniqueId();
        }

        final var targetPos = target.getLocation();
        final var senderID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        final var dataStore = JavaPlugin.getPlugin(GriefPrevention.class).dataStore;

        CompletableFuture.supplyAsync(() -> {
            return dataStore.getClaimAt(targetPos, false, null);
        }).thenAcceptAsync(claim -> Bukkit.getScheduler().runTask(plugin, () -> {
            final var result = ((Supplier<KickRequestResult>) () -> {
                if (claim == null || !claim.contains(target.getLocation(), false, true)) {
                    return KickRequestResult.TARGET_NOT_IN_ANY_CLAIM;
                }

                if (senderID != null && claim.checkPermission(senderID, ClaimPermission.Manage, null) != null) {
                    return KickRequestResult.TARGET_NOT_IN_MANAGED_CLAIM;
                }

                if (claim.checkPermission(trustID, ClaimPermission.Access, null) == null) {
                    return KickRequestResult.TARGET_HAS_TRUST_IN_CLAIM;
                }

                final var player = Bukkit.getPlayer(trustID);
                if (player != null && player.hasPermission("kickfromclaim.exempt")) {
                    return KickRequestResult.TARGET_HAS_EXEMPT_PERMISSION;
                }

                return KickRequestResult.SUCCESS;
            }).get();

            switch (result) {
                case SUCCESS -> {
                    var kickTo = config.resolveConfiguredKickLocation(target.getWorld());
                    if (kickTo == null) {
                        kickTo = target.getLocation();
                    }

                    target.teleport(kickTo);
                    sender.sendMessage("KICK SUCCESS");
                }
                case TARGET_HAS_TRUST_IN_CLAIM -> sender.sendMessage("TARGET HAS TRUST");
                case TARGET_NOT_IN_ANY_CLAIM -> sender.sendMessage("TARGET NOT IN ANY CLAIM");
                case TARGET_NOT_IN_MANAGED_CLAIM -> sender.sendMessage("TARGET NOT IN A CLAIM YOU MANAGE");
                case TARGET_HAS_EXEMPT_PERMISSION -> sender.sendMessage("TARGET HAS EXEMPT PERMISSION");
            }
        }));
    }

    @Override
    protected void tabComplete(
            @NotNull CommandSender sender,
            @NotNull String[] args,
            @NotNull List<String> completions) {
        if (args.length == 1) {
            Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> {
                return !name.equals(sender.getName());
            }).sorted().forEachOrdered(completions::add);
        }
    }
}
