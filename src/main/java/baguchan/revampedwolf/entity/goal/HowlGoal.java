package baguchan.revampedwolf.entity.goal;

import baguchan.revampedwolf.entity.HowlingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;

public class HowlGoal extends Goal {

    private final WolfEntity mob;
    private final World world;
    private int howligTimer;
    private int cooldown;

    public HowlGoal(WolfEntity mob) {
        this.mob = mob;
        this.world = mob.world;
        this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean shouldExecute() {
        if (--this.cooldown <= 0) {
            this.cooldown = this.setCoolDown(this.mob);

            return (this.mob.isTamed() && this.mob.isSitting() || !this.mob.isTamed()) && this.mob.getAttackTarget() == null && this.world.canSeeSky(new BlockPos(this.mob.getPositionVec())) && world.getDayTime() > 16000 && world.getDayTime() < 21000 &&
                    !mob.isChild() && this.mob.getRNG().nextInt(15) == 0;
        }
        return false;
    }

    protected int setCoolDown(MobEntity taskOwnerIn) {
        return 100 + taskOwnerIn.getRNG().nextInt(100);
    }


    @Override
    public void startExecuting() {
        this.howligTimer = 60;
        this.world.setEntityState(this.mob, (byte) 64);
        this.mob.getNavigator().clearPath();
        this.mob.playSound(SoundEvents.ENTITY_WOLF_HOWL, 2.5F, this.mob.getRNG().nextFloat() * 0.1F + 1.0F);
    }

    @Override
    public void resetTask() {
        this.howligTimer = 0;
        ((HowlingEntity) mob).setHowling(false);
        this.world.setEntityState(this.mob, (byte) 0);
    }


    @Override
    public boolean shouldContinueExecuting() {
        return this.howligTimer > 0;
    }

    @Override
    public void tick() {
        this.howligTimer = Math.max(0, this.howligTimer - 1);
    }
}