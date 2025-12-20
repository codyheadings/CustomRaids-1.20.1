package archivist.customraids.util;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

public class MobEntry {
    public String id;
    public int weight = 1; //TODO: add customizability
    public List<MobEffectEntry> effects = List.of(); //TODO: add boosts to mobs
}
