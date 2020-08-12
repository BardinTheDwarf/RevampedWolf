package baguchan.revampedwolf.client;

import baguchan.revampedwolf.RevampedWolfCore;
import baguchan.revampedwolf.client.screen.WolfInventoryScreen;
import baguchan.revampedwolf.container.WolfContainers;
import baguchan.revampedwolf.item.WolfItems;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = RevampedWolfCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistrar {

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event) {
        ItemColors itemColors = event.getItemColors();
        itemColors.register((p_210239_0_, p_210239_1_) -> {
            return p_210239_0_.getItem() instanceof IDyeableArmorItem ? ((IDyeableArmorItem) p_210239_0_.getItem()).getColor(p_210239_0_) : -1;
        }, WolfItems.LEATHER_WOLF_ARMOR);
    }

    public static void setup(final FMLCommonSetupEvent event) {
        ScreenManager.registerFactory(WolfContainers.WOLF_INVENTORY, WolfInventoryScreen::new);
    }
}
