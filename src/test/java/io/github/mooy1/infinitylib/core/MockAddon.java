package io.github.mooy1.infinitylib.core;

import java.io.File;

import javax.annotation.Nullable;

import org.bukkit.plugin.PluginDescriptionFile;

import be.seeseemelk.mockbukkit.MockBukkit;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;

// Tambahkan @SuppressWarnings untuk constructor deprecated
@SuppressWarnings("deprecation")
public class MockAddon extends AbstractAddon {

    private final MockAddonTest test;

    /**
     * Constructor utama untuk MockAddon
     */
    public MockAddon(MockAddonTest test) {
        // Tentukan branch dan autoUpdateKey sesuai test
        super(
                "Mooy1",
                "InfinityLib",
                test == MockAddonTest.BAD_GITHUB_PATH ? "[!#$" : "master",
                test == MockAddonTest.MISSING_KEY ? "missing" : "auto-update",
                Environment.LIBRARY_TESTING
        );

        this.test = test;

        // Load Slimefun secara mock
        MockBukkit.load(Slimefun.class);
    }


    @Override
    protected void load() {
        if (test == MockAddonTest.THROW_EXCEPTION) {
            throw new RuntimeException();
        }
        else if (test == MockAddonTest.CALL_SUPER) {
            super.onLoad();
        }
    }

    @Override
    protected void enable() {
        if (test == MockAddonTest.THROW_EXCEPTION) {
            throw new RuntimeException();
        }
        else if (test == MockAddonTest.CALL_SUPER) {
            super.onEnable();
        }
    }

    @Override
    protected void disable() {
        if (test == MockAddonTest.THROW_EXCEPTION) {
            throw new RuntimeException();
        }
        else if (test == MockAddonTest.CALL_SUPER) {
            super.onDisable();
        }
    }

}

