package baguchan.revampedwolf.client.screen;

import baguchan.revampedwolf.RevampedWolfCore;
import baguchan.revampedwolf.container.WolfInventoryContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WolfInventoryScreen extends ContainerScreen<WolfInventoryContainer> {
    private static final ResourceLocation WOLF_GUI_TEXTURES = new ResourceLocation(RevampedWolfCore.MODID, "textures/gui/container/wolf.png");
    /**
     * The mouse x-position recorded during the last rendered frame.
     */
    private float mousePosx;
    /**
     * The mouse y-position recorded during the last renderered frame.
     */
    private float mousePosY;

    public WolfInventoryScreen(WolfInventoryContainer p_i51084_1_, PlayerInventory p_i51084_2_, ITextComponent title) {
        super(p_i51084_1_, p_i51084_2_, title);
        this.passEvents = false;
    }

    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(WOLF_GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);

        InventoryScreen.drawEntityOnScreen(i + 51, j + 60, 17, (float) (i + 51) - this.mousePosx, (float) (j + 75 - 50) - this.mousePosY, this.container.getWolf());
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.mousePosx = (float) mouseX;
        this.mousePosY = (float) mouseY;
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY);
    }
}