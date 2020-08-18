package baguchan.revampedwolf.item;

import baguchan.revampedwolf.RevampedWolfCore;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

public class WolfArmorItem extends Item {
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");

    private final int armorValue;
    private final ResourceLocation texture;
    private final float toughness;
    protected final float knockbackResistance;
    protected final IArmorMaterial material;
    private final Multimap<Attribute, AttributeModifier> field_234656_m_;

    public WolfArmorItem(IArmorMaterial material, int armorValue, String tierArmor, Item.Properties properties) {
        this(material, armorValue, new ResourceLocation(RevampedWolfCore.MODID, "textures/entity/wolf/armor/wolf_armor_" + tierArmor + ".png"), properties);
    }

    public WolfArmorItem(IArmorMaterial materialIn, int armorValue, ResourceLocation texture, Item.Properties properties) {
        super(properties);
        this.armorValue = armorValue;
        this.toughness = materialIn.getToughness();
        this.knockbackResistance = materialIn.getKnockbackResistance();
        this.texture = texture;
        this.material = materialIn;

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        UUID uuid = ARMOR_MODIFIER_UUID;
        builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Wolf Armor modifier", (double) this.armorValue, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Wolf Armor toughness", (double) this.toughness, AttributeModifier.Operation.ADDITION));
        if (this.knockbackResistance > 0) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Wolf Armor knockback resistance", (double) this.knockbackResistance, AttributeModifier.Operation.ADDITION));
        }

        this.field_234656_m_ = builder.build();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.type == EnchantmentType.ARMOR || super.canApplyAtEnchantingTable(stack, enchantment);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        return equipmentSlot.getSlotType() == EquipmentSlotType.Group.ARMOR ? this.field_234656_m_ : super.getAttributeModifiers(equipmentSlot);
    }

    public int getItemEnchantability() {
        return this.material.getEnchantability();
    }

    public IArmorMaterial getArmorMaterial() {
        return this.material;
    }

    /**
     * Return whether this item is repairable in an anvil.
     */
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return this.material.getRepairMaterial().test(repair) || super.getIsRepairable(toRepair, repair);
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getArmorTexture() {
        return texture;
    }

    public int getArmorValue() {
        return this.armorValue;
    }
}