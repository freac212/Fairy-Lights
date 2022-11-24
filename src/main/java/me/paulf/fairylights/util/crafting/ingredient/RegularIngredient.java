package me.paulf.fairylights.util.crafting.ingredient;

import me.paulf.fairylights.util.crafting.GenericRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public interface RegularIngredient extends GenericIngredient<RegularIngredient, GenericRecipe.MatchResultRegular> {
    default void matched(final ItemStack ingredient, final CompoundTag nbt) {}
}
