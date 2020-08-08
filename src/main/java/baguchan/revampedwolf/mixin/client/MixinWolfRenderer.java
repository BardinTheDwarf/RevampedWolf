package baguchan.revampedwolf.mixin.client;

import baguchan.revampedwolf.RevampedWolfCore;
import baguchan.revampedwolf.entity.IWolfType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.model.WolfModel;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfRenderer.class)
public abstract class MixinWolfRenderer extends MobRenderer<WolfEntity, WolfModel<WolfEntity>> {

    public MixinWolfRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new WolfModel<>(), 0.5F);
    }

    /**
     * Returns the location of an entity's texture.
     */
    @Inject(method = "getEntityTexture", at = @At("HEAD"), cancellable = true)
    public void getEntityTexture(WolfEntity entity, CallbackInfoReturnable<ResourceLocation> callbackInfo) {
        if (entity instanceof IWolfType) {
            if (((IWolfType) entity).getWolfType() == 0) {
                callbackInfo.setReturnValue(new ResourceLocation(((IWolfType) entity).getWolfTypeName()));
            } else {
                callbackInfo.setReturnValue(new ResourceLocation(RevampedWolfCore.MODID, ((IWolfType) entity).getWolfTypeName()));
            }
        }
    }
}