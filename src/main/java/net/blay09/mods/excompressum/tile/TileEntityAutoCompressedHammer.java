package net.blay09.mods.excompressum.tile;

import java.util.Collection;

import exnihilo.registries.helpers.Smashable;
import net.blay09.mods.excompressum.ExCompressum;
import net.blay09.mods.excompressum.ModItems;
import net.blay09.mods.excompressum.registry.CompressedHammerRegistry;
import net.minecraft.item.ItemStack;

public class TileEntityAutoCompressedHammer extends TileEntityAutoHammer {

    @Override
    public int getEffectiveEnergy() {
        return ExCompressum.autoCompressedHammerEnergy;
    }

    @Override
    public float getEffectiveSpeed() {
        return ExCompressum.autoCompressedHammerSpeed * getSpeedBoost();
    }

    @Override
    public boolean isRegistered(ItemStack itemStack) {
        return CompressedHammerRegistry.isRegistered(itemStack);
    }

    @Override
    public boolean isHammerUpgrade(ItemStack itemStack) {
        return itemStack.getItem() == ModItems.compressedHammerDiamond;
    }

    @Override
    protected Collection<Smashable> getSmashables(ItemStack itemStack) {
        return CompressedHammerRegistry.getSmashables(itemStack);
    }

}
