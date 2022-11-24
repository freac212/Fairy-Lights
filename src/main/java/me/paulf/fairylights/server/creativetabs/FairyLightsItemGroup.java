package me.paulf.fairylights.server.creativetabs;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.item.FLItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class FairyLightsItemGroup extends CreativeModeTab {
    public FairyLightsItemGroup() {
        super(FairyLights.ID);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(FLItems.HANGING_LIGHTS.get());
    }
}
