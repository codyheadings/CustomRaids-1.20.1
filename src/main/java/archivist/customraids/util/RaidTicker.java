package archivist.customraids.util;

import archivist.customraids.Config;
import archivist.customraids.Customraids;
import archivist.customraids.util.raidcontext.RaidContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(
        modid = Customraids.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RaidTicker {
    private static final Map<ResourceKey<Level>, Boolean> WAS_NIGHT =
            new HashMap<>();
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if(tickCounter++ % 20 == 0) {

            for (ServerLevel level : event.getServer().getAllLevels()) {
                if (!level.dimension().equals(Level.OVERWORLD)) continue;

                long time = level.getDayTime() % 24000L;
                int day = (int) (level.getGameTime() / 24000L);

                boolean night = time >= 13000 && time <= 23000;
                boolean wasNight = WAS_NIGHT.getOrDefault(level.dimension(), false);
                boolean approachingDay = time >= 22000 && time < 23000;

                List<RaidState> activeRaids = RaidManager.getActiveRaids();
                for (RaidState raid : new ArrayList<>(activeRaids)) {
                    if (raid.isPaused()) continue;
                    RaidContext context = raid.context;
                    if (!context.hasLivingParticipants()) {
                        raid.end(false);
                    }
                }

                if (approachingDay && Config.dayGlow) {
                    for (RaidState raid : activeRaids) {
                        if (raid.isPaused()) continue;
                        if (raid.isActive()) {
                            raid.applyDayGlow();
                        }
                    }
                }

                if (night && !wasNight && !RaidManager.wasRaidAttemptedToday(level, day) && day % Config.raidInterval == 0) {
                    Customraids.getLOGGER().info("Night detected, starting raids");
                    RaidManager.startRaids(level);
                }

                if (!night && wasNight) {
                    for (RaidState raid : new ArrayList<>(activeRaids)) {
                        raid.end(false);
                    }
                }

                WAS_NIGHT.put(level.dimension(), night);
            }
        }
    }
}
