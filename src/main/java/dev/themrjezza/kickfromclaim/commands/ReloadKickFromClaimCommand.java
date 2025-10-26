package dev.themrjezza.kickfromclaim.commands;

import dev.themrjezza.kickfromclaim.api.KickFromClaimConfig;
import dev.themrjezza.kickfromclaim.config.MessageCache;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReloadKickFromClaimCommand extends AbstractCommandBase {

    private final KickFromClaimConfig config;

    public ReloadKickFromClaimCommand(
            @NotNull JavaPlugin plugin,
            @NotNull MessageCache messageCache,
            @NotNull KickFromClaimConfig config) {
        super(plugin, messageCache, "reloadkickfromclaim");
        this.config = config;
    }

    @Override
    protected void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        messageCache.sendInfo(sender, MessageCache.Message.RELOAD_REQUEST_ACKNOWLEDGED);
        messageCache.reload().thenApplyAsync(messageSuccess -> {
            return messageSuccess ? config.reload().join() : false;
        }).thenAcceptAsync(reloadSuccess -> Bukkit.getScheduler().runTask(plugin, () -> {
            if (reloadSuccess) {
                messageCache.sendInfo(sender, MessageCache.Message.RELOAD_COMPLETE);
            } else {
                messageCache.sendInfo(sender, MessageCache.Message.RELOAD_FAILED);
            }
        }));
    }

    @Override
    protected void tabComplete(
            @NotNull CommandSender sender,
            @NotNull String[] args,
            @NotNull List<String> completions) {
    }
}
