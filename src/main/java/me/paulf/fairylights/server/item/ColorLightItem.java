package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.block.LightBlock;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.util.Constants;

import net.minecraft.world.item.Item.Properties;

public class ColorLightItem extends LightItem {
    public ColorLightItem(final LightBlock light, final Properties properties) {
        super(light, properties);
    }

    @Override
    public Component getDisplayName(final ItemStack stack) {
        final CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("colors", Constants.NBT.TAG_LIST)) {
            return new TranslatableComponent("format.fairylights.color_changing", super.getDisplayName(stack));
        }
        return DyeableItem.getDisplayName(stack, super.getDisplayName(stack));
    }

    @Override
    public void fillItemGroup(final ItemGroup group, final NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            for (final DyeColor dye : DyeColor.values()) {
                items.add(DyeableItem.setColor(new ItemStack(this), dye));
            }
        }
    }
}
