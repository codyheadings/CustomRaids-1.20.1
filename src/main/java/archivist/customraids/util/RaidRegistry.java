package archivist.customraids.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class RaidRegistry {

    private static final Map<ResourceLocation, RaidDefinition> RAIDS = new HashMap<>();

    public static void clear() {
        RAIDS.clear();
    }

    public static void register(ResourceLocation id, RaidDefinition raid) {
        RAIDS.put(id, raid);
    }

    public static Collection<RaidDefinition> getAll() {

        return RAIDS.values();
    }

    public static RaidDefinition getRandom(RandomSource random) {
        if (RAIDS.isEmpty()) return null;
        int index = random.nextInt(RAIDS.size());
        return RAIDS.values().stream().skip(index).findFirst().orElse(null);
    }
}
