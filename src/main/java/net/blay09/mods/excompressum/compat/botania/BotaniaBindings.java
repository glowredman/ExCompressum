package net.blay09.mods.excompressum.compat.botania;

import net.blay09.mods.excompressum.block.ManaSieveBlock;
import net.blay09.mods.excompressum.item.ManaHammerItem;
import net.blay09.mods.excompressum.tile.ManaSieveTileEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.ManaItemHandler;

import javax.annotation.Nullable;

class BotaniaBindings {

    public static ResourceLocation RED_STRING_RELAY = new ResourceLocation("botania", "red_string_relay");

    public static IItemTier getManaSteelItemTier() {
        return BotaniaAPI.instance().getManasteelItemTier();
    }

    public static boolean requestManaExactForTool(ItemStack stack, PlayerEntity player, int mana, boolean remove) {
        return ManaItemHandler.instance().requestManaExactForTool(stack, player, mana, remove);
    }

    public static Block createOrechidBlock() {
        AbstractBlock.Properties properties = AbstractBlock.Properties.from(Blocks.POPPY);
        if (isBotaniaLoaded()) {
            return new EvolvedOrechidBlock(properties);
        } else {
            return new Block(properties);
        }
    }

    @Nullable
    public static EvolvedOrechidTileEntity createOrechidTileEntity() {
        if (isBotaniaLoaded()) {
            return new EvolvedOrechidTileEntity();
        } else {
            return null;
        }
    }

    @Nullable
    public static ManaSieveTileEntity createManaSieveTileEntity() {
        if (isBotaniaLoaded()) {
            return new ManaSieveTileEntity();
        } else {
            return null;
        }
    }

    public static Block createManaSieveBlock() {
        if (isBotaniaLoaded()) {
            return new ManaSieveBlock();
        } else {
            return new Block(AbstractBlock.Properties.from(Blocks.IRON_BLOCK));
        }
    }

    private static boolean isBotaniaLoaded() {
        return ModList.get().isLoaded("botania");
    }

    public static Item createManaHammerItem(Item.Properties properties) {
        return new ManaHammerItem(properties);
    }
}
