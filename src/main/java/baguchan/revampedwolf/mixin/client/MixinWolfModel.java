package baguchan.revampedwolf.mixin.client;

import baguchan.revampedwolf.entity.HowlingEntity;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.entity.model.TintedAgeableModel;
import net.minecraft.client.renderer.entity.model.WolfModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.passive.WolfEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfModel.class)
public abstract class MixinWolfModel<T extends WolfEntity> extends TintedAgeableModel<T> implements IHasHead {

    @Shadow
    @Final
    private ModelRenderer head;

    @Shadow
    @Final
    private ModelRenderer headChild;

    @Shadow
    @Final
    private ModelRenderer mane;

    @Inject(method = "setLivingAnimations", at = @At("TAIL"), cancellable = true)
    public void setLivingAnimations(T wolfEntity, float f, float g, float h, CallbackInfo callbackInfo) {
        float progress = ((HowlingEntity) wolfEntity).getHowlAnimationProgress(h);
        this.head.rotationPointY = 13.5F - 2 * progress;
        this.head.rotateAngleX = this.head.rotateAngleX + (-0.8F - this.head.rotateAngleX) * progress;
        this.headChild.rotateAngleX = progress * -0.3F;
    }

    @Inject(method = "setRotationAngles", at = @At("HEAD"), cancellable = true)
    public void setRotationAngles(T wolfEntity, float f, float g, float h, float i, float j, CallbackInfo callbackInfo) {
        if (((HowlingEntity) wolfEntity).getHowlAnimationProgress(1F) >= 0.001D) {
            callbackInfo.cancel();
        }
    }

    @Override
    public ModelRenderer getModelHead() {
        return this.head;
    }
}