package net.blay09.mods.excompressum.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class GroupedRegistry<
        TGroup extends RegistryGroup,
        TGroupOverride extends RegistryOverride,
        TEntryOverride extends RegistryOverride,
        TEntry extends RegistryEntry,
        TData extends GroupedRegistryData<TGroup, TGroupOverride, TEntryOverride, TEntry>> extends ExRegistry<TData> {

    private final Map<ResourceLocation, TGroupOverride> groupOverrides = new HashMap<>();
    private final Map<ResourceLocation, TEntryOverride> entryOverrides = new HashMap<>();
    private final Map<ResourceLocation, RegistryGroupInitializer<TGroupOverride>> groupInitializers = new HashMap<>();

    public GroupedRegistry(String registryName) {
        super(registryName);
    }

    @Override
    protected void reset() {
        groupOverrides.clear();
        entryOverrides.clear();
        groupInitializers.clear();
    }

    @Override
    protected void load(TData data) {
        if (data.getModId() != null && !data.getModId().equals("minecraft") && !ModList.get().isLoaded(data.getModId())) {
            return;
        }

        RegistryGroup group = data.getGroup();
        if (group != null) {
            groupInitializers.put(group.getId(), new RegistryGroupInitializer<>(group, override -> {
                loadData(data, override);
            }));
        } else {
            loadData(data, null);
        }
    }

    private void loadData(TData data, @Nullable TGroupOverride groupOverride) {
        if (data.getGroupOverrides() != null) {
            groupOverrides.putAll(data.getGroupOverrides());
        }

        if (data.getEntryOverrides() != null) {
            entryOverrides.putAll(data.getEntryOverrides());
        }

        if (data.getCustomEntries() != null) {
            for (TEntry customEntry : data.getCustomEntries()) {
                TEntryOverride entryOverride = getEntryOverride(customEntry.getId());
                boolean enabled = customEntry.isEnabledByDefault();
                if (entryOverride != null) {
                    enabled = entryOverride.isEnabled();
                }

                if (enabled) {
                    loadEntry(customEntry, groupOverride, entryOverride);
                }
            }
        }
    }

    protected abstract void loadEntry(TEntry entry, @Nullable TGroupOverride groupOverride, @Nullable TEntryOverride entryOverride);

    @Override
    protected void loadingFinished() {
        super.loadingFinished();
        for (RegistryGroupInitializer<TGroupOverride> groupInitializer : groupInitializers.values()) {
            RegistryGroup group = groupInitializer.getGroup();
            TGroupOverride override = getGroupOverride(group.getId());
            boolean enabled = group.isEnabledByDefault();
            if (override != null) {
                enabled = override.isEnabled();
            }

            if (enabled) {
                groupInitializer.getInitializer().accept(override);
            }
        }
    }

    @Nullable
    protected TGroupOverride getGroupOverride(ResourceLocation groupId) {
        return groupOverrides.get(groupId);
    }

    @Nullable
    protected TEntryOverride getEntryOverride(ResourceLocation entryId) {
        return entryOverrides.get(entryId);
    }
}
