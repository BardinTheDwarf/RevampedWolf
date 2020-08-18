package baguchan.revampedwolf.mixin;

import baguchan.revampedwolf.container.WolfInventoryContainer;
import baguchan.revampedwolf.entity.*;
import baguchan.revampedwolf.entity.goal.FollowLeaderGoal;
import baguchan.revampedwolf.entity.goal.GoToEatGoal;
import baguchan.revampedwolf.entity.goal.HowlGoal;
import baguchan.revampedwolf.entity.goal.WolfAvoidEntityGoal;
import baguchan.revampedwolf.item.WolfArmorItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mixin(WolfEntity.class)
public abstract class MixinWolfEntity extends TameableEntity implements HowlingEntity, LeaderEntity, IAngerable, IEatable, IWolfType, IWolfArmor, IWolfInventory, IInventoryChangedListener {
    private static final DataParameter<Boolean> HOWLING = EntityDataManager.createKey(WolfEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<UUID>> LEADER_UUID_SECONDARY = EntityDataManager.createKey(WolfEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Optional<UUID>> LEADER_UUID_MAIN = EntityDataManager.createKey(WolfEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Integer> EAT_COUNTER = EntityDataManager.createKey(WolfEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WOLF_TYPE = EntityDataManager.createKey(WolfEntity.class, DataSerializers.VARINT);

    private static final Map<Integer, String> TEXTURE_BY_STRING = Util.make(Maps.newHashMap(), (p_213410_0_) -> {
        p_213410_0_.put(0, "textures/entity/wolf/wolf");
        p_213410_0_.put(1, "textures/entity/wolf/wolf_brown");
    });

    private float howlAnimationProgress;
    private float lastHowlAnimationProgress;
    private int eatCooldownTicks;
    protected Inventory wolfInventory;

    protected MixinWolfEntity(EntityType<? extends WolfEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    public void onConstructor(CallbackInfo info) {
        this.initWolfInventory();
        this.setCanPickUpLoot(true);
    }

    @Inject(method = "registerData", at = @At("TAIL"), cancellable = true)
    protected void onRegisterData(CallbackInfo callbackInfo) {
        this.dataManager.register(HOWLING, false);
        this.dataManager.register(LEADER_UUID_MAIN, Optional.empty());
        this.dataManager.register(LEADER_UUID_SECONDARY, Optional.empty());
        this.dataManager.register(EAT_COUNTER, 0);
        this.dataManager.register(WOLF_TYPE, 0);
    }

    /*
     * Override Wolf's ai and revamped
     */
    @Inject(method = "registerGoals", at = @At("HEAD"), cancellable = true)
    protected void registerGoals(CallbackInfo callbackInfo) {
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
        this.targetSelector.addGoal(5, new NonTamedTargetGoal(this, AnimalEntity.class, false, WolfEntity.TARGET_ENTITIES) {
            @Override
            public boolean shouldExecute() {
                return eatCooldownTicks <= 0 && super.shouldExecute();
            }
        });
        this.targetSelector.addGoal(6, new NonTamedTargetGoal<>(this, TurtleEntity.class, false, TurtleEntity.TARGET_DRY_BABY));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeletonEntity.class, false));
        this.targetSelector.addGoal(8, new ResetAngerGoal<>(this, true));
        callbackInfo.cancel();
    }

    public ItemStack func_213803_dV() {
        return this.getItemStackFromSlot(EquipmentSlotType.CHEST);
    }

    private void setChestArmor(ItemStack p_213805_1_) {
        this.setItemStackToSlot(EquipmentSlotType.CHEST, p_213805_1_);
        this.setDropChance(EquipmentSlotType.CHEST, 0.0F);
    }

    protected void setEquipArmor() {
        if (!this.world.isRemote) {
            this.setArmor(this.wolfInventory.getStackInSlot(0));
            this.setDropChance(EquipmentSlotType.CHEST, 0.0F);
        }
    }


    private void setArmor(ItemStack p_213804_1_) {
        this.setChestArmor(p_213804_1_);
    }

    /**
     * Called by InventoryBasic.onInventoryChanged() on a array that is never filled.
     */
    public void onInventoryChanged(IInventory invBasic) {
        ItemStack itemstack = this.func_213803_dV();
        ItemStack itemstack1 = this.func_213803_dV();
        if (this.ticksExisted > 20 && this.isWolfArmor(itemstack1) && itemstack != itemstack1) {
            this.playSound(SoundEvents.ENTITY_HORSE_ARMOR, 0.5F, 1.0F);
        }
        this.setEquipArmor();
    }

    public boolean isWolfArmor(ItemStack stack) {
        return stack.getItem() instanceof WolfArmorItem;
    }

    protected int getInventorySize() {
        return 1;
    }

    public Inventory getWolfInventory() {
        return this.wolfInventory;
    }

    protected void initWolfInventory() {
        Inventory inventory = this.wolfInventory;
        this.wolfInventory = new Inventory(this.getInventorySize());
        if (inventory != null) {
            inventory.removeListener(this);
            int i = Math.min(inventory.getSizeInventory(), this.wolfInventory.getSizeInventory());

            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = inventory.getStackInSlot(j);
                if (!itemstack.isEmpty()) {
                    this.wolfInventory.setInventorySlotContents(j, itemstack.copy());
                }
            }
        }

        this.wolfInventory.addListener(this);
        this.setEquipArmor();
        this.itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this.wolfInventory));
    }

