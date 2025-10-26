package dev.themrjezza.kickfromclaim.commands;

import dev.themrjezza.kickfromclaim.config.MessageCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommandBase implements CommandExecutor, TabCompleter {

    protected final Plugin plugin;
    protected final MessageCache messageCache;

    public AbstractCommandBase(
            @NotNull JavaPlugin plugin,
            @NotNull MessageCache messageCache,
            @NotNull String commandName) {
        this.plugin = plugin;
        this.messageCache = messageCache;
        final var command = plugin.getCommand(commandName);
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    @Override
    public final @NotNull List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        final var completions = new ArrayList<String>();
        tabComplete(sender, args, completions);
        return completions;
    }

    @Override
    public final boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        execute(sender, args);
        return true;
    }

    protected abstract void execute(
            @NotNull CommandSender sender,
            @NotNull String[] args
    );

    protected abstract void tabComplete(
            @NotNull CommandSender sender,
            @NotNull String[] args,
            @NotNull List<String> completions
    );
}
