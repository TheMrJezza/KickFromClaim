package dev.themrjezza.kickfromclaim.commands;

import dev.themrjezza.kickfromclaim.api.ClickKickManager;
import dev.themrjezza.kickfromclaim.config.MessageCache;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClickKickFromClaimCommand extends AbstractCommandBase {

    private final ClickKickManager ckManager;

    public ClickKickFromClaimCommand(
            @NotNull JavaPlugin plugin,
            @NotNull MessageCache messageCache,
            @NotNull ClickKickManager punchKickEngine) {
        super(plugin, messageCache, "clickkickfromclaim");
        this.ckManager = punchKickEngine;
    }

    @Override
    protected void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 0 && !(sender instanceof Player)) {
            messageCache.sendInfo(sender, MessageCache.Message.CONSOLE_UNSPECIFIED_TARGET);
            return;
        }

        Player target;
        if (!(sender instanceof Player player)) {
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                messageCache.sendInfo(sender, MessageCache.Message.UNKNOWN_TARGET_SELECTED);
                return;
            }
        } else {
            target = player;
        }

        if (ckManager.isClickKickEnabled(target)) {
            ckManager.stopClickKick(target);
        } else {
            ckManager.startClickKick(target);
        }
    }

    @Override
    protected void tabComplete(
            @NotNull CommandSender sender,
            @NotNull String[] args,
            @NotNull List<String> completions) {
        if (!(sender instanceof Player) && args.length == 1) {
            Bukkit.getOnlinePlayers().stream().map(Player::getName).sorted().forEachOrdered(completions::add);
        }
    }
}
