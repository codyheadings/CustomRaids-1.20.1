package archivist.customraids.util;

import archivist.customraids.util.raidcontext.RaidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class RaidDefinition {
    public String name;
    public List<MobEntry> mobs;
    public List<WaveDefinition> waves;
    public int weight = 1;
    public int reward_xp;

    public MobEntry getRandomMob(RaidContext raidContext) {
        List<MobEntry> mobs = this.mobs;
        MobEntry mobEntry = new MobEntry();
        mobEntry.id="minecraft:zombie";
        if (mobs.isEmpty()) return mobEntry;

        return Selector.weightedRandom(
                mobs,
                mob -> mob.weight,
                raidContext.level().random
        );
    }

    public int getWeight() {
        return this.weight;
    }
}

