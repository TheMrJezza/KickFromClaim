package dev.themrjezza.kickfromclaim.config;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class MessageCache extends DataFolderYamlFile {
    public MessageCache(@NonNull Plugin plugin) {
        super(plugin, "messages.yml");
    }

    public enum Message {
        CONSOLE_UNSPECIFIED_TARGET("specify-target-from-console"),
        PLAYER_NO_TARGETS_IN_SIGHT("no-targets-in-line-of-sight"),
        TARGET_IS_UNTAMED_ANIMAL("target-is-untamed-animal"),
        TARGET_IS_NOT_SITTING("target-is-not-sitting"),
        INVALID_TARGET_SELECTED("invalid-target-selected"),
        UNKNOWN_TARGET_SELECTED("unknown-target-selected"),
        KICK_REQUEST_ACKNOWLEDGED("kick-request-acknowledged"),
        CLICK_KICK_STARTED("click-kick-started"),
        CLICK_KICK_STOPPED("click-kick-stopped"),
        RELOAD_REQUEST_ACKNOWLEDGED("reload-request-acknowledged"),
        RELOAD_COMPLETE("reload-complete"),
        RELOAD_FAILED("reload-failed");

        private final String configKey;

        Message(@NotNull String configKey) {
            this.configKey = configKey;
        }
    }

    public void sendInfo(@NotNull CommandSender sender, @NotNull Message message) {
        final var text = resolveMessage(message);
        if (text == null || text.isBlank()) return;
        sender.sendMessage(ChatColor.YELLOW + text);
    }

    private String resolveMessage(@NotNull Message message) {
        return getString(message.configKey);
    }

    @Override
    protected void generateComments(boolean withDefaultValues) {
        useResource("default-messages.yml", reader -> {
            final var yaml = YamlConfiguration.loadConfiguration(reader);
            yaml.getKeys(false).forEach(key -> {
                if (withDefaultValues) set(key, yaml.getString(key + ".value"));
                setComments(key, yaml.getStringList(key + ".comments"));
            });
        });
    }
}
