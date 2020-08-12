package baguchan.revampedwolf.container;

import baguchan.revampedwolf.RevampedWolfCore;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RevampedWolfCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WolfContainers {
    public static final ContainerType<WolfInventoryContainer> WOLF_INVENTORY = IForgeContainerType.create((windowId, inv, data) -> {
        return new WolfInventoryContainer(windowId, inv, data.readInt());
    });

    @SubscribeEvent
    public static void registerContaner(RegistryEvent.Register<ContainerType<?>> registry) {
        registry.getRegistry().register(WOLF_INVENTORY.setRegistryName("wolf_inventory"));
    }
}
