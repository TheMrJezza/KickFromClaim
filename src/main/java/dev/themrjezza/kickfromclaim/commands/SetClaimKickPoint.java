package dev.themrjezza.kickfromclaim.commands;

import dev.themrjezza.kickfromclaim.api.KickFromClaimConfig;
import dev.themrjezza.kickfromclaim.config.MessageCache;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SetClaimKickPoint extends AbstractCommandBase {

    private KickFromClaimConfig config;

    public SetClaimKickPoint(
            @NotNull JavaPlugin plugin,
            @NotNull MessageCache messageCache,
            @NotNull KickFromClaimConfig config) {
        super(plugin, messageCache, "setclaimkickpoint");
        this.config = config;
    }

    @Override
    protected void execute(
            @NotNull CommandSender sender,
            @NotNull String[] args) {
        if (sender instanceof Player player) {
            messageCache.sendInfo(sender, MessageCache.Message.CONFIG_CHANGE_REQUEST_ACKNOWLEDGED);
            config.setKickToLocation(player.getLocation()).thenApplyAsync(res -> {
                return res ? config.setKickToLocationEnabled(true).join() : false;
            }).thenAcceptAsync(success -> Bukkit.getScheduler().runTask(plugin, () -> {
                if (success) {
                    messageCache.sendInfo(sender, MessageCache.Message.CONFIG_CHANGE_COMPLETE);
                } else {
                    messageCache.sendInfo(sender, MessageCache.Message.CONFIG_CHANGE_FAILED);
                }
            }));
        } else {
            messageCache.sendInfo(sender, MessageCache.Message.PLAYER_ONLY_COMMAND);
        }
    }

    @Override
    protected void tabComplete(
            @NotNull CommandSender sender,
            @NotNull String[] args,
            @NotNull List<String> completions) {
    }
}
