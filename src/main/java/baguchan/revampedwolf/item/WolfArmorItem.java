package baguchan.revampedwolf.item;

import baguchan.revampedwolf.RevampedWolfCore;
import net.minecraft.item.Item;
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

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getArmorTexture() {
        return texture;
    }

    public int getArmorValue() {
        return this.armorValue;
    }
}