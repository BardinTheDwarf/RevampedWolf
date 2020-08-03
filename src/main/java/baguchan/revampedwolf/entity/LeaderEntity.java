package baguchan.revampedwolf.entity;

import net.minecraft.entity.passive.TameableEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface LeaderEntity {
    boolean isLeader();

    boolean hasLeader();

    @Nullable
    TameableEntity getLeader();

    List<UUID> getLeaderUUIDs();

    void addLeaderUUID(@Nullable UUID uuidIn);
}