    @Inject(method = "func_230254_b_", at = @At("HEAD"), cancellable = true)
    public void func_230254_b_(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResultType> callbackInfo) {
        if (!this.isChild()) {
            if (this.isTamed() && this.func_233685_eM_() && player.isSecondaryUseActive()) {
                if (player instanceof ServerPlayerEntity && !(player instanceof FakePlayer)) {
                    if (!player.world.isRemote) {
                        ServerPlayerEntity entityPlayerMP = (ServerPlayerEntity) player;
                        NetworkHooks.openGui(entityPlayerMP, new INamedContainerProvider() {
                            @Override
                            public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
                                return new WolfInventoryContainer(windowId, inventory, getEntityId());
                            }

                            @Override
                            public ITextComponent getDisplayName() {
                                return getName();
                            }
                        }, buf -> {
                            buf.writeInt(this.getEntityId());
                        });
                    }
                }
                callbackInfo.setReturnValue(ActionResultType.func_233537_a_(this.world.isRemote));
            }
        }
    }

    @Override
    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {
        if (super.replaceItemInInventory(inventorySlot, itemStackIn)) {
            return true;
        } else {
            int i = inventorySlot - 300;
            if (i >= 0 && i < this.wolfInventory.getSizeInventory()) {
                this.wolfInventory.setInventorySlotContents(i, itemStackIn);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public String getWolfTypeName() {
        String state = null;

        if (this.isTamed()) {
            state = "_tame";
        } else if (this.func_233678_J__()) {
            state = "_angry";
        }

        if (state != null) {
            return TEXTURE_BY_STRING.getOrDefault(this.getWolfType(), TEXTURE_BY_STRING.get(0)) + state + ".png";
        } else {
            return TEXTURE_BY_STRING.getOrDefault(this.getWolfType(), TEXTURE_BY_STRING.get(0)) + ".png";
        }
    }

    public int getWolfType() {
        return this.dataManager.get(WOLF_TYPE);
    }

    public void setWolfType(int type) {
        if (type < 0 || type >= 2) {
            type = this.rand.nextInt(2);
        }

        this.dataManager.set(WOLF_TYPE, type);
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

        if (!this.wolfInventory.getStackInSlot(0).isEmpty()) {
            compound.put("ArmorItem", this.wolfInventory.getStackInSlot(0).write(new CompoundNBT()));
        }

        compound.put("Leader", listnbt);
        compound.putInt("EatCooldown", eatCooldownTicks);
        compound.putInt("WolfType", this.getWolfType());
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
        this.eatCooldownTicks = compound.getInt("EatCooldown");
        this.setWolfType(compound.getInt("WolfType"));

        if (compound.contains("ArmorItem", 10)) {
            ItemStack itemstack = ItemStack.read(compound.getCompound("ArmorItem"));
            this.wolfInventory.setInventorySlotContents(0, itemstack);
        }

        this.setEquipArmor();
        this.setCanPickUpLoot(true);
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

    private void spitOutItem(ItemStack stackIn) {
        if (!stackIn.isEmpty() && !this.world.isRemote) {
            ItemEntity itementity = new ItemEntity(this.world, this.getPosX() + this.getLookVec().x, this.getPosY() + 1.0D, this.getPosZ() + this.getLookVec().z, stackIn);
            itementity.setPickupDelay(40);
            itementity.setThrowerId(this.getUniqueID());
            this.world.addEntity(itementity);
        }
    }

    private void spawnItem(ItemStack stackIn) {
        ItemEntity itementity = new ItemEntity(this.world, this.getPosX(), this.getPosY(), this.getPosZ(), stackIn);
        this.world.addEntity(itementity);
    }

    @Override
    protected void updateEquipmentIfNeeded(ItemEntity itemEntity) {
        if (!this.isTamed() && this.eatCooldownTicks <= 0) {
            if (this.getItemStackFromSlot(EquipmentSlotType.MAINHAND).isEmpty() && itemEntity.getItem().isFood() && itemEntity.getItem().getItem().getFood().isMeat()) {
                ItemStack itemstack = itemEntity.getItem();
                int i = itemstack.getCount();
                if (i > 1) {
                    this.spawnItem(itemstack.split(i - 1));
                }

                this.spitOutItem(this.getItemStackFromSlot(EquipmentSlotType.MAINHAND));
                this.triggerItemPickupTrigger(itemEntity);
                this.setItemStackToSlot(EquipmentSlotType.MAINHAND, itemstack.split(1));
                this.inventoryHandsDropChances[EquipmentSlotType.MAINHAND.getIndex()] = 2.0F;
                this.onItemPickup(itemEntity, itemstack.getCount());
                itemEntity.remove();
                this.eatCooldownTicks = 1200;
            }
        }
    }

    /*
     * prevent fixing health, this method make modded health friendly
     */
    @Inject(method = "setTamed", at = @At("HEAD"), cancellable = true)
    public void setTamed(boolean tamed, CallbackInfo info) {
        super.setTamed(tamed);

        if (tamed && this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() < 20.0D) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
            this.setHealth(this.getMaxHealth());
        }

        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        info.cancel();
    }

    @Override
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        //when cold biome, alway white
        if (worldIn.getBiome(this.getPosition()).getTemperature(this.getPosition()) <= 0.15F) {
            this.setWolfType(0);
        } else {
            this.setWolfType(this.rand.nextInt(2));
        }

        if (reason != SpawnReason.BREEDING && reason != SpawnReason.STRUCTURE && reason != SpawnReason.COMMAND && reason != SpawnReason.SPAWN_EGG) {
            if (spawnDataIn == null) {
                spawnDataIn = new GroupData(this);
                this.addLeaderUUID(this.getUniqueID());
            } else {
                this.addLeaderUUID(((GroupData) spawnDataIn).groupLeader.getUniqueID());
                if (((GroupData) spawnDataIn).groupLeader instanceof IWolfType) {
                    this.setWolfType(((IWolfType) ((GroupData) spawnDataIn).groupLeader).getWolfType());
                }
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
    
    @Inject(method = "func_241840_a", at = @At("HEAD"), cancellable = true)
    public void func_241840_a(ServerWorld p_241840_1_, AgeableEntity ageable, CallbackInfoReturnable<WolfEntity> callbackInfo) {
        WolfEntity wolfentity = EntityType.WOLF.create(this.world);
        if (ageable instanceof WolfEntity && ageable instanceof IWolfType) {
            if (wolfentity instanceof IWolfType) {
                if (this.rand.nextBoolean()) {
                    ((IWolfType) wolfentity).setWolfType(this.getWolfType());
                } else {
                    ((IWolfType) wolfentity).setWolfType(((IWolfType) ageable).getWolfType());
                }

                if (this.isTamed()) {
                    UUID uuid = this.getOwnerId();
                    if (uuid != null) {
                        wolfentity.setOwnerId(this.getOwnerId());
                        wolfentity.setTamed(true);
                    }
                    if (this.rand.nextBoolean()) {
                        wolfentity.setCollarColor(this.getCollarColor());
                    } else {
                        wolfentity.setCollarColor(((WolfEntity) ageable).getCollarColor());
                    }
                }
            }
        }

        callbackInfo.setReturnValue(wolfentity);
        callbackInfo.cancel();
    }

    @Shadow
    public DyeColor getCollarColor() {
        return null;
    }

    private net.minecraftforge.common.util.LazyOptional<?> itemHandler = null;

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.util.Direction facing) {
        if (this.isAlive() && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && itemHandler != null)
            return itemHandler.cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void remove(boolean keepData) {
        super.remove(keepData);
        if (!keepData && itemHandler != null) {
            itemHandler.invalidate();
            itemHandler = null;
        }
    }
}