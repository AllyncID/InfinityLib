package io.github.mooy1.otheraddon;

import io.github.mooy1.infinitylib.core.Environment;
import io.github.mooy1.infinitylib.core.MockAddon;

public final class MockOtherAddon extends MockAddon {

    public MockOtherAddon(Environment environment) {
        // Memanggil constructor MockAddon modern
        super(null); // atau bisa buat MockAddonTest khusus jika perlu
    }

    @Override
    protected void enable() {
        // Logika enable addon
    }

    @Override
    protected void disable() {
        // Logika disable addon
    }
}
