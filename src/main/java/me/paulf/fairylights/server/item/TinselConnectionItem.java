package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.connection.ConnectionTypes;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.network.chat.Component;

import net.minecraft.world.item.Item.Properties;

public final class TinselConnectionItem extends ConnectionItem {
    public TinselConnectionItem(final Properties properties) {
        super(properties, ConnectionTypes.TINSEL_GARLAND);
    }

    @Override
    public Component getDisplayName(final ItemStack stack) {
        return DyeableItem.getDisplayName(stack, super.getDisplayName(stack));
    }

    @Override
    public void fillItemGroup(final ItemGroup tab, final NonNullList<ItemStack> items) {
        if (this.isInGroup(tab)) {
            for (final DyeColor color : DyeColor.values()) {
                items.add(DyeableItem.setColor(new ItemStack(this), color));
            }
        }
    }
}
