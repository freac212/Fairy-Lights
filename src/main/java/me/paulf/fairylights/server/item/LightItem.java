package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.block.LightBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class LightItem extends BlockItem {
    private final LightBlock light;

    public LightItem(final LightBlock light, final Properties properties) {
        super(light, properties);
        this.light = light;
    }

    @Override
    public LightBlock getBlock() {
        return this.light;
    }

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final CompoundTag nbt) {
        return LightVariant.provider(this.light.getVariant());
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final Level world, final List<Component> tooltip, final TooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        final CompoundNBT tag = stack.getTag();
        if (tag != null) {
            if (tag.getBoolean("twinkle")) {
                tooltip.add(new TranslationTextComponent("item.fairyLights.twinkle").mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
            }
            if (tag.contains("colors", Constants.NBT.TAG_LIST)) {
                final ListNBT colors = tag.getList("colors", Constants.NBT.TAG_INT);
                for (int i = 0; i < colors.size(); i++) {
                    tooltip.add(DyeableItem.getColorName(colors.getInt(i)).mergeStyle(TextFormatting.GRAY));
                }
            }
        }
    }
}
