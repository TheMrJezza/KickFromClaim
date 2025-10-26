package dev.themrjezza.kickfromclaim.config;

import dev.themrjezza.kickfromclaim.api.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

public abstract class DataFolderYamlFile extends YamlConfiguration implements Reloadable {
    protected final Plugin plugin;
    protected File yamlFile;

    public DataFolderYamlFile(@NotNull Plugin plugin, @NotNull String fileName) {
        this.plugin = plugin;
        this.yamlFile = new File(plugin.getDataFolder(), fileName);
        options().width(150);
    }

    protected abstract void generateComments(boolean withDefaultValues);

    protected void useResource(String resourceName, Consumer<InputStreamReader> consumer) {
        try (final var resource = plugin.getResource(resourceName)) {
            if (resource == null) return;
            try (final var reader = new InputStreamReader(resource)) {
                consumer.accept(reader);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load resource " + resourceName);
        }
    }

    @Override
    public CompletableFuture<Boolean> reload() {
        return CompletableFuture.supplyAsync(() -> {

            final var exists = this.yamlFile.exists();
            if (!exists) {
                generateComments(true);
                if (!saveAsync().join()) {
                    return false;
                }
            }

            try {
                this.load(this.yamlFile);
                generateComments(false);
                return exists ? saveAsync().join() : true;
            } catch (IOException | InvalidConfigurationException e) {
                printError(e, "Failed to load " + this.yamlFile.getName());
                return false;
            }
        });
    }

    protected CompletableFuture<Boolean> saveAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.save(this.yamlFile);
                return true;
            } catch (IOException e) {
                printError(e, "Failed to save " + this.yamlFile.getName());
                return false;
            }
        });
    }

    private void printError(Throwable throwable, String message) {
        Bukkit.getScheduler().runTask(plugin, () -> plugin.getLogger().log(
                Level.SEVERE,
                message,
                throwable
        ));
    }
}
