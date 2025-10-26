package dev.themrjezza.kickfromclaim.config;

import dev.themrjezza.kickfromclaim.api.KickFromClaimConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class KickFromClaimSettings extends DataFolderYamlFile implements KickFromClaimConfig {

    public KickFromClaimSettings(@NotNull Plugin plugin) {
        super(plugin, "settings.yml");
    }

    @Override
    protected void generateComments(boolean withDefaultValues) {
        useResource("default-settings.yml", reader -> {
            final var yaml = YamlConfiguration.loadConfiguration(reader);
            yaml.getKeys(false).forEach(key -> {
                if (withDefaultValues) set(key, yaml.get(key + ".value"));
                setComments(key, yaml.getStringList(key + ".comments"));
            });

            if (!withDefaultValues) return;
            final var worlds = Bukkit.getWorlds();
            if (!worlds.isEmpty()) {
                set("kick-to-location", worlds.getFirst().getSpawnLocation());
            } else {
                set("kick-to-location", null);
            }
        });
    }

    @Override
    public long getClickKickDurationMs() {
        return getLong("click-kick-duration", 10000);
    }

    @Override
    public CompletableFuture<Boolean> setClickKickDurationMs(long durationMs) {
        set("click-kick-duration", durationMs);
        return saveAsync();
    }

    @Override
    public boolean getKickToWorldSpawn() {
        return getBoolean("kick-to-world-spawn", false);
    }

    @Override
    public CompletableFuture<Boolean> setKickToWorldSpawn(boolean enabled) {
        set("kick-to-world-spawn", enabled);
        return saveAsync();
    }

    @Override
    public boolean getKickToLocationEnabled() {
        return getBoolean("kick-to-location-enabled", false);
    }

    @Override
    public CompletableFuture<Boolean> setKickToLocationEnabled(boolean enabled) {
        set("kick-to-location-enabled", enabled);
        return saveAsync();
    }

    @Override
    public boolean getKickAnimalsToOwners() {
        return getBoolean("kick-animals-to-owners", false);
    }

    @Override
    public CompletableFuture<Boolean> setAnimalsToOwners(boolean enabled) {
        set("kick-animals-to-owners", enabled);
        return saveAsync();
    }

    @Override
    public boolean getAnimalsMustBeSitting() {
        return getBoolean("animals-must-be-sitting", false);
    }

    @Override
    public CompletableFuture<Boolean> setAnimalsMustBeSitting(boolean enabled) {
        set("animals-must-be-sitting", enabled);
        return saveAsync();
    }

    @Override
    public @Nullable Location getKickToLocation() {
        return getLocation("kick-to-location", null);
    }

    @Override
    public CompletableFuture<Boolean> setKickToLocation(@NonNull World world, @Nullable Location location) {
        set("kick-to-location", location);
        return saveAsync();
    }

    @Override
    public @Nullable Location resolveConfiguredKickLocation(@NonNull World world) {
        if (getKickToWorldSpawn()) {
            return world.getSpawnLocation();
        }
        if (getKickToLocationEnabled()) {
            return getKickToLocation();
        }
        return null;
    }
}
