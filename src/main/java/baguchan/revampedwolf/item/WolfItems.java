package baguchan.revampedwolf.item;

import baguchan.revampedwolf.RevampedWolfCore;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RevampedWolfCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WolfItems {
    public static final Item LEATHER_WOLF_ARMOR = new DyeableWolfArmorItem(5, "leather", (new Item.Properties()).maxStackSize(1).group(ItemGroup.MISC));


    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> registry) {
        registry.getRegistry().register(LEATHER_WOLF_ARMOR.setRegistryName("leather_wolf_armor"));
    }

}