package baguchan.revampedwolf.registry;

import baguchan.revampedwolf.RevampedWolfCore;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RevampedWolfCore.MODID)
public class WolfSounds {
    public static final SoundEvent WOLF_HOWLING = createEvent("mob.wolf.howling");

    private static SoundEvent createEvent(String sound) {

        ResourceLocation name = new ResourceLocation(RevampedWolfCore.MODID, sound);

        return new SoundEvent(name).setRegistryName(name);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> evt) {
        evt.getRegistry().register(WOLF_HOWLING);
    }
}