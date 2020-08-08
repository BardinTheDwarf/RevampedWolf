package baguchan.revampedwolf.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;

public class WolfAvoidEntityGoal<T extends LivingEntity> extends net.minecraft.entity.ai.goal.AvoidEntityGoal<T> {
    private final WolfEntity wolf;

    public WolfAvoidEntityGoal(WolfEntity wolfIn, Class<T> entityClassToAvoidIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
        super(wolfIn, entityClassToAvoidIn, avoidDistanceIn, farSpeedIn, nearSpeedIn);
        this.wolf = wolfIn;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean shouldExecute() {
        if (super.shouldExecute() && this.avoidTarget instanceof LlamaEntity) {
            return !this.wolf.isTamed() && this.avoidLlama((LlamaEntity) this.avoidTarget);
        } else {
            return false;
        }
    }

    private boolean avoidLlama(LlamaEntity llamaIn) {
        return llamaIn.getStrength() >= this.wolf.getRNG().nextInt(5);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.wolf.setAttackTarget((LivingEntity) null);
        super.startExecuting();
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        this.wolf.setAttackTarget((LivingEntity) null);
        super.tick();
    }
}