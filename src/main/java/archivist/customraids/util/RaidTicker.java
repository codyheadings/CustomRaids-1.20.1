package archivist.customraids.util;

import archivist.customraids.Config;
import archivist.customraids.Customraids;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(
        modid = Customraids.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RaidTicker {
    private static final Map<ResourceKey<Level>, Boolean> WAS_NIGHT =
            new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.dimension().equals(Level.OVERWORLD)) continue;

            long time = level.getDayTime() % 24000L;
            int day = (int) (level.getGameTime() / 24000L);

            boolean night = time >= 13000 && time <= 23000;
            boolean wasNight = WAS_NIGHT.getOrDefault(level.dimension(), false);

            Customraids.getLOGGER().debug("Raid attempt day={} nightStart={}", day, !wasNight && night);

            if (night && !wasNight && !RaidManager.wasRaidAttemptedToday(level, day) && day % Config.raidInterval == 0) {
                Customraids.getLOGGER().info("Night detected, starting raids");
                RaidManager.startRaids(level);
            }

            if (!night && wasNight){
                for (RaidState raid : new ArrayList<>(RaidManager.getActiveRaids())){
                    raid.end(false);
                }
            }

            WAS_NIGHT.put(level.dimension(), night);
        }
    }
}
