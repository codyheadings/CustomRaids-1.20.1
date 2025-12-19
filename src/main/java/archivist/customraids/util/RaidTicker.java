package archivist.customraids.util;

import archivist.customraids.Config;
import archivist.customraids.Customraids;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = Customraids.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RaidTicker {

    private static boolean raidStartedTonight = false;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.dimension().equals(Level.OVERWORLD)) continue;

            long time = level.getDayTime() % 24000L;
            int day = (int) (level.getDayTime() / 24000L);

            boolean night = time >= 13000 && time <= 23000;

//            if (night && day % Config.raidInterval == 0 && !raidStartedTonight) {
            if (night && !raidStartedTonight) { // made to occur every night for testing
                Customraids.getLOGGER().info("Night detected, starting raids");
                raidStartedTonight = true;
                RaidManager.startRaids(level);
            }

            if (!night) {
                raidStartedTonight = false;
            }
        }
    }
}
