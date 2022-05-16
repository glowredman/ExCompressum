package net.blay09.mods.excompressum.tile;

import java.util.Collection;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import exnihilo.registries.HammerRegistry;
import exnihilo.registries.helpers.Smashable;
import exnihilo.utils.ItemInfo;
import net.blay09.mods.excompressum.ExCompressum;
import net.blay09.mods.excompressum.handler.VanillaPacketHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityAutoHammer extends TileEntity implements ISidedInventory, IEnergyHandler {

    private static final int UPDATE_INTERVAL = 20;

    private final EnergyStorage storage = new EnergyStorage(32000);
    private ItemStack[] inventory = new ItemStack[getSizeInventory()];
    private ItemStack currentStack;

    private int ticksSinceUpdate;
    private boolean isDirty;
    private float progress;

    @Override
    public void updateEntity() {
        ticksSinceUpdate++;
        if (ticksSinceUpdate > UPDATE_INTERVAL) {
            if (isDirty) {
                VanillaPacketHandler.sendTileEntityUpdate(this);
                isDirty = false;
            }
            ticksSinceUpdate = 0;
        }
        int effectiveEnergy = getEffectiveEnergy();
        if (storage.getEnergyStored() >= effectiveEnergy) {
            if (currentStack == null) {
                if (inventory[0] != null && isRegistered(inventory[0])) {
                    boolean foundSpace = false;
                    for(int i = 1; i < inventory.length - 2; i++) {
                        if(inventory[i] == null) {
                            foundSpace = true;
                        }
                    }
                    if(!foundSpace) {
                        return;
                    }
                    currentStack = inventory[0].splitStack(1);
                    if (inventory[0].stackSize == 0) {
                        inventory[0] = null;
                    }
                    storage.extractEnergy(effectiveEnergy, false);
                    VanillaPacketHandler.sendTileEntityUpdate(this);
                    progress = 0f;
                }
            } else {
                storage.extractEnergy(effectiveEnergy, false);
                progress += getEffectiveSpeed();
                isDirty = true;
                if (progress >= 1) {
                    if(worldObj.rand.nextFloat() <= ExCompressum.autoHammerDecay) {
                        if (inventory[21] != null) {
                            if(inventory[21].attemptDamageItem(1, worldObj.rand)) {
                                inventory[21] = null;
                            }
                        }
                        if (inventory[22] != null) {
                            if(inventory[22].attemptDamageItem(1, worldObj.rand)) {
                                inventory[22] = null;
                            }
                        }
                    }
                    if (!worldObj.isRemote) {
                        Collection<Smashable> rewards = getSmashables(currentStack);
                        if (rewards != null && !rewards.isEmpty()) {
                            for (Smashable reward : rewards) {
                                if (worldObj.rand.nextFloat() <= reward.chance + (reward.luckMultiplier * getEffectiveLuck())) {
                                    ItemStack rewardStack = new ItemStack(reward.item, 1, reward.meta);
                                    if (!addItemToOutput(rewardStack)) {
                                        EntityItem entityItem = new EntityItem(worldObj, xCoord + 0.5, yCoord + 1.5, zCoord + 0.5, rewardStack);
                                        double motion = 0.05;
                                        entityItem.motionX = worldObj.rand.nextGaussian() * motion;
                                        entityItem.motionY = 0.2;
                                        entityItem.motionZ = worldObj.rand.nextGaussian() * motion;
                                        worldObj.spawnEntityInWorld(entityItem);
                                    }
                                }
                            }
                        }
                    } else {
                        spawnCrushParticles();
                    }
                    progress = 0f;
                    currentStack = null;
                }
            }
        }
    }

    protected Collection<Smashable> getSmashables(ItemStack itemStack) {
        return HammerRegistry.getRewards(new ItemInfo(itemStack));
    }

    public boolean isRegistered(ItemStack itemStack) {
        return HammerRegistry.registered(itemStack);
    }

    private boolean addItemToOutput(ItemStack itemStack) {
        int firstEmptySlot = -1;
        for (int i = 1; i < getSizeInventory() - 2; i++) {
            if (inventory[i] == null) {
                if(firstEmptySlot == -1){
                    firstEmptySlot = i;
                }
            } else {
                if (inventory[i].stackSize + itemStack.stackSize <= inventory[i].getMaxStackSize() && inventory[i].isItemEqual(itemStack) && ItemStack.areItemStackTagsEqual(inventory[i], itemStack)) {
                    inventory[i].stackSize += itemStack.stackSize;
                    return true;
                }
            }
        }
        if (firstEmptySlot != -1) {
            inventory[firstEmptySlot] = itemStack;
            return true;
        }
        return false;
    }

    public int getEffectiveEnergy() {
        return ExCompressum.autoHammerEnergy;
    }

    public float getSpeedBoost() {
        float boost = 1f;
        if(inventory[21] != null && isHammerUpgrade(inventory[21])) {
            boost += 1f;
        }
        if(inventory[22] != null && isHammerUpgrade(inventory[22])) {
            boost += 1f;
        }
        return boost;
    }

    public float getEffectiveSpeed() {
        return ExCompressum.autoHammerSpeed * getSpeedBoost();
    }

    public float getEffectiveLuck() {
        return 0f;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        storage.readFromNBT(tagCompound);
        NBTTagList items = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemCompound = items.getCompoundTagAt(i);
            int slot = itemCompound.getByte("Slot");
            if (slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(itemCompound);
            }
        }
    }

    private void readFromNBTSynced(NBTTagCompound tagCompound) {
        currentStack = ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag("CurrentStack"));
        progress = tagCompound.getFloat("Progress");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        writeToNBTSynced(tagCompound);
        storage.writeToNBT(tagCompound);
        NBTTagList items = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound itemCompound = new NBTTagCompound();
                itemCompound.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(itemCompound);
                items.appendTag(itemCompound);
            }
        }
        tagCompound.setTag("Items", items);
    }

    private void writeToNBTSynced(NBTTagCompound tagCompound) {
        if (currentStack != null) {
            tagCompound.setTag("CurrentStack", currentStack.writeToNBT(new NBTTagCompound()));
        }
        tagCompound.setFloat("Progress", progress);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        writeToNBTSynced(tagCompound);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, blockMetadata, tagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBTSynced(pkt.func_148857_g());
    }

    @SideOnly(Side.CLIENT)
    private void spawnCrushParticles() {
        if (currentStack != null) {
            for (int i = 0; i < 10; i++) {
                EntityDiggingFX particle = new EntityDiggingFX(getWorldObj(), xCoord + 0.5, yCoord + 5d / 16d, zCoord + 0.5, 0, 0, 0, Block.getBlockFromItem(currentStack.getItem()), currentStack.getItemDamage());
                particle.setVelocity((worldObj.rand.nextDouble() / 2) - 0.25, 0, (worldObj.rand.nextDouble() / 2) - 0.25);
                Minecraft.getMinecraft().effectRenderer.addEffect(particle);
            }
        }
    }

    @Override
    public int receiveEnergy(ForgeDirection side, int maxReceive, boolean simulate) {
        if(!simulate) {
            isDirty = true;
        }
        return storage.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(ForgeDirection side, int maxExtract, boolean simulate) {
        return storage.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored(ForgeDirection side) {
        return storage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection side) {
        return storage.getMaxEnergyStored();
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection side) {
        return true;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22};
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, int side) {
        return isItemValidForSlot(slot, itemStack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
        return slot >= 1;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
        return
                (slot == 0 && isRegistered(itemStack))
                || ((slot == 21 || slot == 22) && isHammerUpgrade(itemStack));
    }

    public boolean isHammerUpgrade(ItemStack itemStack) {
        return itemStack.getItem() == GameRegistry.findItem("exnihilo", "hammer_diamond");
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public int getSizeInventory() {
        return 23;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (inventory[slot] != null) {
            if (inventory[slot].stackSize <= amount) {
                ItemStack itemStack = inventory[slot];
                inventory[slot] = null;
                return itemStack;
            }
            ItemStack itemStack = inventory[slot].splitStack(amount);
            if (inventory[slot].stackSize == 0) {
                inventory[slot] = null;
            }
            return itemStack;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack) {
        inventory[slot] = itemStack;
    }

    @Override
    public String getInventoryName() {
        return null;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && entityPlayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64;
    }

    public void setEnergyStored(int energyStored) {
        storage.setEnergyStored(energyStored);
    }

    public boolean isProcessing() {
        return progress > 0f;
    }

    public float getProgress() {
        return progress;
    }

    public float getEnergyPercentage() {
        return (float) storage.getEnergyStored() / (float) storage.getMaxEnergyStored();
    }

    public ItemStack getCurrentStack() {
        return currentStack;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }
}
