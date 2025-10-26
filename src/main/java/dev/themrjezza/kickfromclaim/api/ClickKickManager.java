package dev.themrjezza.kickfromclaim.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ClickKickManager {
    boolean startClickKick(@NotNull Player player);

    boolean stopClickKick(@NotNull Player player);

    boolean isClickKickEnabled(@NotNull Player player);
}
