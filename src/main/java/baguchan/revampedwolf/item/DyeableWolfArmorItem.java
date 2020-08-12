package baguchan.revampedwolf.item;

import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.Item;

public class DyeableWolfArmorItem extends WolfArmorItem implements IDyeableArmorItem {
    public DyeableWolfArmorItem(int armorValue, String p_i50047_2_, Item.Properties builder) {
        super(armorValue, p_i50047_2_, builder);
    }

    public DyeableWolfArmorItem(int armorValue, net.minecraft.util.ResourceLocation texture, Item.Properties builder) {
        super(armorValue, texture, builder);
    }
}