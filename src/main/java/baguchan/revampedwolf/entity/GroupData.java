package baguchan.revampedwolf.entity;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.passive.TameableEntity;

public class GroupData extends AgeableEntity.AgeableData {
    public final TameableEntity groupLeader;

    public GroupData(TameableEntity groupLeaderIn) {
        this.groupLeader = groupLeaderIn;
    }
}