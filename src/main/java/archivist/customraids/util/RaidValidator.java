package archivist.customraids.util;

import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class RaidValidator {

    public static void validate(ResourceLocation id, RaidDefinition raid) {

        if (raid.name == null || raid.name.isBlank()) {
            throw new JsonSyntaxException("Missing raid name");
        }

        if (raid.mobs == null || raid.mobs.isEmpty()) {
            throw new JsonSyntaxException("Raid has no mobs");
        }

        if (raid.waves == null || raid.waves.isEmpty()) {
            throw new JsonSyntaxException("Raid has no waves");
        }

        for (MobEntry mobId : raid.mobs) {
            ResourceLocation loc = new ResourceLocation(mobId.id);
            if (!ForgeRegistries.ENTITY_TYPES.containsKey(loc)) {
                throw new JsonSyntaxException("Unknown entity: " + mobId.id);
            }
        }

        for (WaveDefinition wave : raid.waves) {
            if (wave.count <= 0) {
                throw new JsonSyntaxException("Raider count must be > 0");
            }
        }
    }
}
