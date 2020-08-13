package baguchan.revampedwolf.entity;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.passive.TameableEntity;

public class GroupData extends AgeableEntity.AgeableData {
    public final TameableEntity groupLeader;

    public GroupData(TameableEntity groupLeaderIn) {
        super(true);
        this.groupLeader = groupLeaderIn;
    }
}