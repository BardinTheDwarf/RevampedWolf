package baguchan.revampedwolf.mixin;

import baguchan.revampedwolf.entity.GroupData;
import baguchan.revampedwolf.entity.HowlingEntity;
import baguchan.revampedwolf.entity.IEatable;
import baguchan.revampedwolf.entity.LeaderEntity;
import baguchan.revampedwolf.entity.goal.FollowLeaderGoal;
import baguchan.revampedwolf.entity.goal.GoToEatGoal;
import baguchan.revampedwolf.entity.goal.HowlGoal;
import baguchan.revampedwolf.entity.goal.WolfAvoidEntityGoal;
import com.google.common.collect.Lists;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
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

import static net.minecraft.entity.passive.WolfEntity.TARGET_ENTITIES;

@Mixin(WolfEntity.class)
public abstract class MixinWolfEntity extends TameableEntity implements HowlingEntity, LeaderEntity, IAngerable, IEatable {
    private static final DataParameter<Boolean> HOWLING = EntityDataManager.createKey(WolfEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<UUID>> LEADER_UUID_SECONDARY = EntityDataManager.createKey(WolfEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Optional<UUID>> LEADER_UUID_MAIN = EntityDataManager.createKey(WolfEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Integer> EAT_COUNTER = EntityDataManager.createKey(WolfEntity.class, DataSerializers.VARINT);

    private float howlAnimationProgress;
    private float lastHowlAnimationProgress;
    private int eatCooldownTicks;

    protected MixinWolfEntity(EntityType<? extends TameableEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    public void onConstructor(CallbackInfo info) {
        this.setCanPickUpLoot(true);
    }

    @Inject(method = "registerData", at = @At("TAIL"), cancellable = true)
    protected void onRegisterData(CallbackInfo callbackInfo) {
        this.dataManager.register(HOWLING, false);
        this.dataManager.register(LEADER_UUID_MAIN, Optional.empty());
        this.dataManager.register(LEADER_UUID_SECONDARY, Optional.empty());
        this.dataManager.register(EAT_COUNTER, 0);
    }

    @Overwrite
    protected void registerGoals() {
        WolfEntity wolfEntity = (WolfEntity) ((Object) this);

        this.goalSelector.addGoal(1, new SwimGoal(this));
        this.goalSelector.addGoal(2, new SitGoal(this));
        this.goalSelector.addGoal(3, new WolfAvoidEntityGoal(wolfEntity, LlamaEntity.class, 24.0F, 1.5D, 1.5D));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(6, new FollowLeaderGoal<>(this));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new GoToEatGoal<>(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(9, new BegGoal(wolfEntity, 8.0F));
        this.goalSelector.addGoal(9, new HowlGoal(wolfEntity));
        this.goalSelector.addGoal(10, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(10, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setCallsForHelp());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(wolfEntity, PlayerEntity.class, 10, true, false, this::func_233680_b_));
        this.targetSelector.addGoal(5, new NonTamedTargetGoal<>(this, AnimalEntity.class, false, TARGET_ENTITIES));
        this.targetSelector.addGoal(6, new NonTamedTargetGoal<>(this, TurtleEntity.class, false, TurtleEntity.TARGET_DRY_BABY));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeletonEntity.class, false));
        this.targetSelector.addGoal(8, new ResetAngerGoal<>(this, true));
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

                if (entity instanceof WolfEntity) {

                    return (TameableEntity) entity;
                }
            }
        }
        return null;
    }

    private int getEatCounter() {
        return this.dataManager.get(EAT_COUNTER);
    }

    private void setEatCounter(int p_213571_1_) {
        this.dataManager.set(EAT_COUNTER, p_213571_1_);
    }


    @Inject(method = "writeAdditional", at = @At("TAIL"), cancellable = true)
    public void onWriteAdditional(CompoundNBT compound, CallbackInfo callbackInfo) {
        List<UUID> list = this.getLeaderUUIDs();
        ListNBT listnbt = new ListNBT();

        for (UUID uuid : list) {
            if (uuid != null) {
                listnbt.add(NBTUtil.func_240626_a_(uuid));
            }
        }

        compound.put("Leader", listnbt);
        compound.putInt("EatCooldown", eatCooldownTicks);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Inject(method = "readAdditional", at = @At("TAIL"), cancellable = true)
    public void onReadAdditional(CompoundNBT compound, CallbackInfo callbackInfo) {
        ListNBT listnbt = compound.getList("Leader", 11);

        for (int i = 0; i < listnbt.size(); ++i) {
            this.addLeaderUUID(NBTUtil.readUniqueId(listnbt.get(i)));
        }
        this.setCanPickUpLoot(true);

        this.eatCooldownTicks = compound.getInt("EatCooldown");
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

            if (eatCooldownTicks > 0) {
                eatCooldownTicks = MathHelper.clamp(--eatCooldownTicks, 0, 1200);
            }

            this.eatTick();
        }
    }

    private void eatTick() {
        if (!this.isEating() && !this.getItemStackFromSlot(EquipmentSlotType.MAINHAND).isEmpty() && this.rand.nextInt(80) == 0) {
            this.setEating(true);
        } else if (this.getItemStackFromSlot(EquipmentSlotType.MAINHAND).isEmpty() && this.isEating()) {
            this.setEating(false);
        }

        if (this.isEating()) {
            this.makeEatParticle();
            if (!this.world.isRemote && this.getEatCounter() > 60) {
                if (this.isBreedingItem(this.getItemStackFromSlot(EquipmentSlotType.MAINHAND)) && this.getItemStackFromSlot(EquipmentSlotType.MAINHAND).isFood()) {
                    if (!this.world.isRemote) {
                        this.heal(this.getItemStackFromSlot(EquipmentSlotType.MAINHAND).getItem().getFood().getHealing());
                        this.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
                    }

                }

                this.setEating(false);
                return;
            }

            this.setEatCounter(this.getEatCounter() + 1);
        }
    }

    private void makeEatParticle() {
        if (this.getEatCounter() % 5 == 0) {
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + 0.1F * this.rand.nextFloat(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);

            ItemStack itemstack = this.getItemStackFromSlot(EquipmentSlotType.MAINHAND);
            if (!itemstack.isEmpty()) {
                for (int i = 0; i < 8; ++i) {
                    Vector3d vector3d = (new Vector3d(((double) this.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D)).rotatePitch(-this.rotationPitch * ((float) Math.PI / 180F)).rotateYaw(-this.rotationYaw * ((float) Math.PI / 180F));
                    this.world.addParticle(new ItemParticleData(ParticleTypes.ITEM, itemstack), this.getPosX() + this.getLookVec().x / 1.85D, this.getPosY() + this.getEyeHeight(), this.getPosZ() + this.getLookVec().z / 1.85D, vector3d.x, vector3d.y + 0.05D, vector3d.z);
                }
            }
        }

    }

    @Override
    public boolean canEatableFood() {
        return this.eatCooldownTicks <= 0;
    }

    public boolean isEating() {
        return this.dataManager.get(EAT_COUNTER) > 0;
    }

    public void setEating(boolean p_213534_1_) {
        this.dataManager.set(EAT_COUNTER, p_213534_1_ ? 1 : 0);
    }

    @Override
    protected void updateEquipmentIfNeeded(ItemEntity itemEntity) {
        if (!this.isTamed() && this.eatCooldownTicks <= 0) {
            if (this.getItemStackFromSlot(EquipmentSlotType.MAINHAND).isEmpty() && itemEntity.getItem().isFood() && itemEntity.getItem().getItem().getFood().isMeat()) {
                this.triggerItemPickupTrigger(itemEntity);
                ItemStack itemstack = itemEntity.getItem();
                this.setItemStackToSlot(EquipmentSlotType.MAINHAND, itemstack);
                this.inventoryHandsDropChances[EquipmentSlotType.MAINHAND.getIndex()] = 2.0F;
                this.onItemPickup(itemEntity, itemstack.getCount());
                itemEntity.remove();
                this.eatCooldownTicks = 1200;
            }
        }
    }

    @Inject(method = "setTamed", at = @At("HEAD"), cancellable = true)
    public void setTamed(boolean tamed, CallbackInfo info) {
        super.setTamed(tamed);
        /*
         * prevent fixing health, this method make modded health friendly
         */
        if (tamed && this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() < 20.0D) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
            this.setHealth(this.getMaxHealth());
        }

        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        info.cancel();
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