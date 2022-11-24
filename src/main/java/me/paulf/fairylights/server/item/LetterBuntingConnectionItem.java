package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.connection.ConnectionTypes;
import me.paulf.fairylights.util.styledstring.StyledString;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.List;

public class LetterBuntingConnectionItem extends ConnectionItem {
    public LetterBuntingConnectionItem(final Item.Properties properties) {
        super(properties, ConnectionTypes.LETTER_BUNTING);
    }

    @Override
    public void addInformation(final ItemStack stack, final Level world, final List<Component> tooltip, final TooltipFlag flag) {
        if (!stack.hasTag()) {
            return;
        }
        final CompoundTag compound = stack.getTag();
        if (compound.contains("text", NBT.TAG_COMPOUND)) {
            final CompoundTag text = compound.getCompound("text");
            final StyledString s = StyledString.deserialize(text);
            if (s.length() > 0) {
                tooltip.add(new TranslatableComponent("format.fairylights.text", s.toTextComponent()).mergeStyle(TextFormatting.GRAY));
            }
        }
    }

    @Override
    public void fillItemGroup(final ItemGroup tab, final NonNullList<ItemStack> items) {
        if (this.isInGroup(tab)) {
            final ItemStack bunting = new ItemStack(this, 1);
            bunting.getOrCreateTag().put("text", StyledString.serialize(new StyledString()));
            items.add(bunting);
        }
    }
}
