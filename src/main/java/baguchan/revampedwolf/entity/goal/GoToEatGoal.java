package baguchan.revampedwolf.entity.goal;

import baguchan.revampedwolf.entity.IEatable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.inventory.EquipmentSlotType;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class GoToEatGoal<T extends TameableEntity & IEatable> extends Goal {
    private static final Predicate<ItemEntity> canPickupItem = (itemEntity) -> {
        return !itemEntity.cannotPickup() && itemEntity.isAlive() && itemEntity.getItem().isFood() && itemEntity.getItem().getItem().getFood().isMeat();
    };
    private final T mob;
    private int cooldown;

    public GoToEatGoal(T p_i50572_2_) {
        this.mob = p_i50572_2_;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean shouldExecute() {
        if (this.mob.canEatableFood() && !this.mob.isBreedingItem(this.mob.getItemStackFromSlot(EquipmentSlotType.MAINHAND))) {
            if (--this.cooldown <= 0) {
                this.cooldown = this.setCoolDown(this.mob);

                List<ItemEntity> list = this.mob.world.getEntitiesWithinAABB(ItemEntity.class, this.mob.getBoundingBox().grow(16.0D, 8.0D, 16.0D), canPickupItem);
                if (!list.isEmpty()) {
                    return this.mob.getNavigator().tryMoveToEntityLiving(list.get(0), (double) 1.15F);
                }

            }

            return false;
        } else {
            return false;
        }
    }

    protected int setCoolDown(MobEntity taskOwnerIn) {
        return taskOwnerIn.getRNG().nextInt(60);
    }

}