package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.connection.ConnectionTypes;
import me.paulf.fairylights.server.item.crafting.FLCraftingRecipes;
import me.paulf.fairylights.util.styledstring.StyledString;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.List;

public class PennantBuntingConnectionItem extends ConnectionItem {
    public PennantBuntingConnectionItem(final Item.Properties properties) {
        super(properties, ConnectionTypes.PENNANT_BUNTING);
    }

    @Override
    public void addInformation(final ItemStack stack, final Level world, final List<Component> tooltip, final TooltipFlag flag) {
        final CompoundTag compound = stack.getTag();
        if (compound == null) {
            return;
        }
        if (compound.contains("text", NBT.TAG_COMPOUND)) {
            final CompoundTag text = compound.getCompound("text");
            final StyledString s = StyledString.deserialize(text);
            if (s.length() > 0) {
                tooltip.add(new TranslatableComponent("format.fairylights.text", s.toTextComponent()).mergeStyle(TextFormatting.GRAY));
            }
        }
        if (compound.contains("pattern", NBT.TAG_LIST)) {
            final ListNBT tagList = compound.getList("pattern", NBT.TAG_COMPOUND);
            final int tagCount = tagList.size();
            if (tagCount > 0) {
                tooltip.add(new StringTextComponent(""));
            }
            for (int i = 0; i < tagCount; i++) {
                final ItemStack item = ItemStack.read(tagList.getCompound(i));
                tooltip.add(item.getDisplayName());
            }
        }
    }

    @Override
    public void fillItemGroup(final ItemGroup tab, final NonNullList<ItemStack> subItems) {
        if (this.isInGroup(tab)) {
            for (final DyeColor color : DyeColor.values()) {
                final ItemStack stack = new ItemStack(this);
                DyeableItem.setColor(stack, color);
                subItems.add(FLCraftingRecipes.makePennant(stack, color));
            }
        }
    }
}
