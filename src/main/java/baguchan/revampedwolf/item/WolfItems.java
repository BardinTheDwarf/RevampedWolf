package baguchan.revampedwolf.item;

import baguchan.revampedwolf.RevampedWolfCore;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RevampedWolfCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WolfItems {
    public static final Item LEATHER_WOLF_ARMOR = new DyeableWolfArmorItem(ArmorMaterial.LEATHER, 5, "leather", (new Item.Properties()).maxStackSize(1).maxDamage(81).group(ItemGroup.MISC));
    public static final Item IRON_WOLF_ARMOR = new WolfArmorItem(ArmorMaterial.IRON, 12, "iron", (new Item.Properties()).maxStackSize(1).maxDamage(241).group(ItemGroup.MISC));
    public static final Item GOLD_WOLF_ARMOR = new WolfArmorItem(ArmorMaterial.GOLD, 8, "gold", (new Item.Properties()).maxStackSize(1).maxDamage(113).group(ItemGroup.MISC));
    public static final Item DIAMOND_WOLF_ARMOR = new WolfArmorItem(ArmorMaterial.DIAMOND, 16, "diamond", (new Item.Properties()).maxStackSize(1).maxDamage(529).group(ItemGroup.MISC));
    public static final Item NETHERITE_WOLF_ARMOR = new WolfArmorItem(ArmorMaterial.NETHERITE, 20, "netherite", (new Item.Properties()).isBurnable().maxStackSize(1).maxDamage(858).group(ItemGroup.MISC));


    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> registry) {
        registry.getRegistry().register(LEATHER_WOLF_ARMOR.setRegistryName("leather_wolf_armor"));
        registry.getRegistry().register(IRON_WOLF_ARMOR.setRegistryName("iron_wolf_armor"));
        registry.getRegistry().register(GOLD_WOLF_ARMOR.setRegistryName("golden_wolf_armor"));
        registry.getRegistry().register(DIAMOND_WOLF_ARMOR.setRegistryName("diamond_wolf_armor"));
        registry.getRegistry().register(NETHERITE_WOLF_ARMOR.setRegistryName("netherite_wolf_armor"));
    }

}