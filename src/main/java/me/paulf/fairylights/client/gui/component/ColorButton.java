package me.paulf.fairylights.client.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.paulf.fairylights.client.gui.EditLetteredConnectionScreen;
import me.paulf.fairylights.util.styledstring.StyledString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public final class ColorButton extends Button {
    private static final int TEX_U = 0;

    private static final int TEX_V = 0;

    private ChatFormatting displayColor;

    private float displayColorR;

    private float displayColorG;

    private float displayColorB;

    public ColorButton(final int x, final int y, final Component msg, final Button.OnPress onPress) {
        super(x, y, 20, 20, msg, onPress);
    }

    public void setDisplayColor(final ChatFormatting color) {
        this.displayColor = color;
        final int rgb = StyledString.getColor(color);
        this.displayColorR = (rgb >> 16 & 0xFF) / 255F;
        this.displayColorG = (rgb >> 8 & 0xFF) / 255F;
        this.displayColorB = (rgb & 0xFF) / 255F;
    }

    public ChatFormatting getDisplayColor() {
        return this.displayColor;
    }

    public void removeDisplayColor() {
        this.displayColor = null;
    }

    public boolean hasDisplayColor() {
        return this.displayColor != null;
    }

    @Override
    public void renderButton(final PoseStack stack, final int mouseX, final int mouseY, final float delta) {
        if (this.visible) {
            Minecraft.getInstance().getTextureManager().bindTexture(EditLetteredConnectionScreen.WIDGETS_TEXTURE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(stack, this.x, this.y, TEX_U, this.isHovered ? TEX_V + this.height : TEX_V, this.width, this.height);
            if (this.displayColor != null) {
                this.blit(stack, this.x, this.y, TEX_U + this.width, TEX_V, this.width, this.height);
                RenderSystem.color4f(this.displayColorR, this.displayColorG, this.displayColorB, 1.0F);
                this.blit(stack, this.x, this.y, TEX_U + this.width, TEX_V + this.height, this.width, this.height);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }
}
