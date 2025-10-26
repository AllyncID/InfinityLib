package io.github.mooy1.infinitylib.core;

// ... (import-import lain)
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
        import org.bukkit.plugin.java.JavaPlugin;

import io.github.mooy1.infinitylib.commands.AddonCommand;
import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.GitHubBuildsUpdater;

@SuppressWarnings("deprecation") // Ditambahkan di level class untuk constructor deprecated
@ParametersAreNonnullByDefault
public abstract class AbstractAddon extends JavaPlugin implements SlimefunAddon {

    private static AbstractAddon instance;

    private final GitHubBuildsUpdater updater;
    private final Environment environment;
    private final String githubUserName;
    private final String githubRepo;
    private final String autoUpdateBranch;
    private final String autoUpdateKey;
    private final String bugTrackerURL;

    private AddonCommand command;
    private AddonConfig config;
    private int slimefunTickCount;
    private boolean autoUpdatesEnabled;
    private boolean disabling;
    private boolean enabling;
    private boolean loading;

    /**
     * Live Addon Constructor
     */
    public AbstractAddon(String githubUserName, String githubRepo, String autoUpdateBranch, String autoUpdateKey, Environment libraryTesting) {
        boolean official = getDescription().getVersion().matches("DEV - \\d+ \\(git \\w+\\)");
        this.updater = official ? new GitHubBuildsUpdater(this, getFile(),
                githubUserName + "/" + githubRepo + "/" + autoUpdateBranch) : null;
        this.environment = Environment.LIVE;
        this.githubUserName = githubUserName;
        this.autoUpdateBranch = autoUpdateBranch;
        this.githubRepo = githubRepo;
        this.autoUpdateKey = autoUpdateKey;
        this.bugTrackerURL = "https://github.com/" + githubUserName + "/" + githubRepo + "/issues";
        // validate(); // Dihapus
    }


    @Override
    public final void onLoad() {
        if (loading) {
            throw new IllegalStateException(getName() + " is already loading! Do not call super.onLoad()!");
        }

        loading = true;

        // Load
        try {
            load();
        }
        catch (RuntimeException e) {
            handle(e);
        }
        finally {
            loading = false;
        }
    }

    @Override
    public final void onEnable() {
        if (enabling) {
            throw new IllegalStateException(getName() + " is already enabling! Do not call super.onEnable()!");
        }

        enabling = true;

        // Set static instance
        instance = this;

        // This is used to mark when the config is broken, so we should always auto update
        boolean brokenConfig = false;

        // Create Config
        try {
            config = new AddonConfig("config.yml");
        }
        catch (RuntimeException e) {
            brokenConfig = true;
            e.printStackTrace();
        }

        // Validate autoUpdateKey
        if (autoUpdateKey == null) {
            brokenConfig = true;
            handle(new IllegalStateException("Null auto update key"));
        }
        else if (autoUpdateKey.isEmpty()) {
            brokenConfig = true;
            handle(new IllegalStateException("Empty auto update key!"));
        }
        else if (!brokenConfig && !config.getDefaults().contains(autoUpdateKey, true)) {
            brokenConfig = true;
            handle(new IllegalStateException("Auto update key missing from the default config!"));
        }

        // Auto update if enabled
        if (updater != null) {
            if (brokenConfig) {
                updater.start();
            }
            else if (config.getBoolean(autoUpdateKey)) {
                autoUpdatesEnabled = true;
                updater.start();
            }
        }

        // Get plugin command
        PluginCommand pluginCommand = getCommand(getName());
        if (pluginCommand != null) {
            command = new AddonCommand(pluginCommand);
        }

        // Create total tick count
        Scheduler.repeat(Slimefun.getTickerTask().getTickRate(), () -> slimefunTickCount++);

        // Call addon enable
        try {
            enable();
        }
        catch (RuntimeException e) {
            handle(e);
        }
        finally {
            enabling = false;
        }
    }

    @Override
    public final void onDisable() {
        if (disabling) {
            throw new IllegalStateException(getName() + " is already disabling! Do not call super.onDisable()!");
        }

        disabling = true;

        try {
            disable();
        }
        catch (RuntimeException e) {
            handle(e);
        }
        finally {
            disabling = false;
            instance = null;
            slimefunTickCount = 0;
            command = null;
            config = null;
        }
    }

    /**
     * Throws an exception if in a test environment, otherwise just logs the stacktrace so that the plugin functions
     */
    private void handle(RuntimeException e) {
        switch (this.environment) {
            case TESTING:
                throw e;
            case LIVE:
                e.printStackTrace();
        }
    }

    /**
     * Called when the plugin is loaded
     */
    protected void load() {

    }

    /**
     * Called when the plugin is enabled
     */
    protected abstract void enable();

    /**
     * Called when the plugin is disabled
     */
    protected abstract void disable();

    /**
     * Gets the command of the same name as this addon
     */
    @Nonnull
    protected final AddonCommand getAddonCommand() {
        return Objects.requireNonNull(instance().command, "Command '" + getName() + "' missing from plugin.yml!");
    }

    /**
     * Returns whether auto updates are enabled, for use in metrics
     */
    protected final boolean autoUpdatesEnabled() {
        return instance().autoUpdatesEnabled;
    }

    @Nonnull
    @Override
    public final JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nonnull
    @Override
    public final String getBugTrackerURL() {
        return bugTrackerURL;
    }

    @Nonnull
    @Override
    public final AddonConfig getConfig() {
        return instance().config;
    }

    @Override
    public final void reloadConfig() {
        instance().config.reload();
    }

    @Override
    public final void saveConfig() {
        instance().config.save();
    }

    @Override
    public final void saveDefaultConfig() {
        // Do nothing, it's covered in onEnable()
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends AbstractAddon> T instance() {
        return (T) Objects.requireNonNull(instance, "Addon is not enabled!");
    }

    @Nonnull
    public static AddonConfig config() {
        return instance().getConfig();
    }

    @SuppressWarnings("unused")
    public static void log(Level level, String... messages) {
        Logger logger = instance().getLogger();
        for (String msg : messages) {
            logger.log(level, msg);
        }
    }

    /**
     * Returns the total number of Slimefun ticks that have occurred
     */
    public static int slimefunTickCount() {
        return instance().slimefunTickCount;
    }

    /**
     * Returns the current running environment
     */
    @Nonnull
    public static Environment environment() {
        return instance().environment;
    }

    /**
     * Creates a NameSpacedKey from the given string
     */
    @Nonnull
    public static NamespacedKey createKey(String s) {
        return new NamespacedKey(instance(), s);
    }

}

