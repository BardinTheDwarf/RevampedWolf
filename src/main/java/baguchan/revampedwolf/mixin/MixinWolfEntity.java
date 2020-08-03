package baguchan.revampedwolf.mixin;

import baguchan.revampedwolf.entity.GroupData;
import baguchan.revampedwolf.entity.HowlingEntity;
import baguchan.revampedwolf.entity.LeaderEntity;
import baguchan.revampedwolf.entity.goal.FollowLeaderGoal;
import baguchan.revampedwolf.entity.goal.HowlGoal;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(WolfEntity.class)
public abstract class MixinWolfEntity extends TameableEntity implements HowlingEntity, LeaderEntity {
    private static final DataParameter<Boolean> HOWLING = EntityDataManager.createKey(WolfEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<UUID>> LEADER_UUID_SECONDARY = EntityDataManager.createKey(WolfEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Optional<UUID>> LEADER_UUID_MAIN = EntityDataManager.createKey(WolfEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);


    private float howlAnimationProgress;
    private float lastHowlAnimationProgress;

    protected MixinWolfEntity(EntityType<? extends TameableEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "registerData", at = @At("TAIL"), cancellable = true)
    protected void onRegisterData(CallbackInfo callbackInfo) {
        this.dataManager.register(HOWLING, false);
        this.dataManager.register(LEADER_UUID_MAIN, Optional.empty());
        this.dataManager.register(LEADER_UUID_SECONDARY, Optional.empty());
    }

    @Inject(method = "registerGoals", at = @At("HEAD"), cancellable = true)
    protected void onRegisterGoals(CallbackInfo callbackInfo) {
        this.goalSelector.addGoal(5, new HowlGoal(this));
        this.goalSelector.addGoal(6, new FollowLeaderGoal<>(this));
    }

    @Inject(method = "handleStatusUpdate", at = @At("HEAD"), cancellable = true)
    public void handleStatusUpdate(byte status, CallbackInfo callbackInfo) {
        if (status == 64) {
            this.setHowling(true);
            callbackInfo.cancel();
        } else {
            this.setHowling(false);
        }
    }

    public List<UUID> getLeaderUUIDs() {
        List<UUID> list = Lists.newArrayList();
        list.add(this.dataManager.get(LEADER_UUID_SECONDARY).orElse((UUID) null));
        list.add(this.dataManager.get(LEADER_UUID_MAIN).orElse((UUID) null));
        return list;
    }

    public void addLeaderUUID(@Nullable UUID uuidIn) {
        if (this.dataManager.get(LEADER_UUID_SECONDARY).isPresent()) {
            this.dataManager.set(LEADER_UUID_MAIN, Optional.ofNullable(uuidIn));
        } else {
            this.dataManager.set(LEADER_UUID_SECONDARY, Optional.ofNullable(uuidIn));
        }
    }

    public boolean isLeader(){
        return this.getLeaderUUIDs().contains(this.getUniqueID());
    }

    public boolean hasLeader() {
        return this.getLeader() != null && this.getLeader().isAlive();
    }


    @Nullable
    public TameableEntity getLeader(){
        if(this.world instanceof ServerWorld){
            for(UUID uuid : this.getLeaderUUIDs()) {
                Entity entity = ((ServerWorld) this.world).getEntityByUuid(uuid);

                if(entity instanceof WolfEntity) {

                    return (TameableEntity) entity;
                }
            }
        }
        return null;
    }

    @Inject(method = "writeAdditional", at = @At("HEAD"), cancellable = true)
    public void onWriteAdditional(CompoundNBT compound, CallbackInfo callbackInfo) {
        List<UUID> list = this.getLeaderUUIDs();
        ListNBT listnbt = new ListNBT();

        for(UUID uuid : list) {
            if (uuid != null) {
                listnbt.add(NBTUtil.func_240626_a_(uuid));
            }
        }

        compound.put("Leader", listnbt);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Inject(method = "readAdditional", at = @At("HEAD"), cancellable = true)
    public void onReadAdditional(CompoundNBT compound, CallbackInfo callbackInfo) {
        ListNBT listnbt = compound.getList("Leader", 11);

        for(int i = 0; i < listnbt.size(); ++i) {
            this.addLeaderUUID(NBTUtil.readUniqueId(listnbt.get(i)));
        }
    }

    @Inject(method = "tick", at = @At("TAIL"), cancellable = true)
    public void onTick(CallbackInfo callbackInfo) {
        if (this.isAlive()) {
            this.lastHowlAnimationProgress = this.howlAnimationProgress;
            if (this.isHowling()) {
                this.howlAnimationProgress += (1.0F - this.howlAnimationProgress) * 0.4F;
            } else {
                this.howlAnimationProgress += (0.0F - this.howlAnimationProgress) * 0.4F;
            }
        }
    }

    //prevent fixing health
    @Overwrite
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
        if (tamed && this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() < 20.0D) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
            this.setHealth(this.getMaxHealth());
        }

        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D);
    }

    @Nullable
    @Override
    public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);

        if(reason != SpawnReason.BREEDING && reason != SpawnReason.STRUCTURE && reason != SpawnReason.COMMAND && reason != SpawnReason.SPAWN_EGG) {
            if (spawnDataIn == null) {
                spawnDataIn = new GroupData(this);
                this.addLeaderUUID(this.getUniqueID());
            } else {
                this.addLeaderUUID(((GroupData) spawnDataIn).groupLeader.getUniqueID());
            }
        }

        //leader health bounus
        if(!isTamed() && isLeader()){
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0D);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        }

        return spawnDataIn;
    }

    @Override
    public void setHowling(boolean howling) {
        if(howling) {
            this.howlAnimationProgress = 0.0F;
            this.lastHowlAnimationProgress = 0.0F;
        }
        this.dataManager.set(HOWLING, howling);
    }

    @Override
    public boolean isHowling() {
        return this.dataManager.get(HOWLING);
    }

    @Override
    public float getHowlAnimationProgress(float delta) {
        return this.lastHowlAnimationProgress + (this.howlAnimationProgress - this.lastHowlAnimationProgress) * delta;
    }
}