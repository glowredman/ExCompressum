package net.blay09.mods.excompressum.registry.compressedhammer;

/*public class CompressedHammerRegistryOld extends AbstractRegistry {

    @Override
    protected void registerDefaults(JsonObject defaults) {
        if (tryGetBoolean(defaults, "excompressum:compressed_sand", true)) {
            ItemStack dustBlock = ExRegistro.getNihiloItem(ExNihiloProvider.NihiloItems.DUST);
            if (!dustBlock.isEmpty()) {
                CompressedHammerRegistryEntry entry = new CompressedHammerRegistryEntry(ModBlocks.compressedBlocks[CompressedBlockType.SAND.ordinal()].getDefaultState());
                entry.addReward(new CompressedHammerReward(ItemHandlerHelper.copyStackWithSize(dustBlock, 9), 1f, 0f));
                add(entry);
            }
        }

        if (tryGetBoolean(defaults, "excompressum:compressed_netherrack", true)) {
            ItemStack netherGravelBlock = ExRegistro.getNihiloItem(ExNihiloProvider.NihiloItems.NETHER_GRAVEL);
            if (!netherGravelBlock.isEmpty()) {
                CompressedHammerRegistryEntry entry = new CompressedHammerRegistryEntry(ModBlocks.compressedBlocks[CompressedBlockType.NETHERRACK.ordinal()].getDefaultState());
                entry.addReward(new CompressedHammerReward(ItemHandlerHelper.copyStackWithSize(netherGravelBlock, 9), 1f, 0f));
                add(entry);
            }
        }

        if (tryGetBoolean(defaults, "excompressum:compressed_end_stone", true)) {
            ItemStack enderGravelBlock = ExRegistro.getNihiloItem(ExNihiloProvider.NihiloItems.ENDER_GRAVEL);
            if (!enderGravelBlock.isEmpty()) {
                CompressedHammerRegistryEntry entry = new CompressedHammerRegistryEntry(ModBlocks.compressedBlocks[CompressedBlockType.END_STONE.ordinal()].getDefaultState());
                entry.addReward(new CompressedHammerReward(ItemHandlerHelper.copyStackWithSize(enderGravelBlock, 9), 1f, 0f));
                add(entry);
            }
        }

        if (ModList.get().isLoaded(Compat.EXTRAUTILS2)) {
            if (tryGetBoolean(defaults, "ExtraUtils2:CompressedCobblestone", true)) {
                ResourceLocation location = new ResourceLocation(Compat.EXTRAUTILS2, "compressedcobblestone");
                if (ForgeRegistries.BLOCKS.containsKey(location)) {
                    Block exUtilsBlock = ForgeRegistries.BLOCKS.getValue(location);
                    CompressedHammerRegistryEntry entry = new CompressedHammerRegistryEntry(exUtilsBlock.getDefaultState());
                    entry.addReward(new CompressedHammerReward(new ItemStack(Blocks.GRAVEL, 9), 1f, 0f));
                    add(entry);
                }
            }

            if (tryGetBoolean(defaults, "ExtraUtils2:CompressedGravel", true)) {
                ResourceLocation location = new ResourceLocation(Compat.EXTRAUTILS2, "compressedgravel");
                if (ForgeRegistries.BLOCKS.containsKey(location)) {
                    Block exUtilsBlock = ForgeRegistries.BLOCKS.getValue(location);
                    CompressedHammerRegistryEntry entry = new CompressedHammerRegistryEntry(exUtilsBlock.getDefaultState());
                    entry.addReward(new CompressedHammerReward(new ItemStack(Blocks.SAND, 9), 1f, 0f));
                    add(entry);
                }
            }

            if (tryGetBoolean(defaults, "ExtraUtils2:CompressedSand", true)) {
                ItemStack dustBlock = ExRegistro.getNihiloItem(ExNihiloProvider.NihiloItems.DUST);
                if (!dustBlock.isEmpty()) {
                    ResourceLocation location = new ResourceLocation(Compat.EXTRAUTILS2, "compressedsand");
                    if (ForgeRegistries.BLOCKS.containsKey(location)) {
                        Block exUtilsBlock = ForgeRegistries.BLOCKS.getValue(location);
                        CompressedHammerRegistryEntry entry = new CompressedHammerRegistryEntry(exUtilsBlock.getDefaultState());
                        entry.addReward(new CompressedHammerReward(ItemHandlerHelper.copyStackWithSize(dustBlock, 9), 1f, 0f));
                        add(entry);
                    }
                }
            }

            if (tryGetBoolean(defaults, "ExtraUtils2:CompressedNetherrack", true)) {
                ItemStack netherGravelBlock = ExRegistro.getNihiloItem(ExNihiloProvider.NihiloItems.NETHER_GRAVEL);
                if (!netherGravelBlock.isEmpty()) {
                    ResourceLocation location = new ResourceLocation(Compat.EXTRAUTILS2, "compressednetherrack");
                    if (ForgeRegistries.BLOCKS.containsKey(location)) {
                        Block exUtilsBlock = ForgeRegistries.BLOCKS.getValue(location);
                        CompressedHammerRegistryEntry entry = new CompressedHammerRegistryEntry(exUtilsBlock.getDefaultState());
                        entry.addReward(new CompressedHammerReward(ItemHandlerHelper.copyStackWithSize(netherGravelBlock, 9), 1f, 0f));
                        add(entry);
                    }
                }
            }
        }
    }

    @Override
    protected ReloadRegistryEvent getRegistryEvent() {
        return new ReloadRegistryEvent.CompressedHammer();
    }

}
*/