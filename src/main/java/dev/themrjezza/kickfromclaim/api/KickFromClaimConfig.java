package dev.themrjezza.kickfromclaim.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface KickFromClaimConfig extends Reloadable {
    long getClickKickDurationMs();

    CompletableFuture<Boolean> setClickKickDurationMs(long durationMs);

    boolean getKickToWorldSpawn();

    CompletableFuture<Boolean> setKickToWorldSpawn(boolean enabled);

    boolean getKickToLocationEnabled();

    CompletableFuture<Boolean> setKickToLocationEnabled(boolean enabled);

    boolean getKickAnimalsToOwners();

    CompletableFuture<Boolean> setAnimalsToOwners(boolean enabled);

    boolean getAnimalsMustBeSitting();

    CompletableFuture<Boolean> setAnimalsMustBeSitting(boolean enabled);

    @Nullable
    Location getKickToLocation();

    CompletableFuture<Boolean> setKickToLocation(@Nullable Location location);

    @Nullable
    Location resolveConfiguredKickLocation(@NotNull World world);
}
