package archivist.customraids.util;

import archivist.customraids.Config;
import archivist.customraids.Customraids;
import archivist.customraids.util.raidcontext.PlayerRaidContext;
import archivist.customraids.util.raidcontext.RaidContext;
import archivist.customraids.util.raidcontext.ServerRaidContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public class RaidManager {

    private static final List<RaidState> ACTIVE_RAIDS = new ArrayList<>();

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
                availableRaids.get(random.nextInt(availableRaids.size()));

        RaidState raid = new RaidState(context);
        ACTIVE_RAIDS.add(raid);

        raid.start(def);
        Customraids.getLOGGER().debug(
                "Raid started, good luck!"
        );
    }

    public static List<RaidState> getActiveRaids() {
        return ACTIVE_RAIDS;
    }

    public static void removeRaidsForPlayer(ServerPlayer player) {
        ACTIVE_RAIDS.removeIf(raid ->
                raid.context instanceof PlayerRaidContext prc &&
                        prc.getPlayer().equals(player)
        );
    }
}
