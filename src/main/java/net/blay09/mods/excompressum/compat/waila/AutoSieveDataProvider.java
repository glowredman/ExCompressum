package net.blay09.mods.excompressum.compat.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.blay09.mods.excompressum.tile.TileEntityAutoSieve;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class AutoSieveDataProvider implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> list, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return list;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> list, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if(accessor.getTileEntity() instanceof TileEntityAutoSieve) {
            TileEntityAutoSieve tileEntity = (TileEntityAutoSieve) accessor.getTileEntity();
            if(tileEntity.getCustomSkin() != null) {
                list.add(StatCollector.translateToLocalFormatted("waila.excompressum:sieveSkin", tileEntity.getCustomSkin().getName()));
            }
            if(tileEntity.getSpeedBoost() > 1f) {
                list.add(StatCollector.translateToLocalFormatted("waila.excompressum:speedBoost", tileEntity.getSpeedBoost()));
            }
            if(tileEntity.getEffectiveLuck() > 1) {
                list.add(StatCollector.translateToLocalFormatted("waila.excompressum:luckBonus", tileEntity.getEffectiveLuck() - 1));
            }
        }
        return list;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> list, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return list;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP entityPlayer, TileEntity tileEntity, NBTTagCompound tagCompound, World world, int x, int y, int z) {
        if(tileEntity != null) {
            tileEntity.writeToNBT(tagCompound);
        }
        return tagCompound;
    }

}
