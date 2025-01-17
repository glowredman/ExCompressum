package net.blay09.mods.excompressum.compat.nei;

import java.util.List;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import net.blay09.mods.excompressum.ExCompressum;
import net.blay09.mods.excompressum.compat.IAddon;
import net.blay09.mods.excompressum.compat.INEIAddon;
import net.minecraft.item.ItemStack;

public class NEIExCompressumConfig implements IConfigureNEI {
    @Override
    public void loadConfig() {
        RecipeHandlerCompressedHammer handlerHammer = new RecipeHandlerCompressedHammer();
        API.registerRecipeHandler(handlerHammer);
        API.registerUsageHandler(handlerHammer);

        RecipeHandlerHeavySieve handlerSieve = new RecipeHandlerHeavySieve();
        API.registerRecipeHandler(handlerSieve);
        API.registerUsageHandler(handlerSieve);

        RecipeHandlerComposting handlerComposting = new RecipeHandlerComposting();
        API.registerRecipeHandler(handlerComposting);
        API.registerUsageHandler(handlerComposting);

        RecipeHandlerBarrelProcess handlerBarrel = new RecipeHandlerBarrelProcess();
        API.registerRecipeHandler(handlerBarrel);
        API.registerUsageHandler(handlerBarrel);

        for(IAddon addon : ExCompressum.instance.addons) {
            if(addon instanceof INEIAddon) {
                ((INEIAddon) addon).loadNEIConfig();
            }
        }
    }

    @Override
    public String getName() {
        return "Ex Compressum";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    public static boolean contains(List<PositionedStack> list, ItemStack stack) {
        for(PositionedStack pStack : list) {
            if(NEIServerUtils.areStacksIdentical(stack, pStack.item)) {
                return true;
            }
        }
        return false;
    }
}
