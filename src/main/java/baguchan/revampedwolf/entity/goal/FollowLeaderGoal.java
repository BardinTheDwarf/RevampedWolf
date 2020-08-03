package baguchan.revampedwolf.entity.goal;

import baguchan.revampedwolf.entity.LeaderEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;

import java.util.EnumSet;

public class FollowLeaderGoal<T extends TameableEntity & LeaderEntity> extends Goal {
    private final T taskOwner;
    private int navigateTimer;
    private int cooldown;

    public FollowLeaderGoal(T taskOwnerIn) {
        this.taskOwner = taskOwnerIn;
        this.cooldown = this.setCoolDown(taskOwnerIn);
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
    }

    protected int setCoolDown(T taskOwnerIn) {
        return 200 + taskOwnerIn.getRNG().nextInt(200) % 20;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean shouldExecute() {
        if (this.taskOwner.isLeader() || this.taskOwner.isTamed() || this.taskOwner.getAttackTarget() != null) {
            return false;
        } else if (this.taskOwner.getLeader() == null) {
            return false;
        } else if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        } else {
            this.cooldown = this.setCoolDown(this.taskOwner);

            return this.taskOwner.hasLeader() && this.taskOwner.getDistanceSq(this.taskOwner.getLeader()) > 42;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting() {
        return this.taskOwner.hasLeader() && this.taskOwner.getDistanceSq(this.taskOwner.getLeader()) > 16;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.navigateTimer = 0;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask() {
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        if (--this.navigateTimer <= 0 && this.taskOwner.hasLeader()) {
            this.navigateTimer = 10;
            this.taskOwner.getNavigator().tryMoveToEntityLiving(this.taskOwner.getLeader(), 1.0D);
        }
    }
}