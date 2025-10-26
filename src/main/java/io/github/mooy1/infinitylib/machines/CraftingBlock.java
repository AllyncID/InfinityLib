package io.github.mooy1.infinitylib.machines;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import lombok.Setter;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack; // DIUBAH: Impor
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
// import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack; // DIHAPUS
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemStackSnapshot;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;

@ParametersAreNonnullByDefault
public class CraftingBlock extends MenuBlock {

    // DIUBAH: Menggunakan SlimefunItemStack dan menambahkan .item()
    public static final ItemStack CLICK_TO_CRAFT = new SlimefunItemStack(
            "CLICK_TO_CRAFT", // ID unik
            Material.LIME_STAINED_GLASS_PANE,
            "&aClick To Craft!"
    ).item();

    @Setter
    protected MachineLayout layout = MachineLayout.CRAFTING_DEFAULT;
    private final List<CraftingBlockRecipe> recipes = new ArrayList<>();

    public CraftingBlock(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    protected void craft(Block b, BlockMenu menu, Player p) {
        // DIUBAH: Menggunakan getter
        int[] slots = layout.getInputSlots();
        ItemStack[] input = new ItemStack[slots.length];
        for (int i = 0; i < slots.length; i++) {
            input[i] = menu.getItemInSlot(slots[i]);
        }

        CraftingBlockRecipe recipe = getOutput(input);

        if (recipe != null) {
            if (recipe.check(p)) {
                // DIUBAH: Menggunakan getter
                if (menu.fits(recipe.output, layout.getOutputSlots())) {
                    ItemStack output = recipe.output.clone();
                    onSuccessfulCraft(menu, output);
                    // DIUBAH: Menggunakan getter
                    menu.pushItem(output, layout.getOutputSlots());
                    recipe.consume(input);
                    p.sendMessage(ChatColor.GREEN + "Successfully Crafted: " + ItemUtils.getItemName(output));
                } else {
                    p.sendMessage(ChatColor.GOLD + "Not Enough Room!");
                }
            }
        } else {
            p.sendMessage(ChatColor.RED + "Invalid Recipe!");
        }
    }

    protected void onSuccessfulCraft(BlockMenu menu, ItemStack toOutput) {

    }

    @Override
    protected void setup(BlockMenuPreset preset) {
        // DIUBAH: Menggunakan getter untuk semua akses layout
        preset.drawBackground(OUTPUT_BORDER, layout.getOutputBorder());
        preset.drawBackground(INPUT_BORDER, layout.getInputBorder());
        preset.drawBackground(BACKGROUND_ITEM, layout.getBackground());
        preset.addItem(layout.getStatusSlot(), CLICK_TO_CRAFT, ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    protected void onNewInstance(BlockMenu menu, Block b) {
        // DIUBAH: Menggunakan getter
        menu.addMenuClickHandler(layout.getStatusSlot(), (player, i, itemStack, clickAction) -> {
            craft(b, menu, player);
            return false;
        });
    }

    @Nonnull
    public final CraftingBlock addRecipe(ItemStack output, ItemStack... inputs) {
        if (inputs.length == 0) {
            throw new IllegalArgumentException("Cannot add recipe with no input!");
        }
        CraftingBlockRecipe recipe = new CraftingBlockRecipe(output, inputs);
        recipes.add(recipe);
        return this;
    }

    @Nonnull
    public final CraftingBlock addRecipesFrom(MachineRecipeType recipeType) {
        recipeType.sendRecipesTo((in, out) -> addRecipe(out, in));
        return this;
    }

    @Nullable
    protected final CraftingBlockRecipe getOutput(ItemStack[] input) {
        ItemStackSnapshot[] snapshots = ItemStackSnapshot.wrapArray(input);
        for (CraftingBlockRecipe recipe : recipes) {
            if (recipe.check(snapshots)) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    protected final int[] getInputSlots(DirtyChestMenu menu, ItemStack input) {
        return new int[0];
    }

    @Override
    protected final int[] getInputSlots() {
        // DIUBAH: Menggunakan getter
        return layout.getInputSlots();
    }

    @Override
    protected final int[] getOutputSlots() {
        // DIUBAH: Menggunakan getter
        return layout.getOutputSlots();
    }

}
