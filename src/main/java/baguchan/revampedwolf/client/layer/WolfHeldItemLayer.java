package baguchan.revampedwolf.client.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.entity.model.WolfModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

public class WolfHeldItemLayer extends LayerRenderer<WolfEntity, WolfModel<WolfEntity>> {
    public WolfHeldItemLayer(IEntityRenderer<WolfEntity, WolfModel<WolfEntity>> wolfRenderer) {
        super(wolfRenderer);
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, WolfEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.getEntityModel() instanceof IHasHead) {
            boolean flag = entitylivingbaseIn.isSleeping();
            boolean flag1 = entitylivingbaseIn.isChild();
            matrixStackIn.push();
            if (flag1) {
                float f = 0.75F;
                matrixStackIn.scale(0.75F, 0.75F, 0.75F);
                matrixStackIn.translate(0.0D, 0.65D, (double) 0.0F);
            }

            float f1 = entitylivingbaseIn.getInterestedAngle(partialTicks);

            matrixStackIn.translate((double) (((IHasHead) this.getEntityModel()).getModelHead().rotationPointX / 16.0F), (double) (((IHasHead) this.getEntityModel()).getModelHead().rotationPointY / 16.0F), (double) (((IHasHead) this.getEntityModel()).getModelHead().rotationPointZ / 16.0F));
            matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(f1));
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(netHeadYaw));
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(headPitch));

            matrixStackIn.translate((double) 0.06F, (double) 0.15F, -0.42D);


            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90.0F));

            ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EquipmentSlotType.MAINHAND);
            Minecraft.getInstance().getFirstPersonRenderer().renderItemSide(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, packedLightIn);
            matrixStackIn.pop();
        }
    }
}
