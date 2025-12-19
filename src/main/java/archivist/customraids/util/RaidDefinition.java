package archivist.customraids.util;

import archivist.customraids.util.raidcontext.RaidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class RaidDefinition {
    public String name;
    public List<MobEntry> mobs;
    public List<WaveDefinition> waves;
    public int reward_xp;

    public EntityType<?> getRandomMob(RaidContext raidContext) {
        //TODO: expand later to add weighting
        List<MobEntry> mobs = this.mobs;
        if (mobs.isEmpty()) return EntityType.ZOMBIE;

        MobEntry entry = mobs.get(
                raidContext.level().random.nextInt(mobs.size())
        );

        return ForgeRegistries.ENTITY_TYPES.getValue(
                new ResourceLocation(entry.id)
        );
    }
}

