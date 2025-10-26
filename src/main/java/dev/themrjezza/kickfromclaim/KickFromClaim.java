package dev.themrjezza.kickfromclaim;

import dev.themrjezza.kickfromclaim.api.ClickKickManager;
import dev.themrjezza.kickfromclaim.api.KickFromClaimConfig;
import dev.themrjezza.kickfromclaim.commands.ClickKickFromClaimCommand;
import dev.themrjezza.kickfromclaim.commands.KickFromClaimCommand;
import dev.themrjezza.kickfromclaim.commands.ReloadKickFromClaimCommand;
import dev.themrjezza.kickfromclaim.config.KickFromClaimSettings;
import dev.themrjezza.kickfromclaim.config.MessageCache;
import org.bukkit.plugin.java.JavaPlugin;

public class KickFromClaim extends JavaPlugin {

    private final MessageCache messageCache = new MessageCache(this);
    private final KickFromClaimSettings config = new KickFromClaimSettings(this);
    private final ClickKickEngine ckManager = new ClickKickEngine(messageCache, getConfiguration());

    @Override
    public void onEnable() {
        messageCache.reload();
        config.reload();
        ckManager.start(this);

        new KickFromClaimCommand(this, messageCache, getConfiguration());
        new ClickKickFromClaimCommand(this, messageCache, getClickKickManager());
        new ReloadKickFromClaimCommand(this, messageCache, getConfiguration());
    }

    public ClickKickManager getClickKickManager() {
        return ckManager;
    }

    public KickFromClaimConfig getConfiguration() {
        return config;
    }
}