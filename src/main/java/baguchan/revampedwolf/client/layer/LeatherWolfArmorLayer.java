package baguchan.revampedwolf.client.layer;

import baguchan.revampedwolf.item.DyeableWolfArmorItem;
import baguchan.revampedwolf.item.WolfArmorItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.WolfModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LeatherWolfArmorLayer extends LayerRenderer<WolfEntity, WolfModel<WolfEntity>> {
    private final WolfModel<WolfEntity> model = new WolfModel<>();

    public LeatherWolfArmorLayer(IEntityRenderer<WolfEntity, WolfModel<WolfEntity>> p_i50937_1_) {
        super(p_i50937_1_);
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, WolfEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EquipmentSlotType.CHEST);
        if (itemstack.getItem() instanceof WolfArmorItem) {
            matrixStackIn.push();
            WolfArmorItem wolfarmoritem = (WolfArmorItem) itemstack.getItem();
            this.getEntityModel().copyModelAttributesTo(this.model);
            this.model.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
            this.model.setRotationAngles(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            float f;
            float f1;
            float f2;
            if (wolfarmoritem instanceof DyeableWolfArmorItem) {
                int i = ((DyeableWolfArmorItem) wolfarmoritem).getColor(itemstack);
                f = (float) (i >> 16 & 255) / 255.0F;
                f1 = (float) (i >> 8 & 255) / 255.0F;
                f2 = (float) (i & 255) / 255.0F;
            } else {
                f = 1.0F;
                f1 = 1.0F;
                f2 = 1.0F;
            }

            IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(wolfarmoritem.getArmorTexture()));
            this.model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, f, f1, f2, 1.0F);
            matrixStackIn.pop();
        }
    }
}