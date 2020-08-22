package baguchan.revampedwolf.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.JumpGoal;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class SafeLeapAtTargetGoal extends JumpGoal {
    private final MobEntity mobEntity;
    private final int chance;

    public SafeLeapAtTargetGoal(MobEntity fish, int chance) {
        this.mobEntity = fish;
        this.chance = chance;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (this.mobEntity.getRNG().nextInt(this.chance) != 0) {
            return false;
        } else if (!this.mobEntity.isOnGround()) {
            return false;
        } else {
            LivingEntity livingentity = this.mobEntity.getAttackTarget();

            if (livingentity != null && livingentity.isAlive() && this.mobEntity.getDistanceSq(livingentity) < 16D && this.mobEntity.getDistanceSq(livingentity) > 4D) {
                if (livingentity.getAdjustedHorizontalFacing() != livingentity.getHorizontalFacing()) {
                    return false;
                }
                Direction direction = this.mobEntity.getAdjustedHorizontalFacing();
                int i = direction.getXOffset();
                int j = direction.getZOffset();
                boolean flag = isSafeGround(this.mobEntity, livingentity);
                if (!flag) {
                    this.mobEntity.getNavigator().getPathToEntity(livingentity, 0);
                }

                return flag;
            }

            return false;
        }
    }

    public static boolean isSafeGround(MobEntity mobentity, LivingEntity target) {
        double d0 = target.getPosZ() - mobentity.getPosZ();
        double d1 = target.getPosX() - mobentity.getPosX();
        double d2 = d0 / d1;


        for (int j = 0; j < 3; ++j) {
            double d3 = d2 == 0.0D ? 0.0D : d0 * (double) ((float) j / 3.0F);
            double d4 = d2 == 0.0D ? d1 * (double) ((float) j / 3.0F) : d3 / d2;


            for (int i = 0; i < 2; ++i) {
                BlockPos pos = new BlockPos(mobentity.getPosX() + d4, mobentity.getPosY() + (double) i, mobentity.getPosZ() + d3);

                if (!mobentity.world.getBlockState(pos).getMaterial().isReplaceable()) {
                    return false;
                }

                PathNavigator pathnavigator = mobentity.getNavigator();
                if (pathnavigator != null) {
                    NodeProcessor nodeprocessor = pathnavigator.getNodeProcessor();
                    if (nodeprocessor != null && nodeprocessor.getPathNodeType(mobentity.world, MathHelper.floor(pos.getX()), MathHelper.floor(mobentity.getPosY()), MathHelper.floor(pos.getZ())) != PathNodeType.WALKABLE) {
                        return false;
                    }
                }
            }

        }

        return true;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting() {
        LivingEntity livingentity = this.mobEntity.getAttackTarget();
        if (livingentity != null && livingentity.isAlive()) {
            double d0 = this.mobEntity.getMotion().y;
            return (!(d0 * d0 < (double) 0.03F) || !this.mobEntity.isOnGround());
        } else {
            return false;
        }
    }

    public boolean isPreemptible() {
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        LivingEntity entity = this.mobEntity.getAttackTarget();
        if (entity != null && this.mobEntity.getDistanceSq(entity) < 16D && this.mobEntity.getDistanceSq(entity) > 4D) {
            this.mobEntity.getLookController().setLookPositionWithEntity(entity, 60.0F, 30.0F);
            Vector3d vec3d = (new Vector3d(entity.getPosX() - mobEntity.getPosX(), entity.getPosY() - mobEntity.getPosY(), entity.getPosZ() - mobEntity.getPosZ())).normalize();
            Vector3d vector3d = this.mobEntity.getMotion();
            if (vec3d.lengthSquared() > 1.0E-7D) {
                vec3d = vec3d.normalize().scale(0.4D).add(vector3d.scale(0.2D));
            }
            this.mobEntity.setMotion(vec3d.x, (double) 0.45F, vec3d.z);
        }
        this.mobEntity.getNavigator().clearPath();
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask() {
        this.mobEntity.rotationPitch = 0.0F;
    }
}