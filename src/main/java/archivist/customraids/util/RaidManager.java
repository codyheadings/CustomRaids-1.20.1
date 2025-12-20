package archivist.customraids.util;

import archivist.customraids.Config;
import archivist.customraids.Customraids;
import archivist.customraids.util.raidcontext.PlayerRaidContext;
import archivist.customraids.util.raidcontext.RaidContext;
import archivist.customraids.util.raidcontext.ServerRaidContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.*;

public class RaidManager {

    private static final List<RaidState> ACTIVE_RAIDS = new ArrayList<>();

    public static RaidState getRaidForMob(Mob mob) {
        for (String tag : mob.getTags()) {
            if (tag.startsWith("customraids:raid_id=")) {
                UUID id = UUID.fromString(tag.substring(20));
                return getRaidById(id);
            }
        }
        return null;
    }

    public static RaidState getRaidById(UUID id) {
        for (RaidState raid : ACTIVE_RAIDS) {
            if (raid.getId().equals(id)) {
                return raid;
            }
        }
        return null;
    }

    public static void startRaids(ServerLevel level) {
        List<RaidDefinition> availableRaids =
                new ArrayList<>(RaidRegistry.getAll());

        if (availableRaids.isEmpty()) {
            Customraids.getLOGGER().debug(
                    "No raids found."
            );
            return;
        }

        if (!Config.multiplayer) {
            // Default: One shared raid
            Customraids.getLOGGER().debug(
                    "Starting raid!"
            );
            startSingleRaid(
                    new ServerRaidContext(level),
                    availableRaids,
                    level.random
            );
        } else {
            // Multiplayer enabled: One raid per player
            for (ServerPlayer player : level.players()) {
                startSingleRaid(
                        new PlayerRaidContext(player),
                        availableRaids,
                        level.random
                );
                Customraids.getLOGGER().debug(
                        "Starting player raid!"
                );
            }
        }
    }

    private static void startSingleRaid(
            RaidContext context,
            List<RaidDefinition> availableRaids,
            RandomSource random
    ) {
        RaidDefinition def =
                getRandomRaid(availableRaids, random);

        RaidState raid = new RaidState(context);
        ACTIVE_RAIDS.add(raid);

        raid.start(def);
        Customraids.getLOGGER().debug(
                "Raid started, good luck!"
        );
    }

    private static RaidDefinition getRandomRaid(List<RaidDefinition> availableRaids, RandomSource random) {
//        TODO: add difficulty/conditional selection logic for random raids
        return Selector.weightedRandom(availableRaids, RaidDefinition::getWeight, random);
    }

    public static List<RaidState> getActiveRaids() {
        return ACTIVE_RAIDS;
    }

    public static List<RaidState> getRaidsForPlayer(ServerPlayer player) {
        return ACTIVE_RAIDS.stream()
                .filter(r -> r.context.getParticipants().contains(player))
                .toList();
    }

    public static boolean wasRaidAttemptedToday(ServerLevel level, long day) {
        for (RaidState raid : ACTIVE_RAIDS) {
            if (raid.context.wasAttemptedToday(day)) {
                return true;
            }
        }
        return false;
    }

    public static void onRaidFinished(RaidState raid) {
        ACTIVE_RAIDS.remove(raid);
    }

    public static void removeRaidsForPlayer(ServerPlayer player) {
        for (RaidState raid : new ArrayList<>(ACTIVE_RAIDS)){
            if (raid.context.getParticipants().contains(player)){
                raid.end(false);
            }
        }
    }
}
