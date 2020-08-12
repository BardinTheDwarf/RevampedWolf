package baguchan.revampedwolf.item;

import baguchan.revampedwolf.RevampedWolfCore;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WolfArmorItem extends Item {
    private final int armorValue;
    private final ResourceLocation texture;

    public WolfArmorItem(int armorValue, String tierArmor, Item.Properties builder) {
        this(armorValue, new ResourceLocation(RevampedWolfCore.MODID, "textures/entity/wolf/armor/wolf_armor_" + tierArmor + ".png"), builder);
    }

    public WolfArmorItem(int armorValue, ResourceLocation texture, Item.Properties builder) {
        super(builder);
        this.armorValue = armorValue;
        this.texture = texture;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.type == EnchantmentType.ARMOR || super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getArmorTexture() {
        return texture;
    }

    public int getArmorValue() {
        return this.armorValue;
    }
}