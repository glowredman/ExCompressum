package net.blay09.mods.excompressum.tile;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;
import net.blay09.mods.excompressum.ExCompressum;
import net.blay09.mods.excompressum.registry.CompressedRecipeRegistry;
import net.blay09.mods.excompressum.registry.data.CompressedRecipe;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityAutoCompressor extends TileEntity implements ISidedInventory, IEnergyHandler {

    private final EnergyStorage storage = new EnergyStorage(32000);
    private final Multiset<CompressedRecipe> inputItems = HashMultiset.create();
    private final ItemStack[] inventory = new ItemStack[getSizeInventory()];
    private ItemStack currentStack;

    private float progress;

    @Override
    public void updateEntity() {
        int effectiveEnergy = getEffectiveEnergy();
        if (storage.getEnergyStored() > effectiveEnergy) {
            if (currentStack == null) {
                inputItems.clear();
                for (int i = 0; i < 12; i++) {
                    if (inventory[i] != null) {
                        CompressedRecipe compressedRecipe = CompressedRecipeRegistry.getRecipe(inventory[i]);
                        if (compressedRecipe != null) {
                            inputItems.add(compressedRecipe, inventory[i].stackSize);
                        }
                    }
                }
                for (CompressedRecipe compressedRecipe : inputItems.elementSet()) {
                    ItemStack sourceStack = compressedRecipe.getSourceStack();
                    if (inputItems.count(compressedRecipe) >= sourceStack.stackSize) {
                        int space = 0;
                        for(int i = 12; i < inventory.length; i++) {
                            if(inventory[i] == null) {
                                space = 64;
                            } else if(isItemEqualWildcard(inventory[i], compressedRecipe.getResultStack())) {
                                space += inventory[i].getMaxStackSize() - inventory[i].stackSize;
                            }
                            if(space >= compressedRecipe.getResultStack().stackSize) {
                                break;
                            }
                        }
                        if(space < compressedRecipe.getResultStack().stackSize) {
                            continue;
                        }
                        int count = sourceStack.stackSize;
                        for (int i = 0; i < 12; i++) {
                            if (inventory[i] != null && isItemEqualWildcard(inventory[i], sourceStack)) {
                                if (inventory[i].stackSize >= count) {
                                    inventory[i].stackSize -= count;
                                    if (inventory[i].stackSize == 0) {
                                        inventory[i] = null;
                                    }
                                    count = 0;
                                    break;
                                } else {
                                    count -= inventory[i].stackSize;
                                    inventory[i] = null;
                                }
                            }
                        }
                        if (count <= 0) {
                            currentStack = sourceStack.copy();
                            progress = 0f;
                        }
                        break;
                    }
                }
            } else {
                storage.extractEnergy(effectiveEnergy, false);
                progress = Math.min(1f, progress + getEffectiveSpeed());
                if (progress >= 1) {
                    if (!worldObj.isRemote) {
                        CompressedRecipe compressedRecipe = CompressedRecipeRegistry.getRecipe(currentStack);
                        if (compressedRecipe != null) {
                            ItemStack resultStack = compressedRecipe.getResultStack().copy();
                            if (!addItemToOutput(resultStack)) {
                                EntityItem entityItem = new EntityItem(worldObj, xCoord + 0.5, yCoord + 1.5, zCoord + 0.5, resultStack);
                                double motion = 0.05;
                                entityItem.motionX = worldObj.rand.nextGaussian() * motion;
                                entityItem.motionY = 0.2;
                                entityItem.motionZ = worldObj.rand.nextGaussian() * motion;
                                worldObj.spawnEntityInWorld(entityItem);
                            }
                        }
                    }
                    currentStack = null;
                    progress = 0f;
                }
            }
        }
    }

    private boolean isItemEqualWildcard(ItemStack itemStack, ItemStack otherStack) {
        if(!ItemStack.areItemStackTagsEqual(itemStack, otherStack)) {
            return false;
        }
        if(itemStack.isItemEqual(otherStack) || itemStack.getItem() == otherStack.getItem() && (itemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE || otherStack.getItemDamage() == OreDictionary.WILDCARD_VALUE)) {
            return true;
        }
        return false;
    }

    private boolean addItemToOutput(ItemStack itemStack) {
        int firstEmptySlot = -1;
        for (int i = 12; i < getSizeInventory(); i++) {
            if (inventory[i] == null) {
                if (firstEmptySlot == -1) {
                    firstEmptySlot = i;
                }
            } else {
                if (inventory[i].stackSize + itemStack.stackSize <= inventory[i].getMaxStackSize() && isItemEqualWildcard(inventory[i], itemStack)) {
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
        return ExCompressum.autoCompressorEnergy;
    }

    public float getEffectiveSpeed() {
        return ExCompressum.autoCompressorSpeed;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        currentStack = ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag("CurrentStack"));
        progress = tagCompound.getFloat("Progress");
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

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        if (currentStack != null) {
            tagCompound.setTag("CurrentStack", currentStack.writeToNBT(new NBTTagCompound()));
        }
        tagCompound.setFloat("Progress", progress);
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

    @Override
    public int receiveEnergy(ForgeDirection side, int maxReceive, boolean simulate) {
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
        return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 };
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, int side) {
        return isItemValidForSlot(slot, itemStack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
        return slot >= 12;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
        return slot < 12 && CompressedRecipeRegistry.getRecipe(itemStack) != null;
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
        return 24;
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

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public float getEnergyPercentage() {
        return (float) storage.getEnergyStored() / (float) storage.getMaxEnergyStored();
    }

    public ItemStack getCurrentStack() {
        return currentStack;
    }
}
