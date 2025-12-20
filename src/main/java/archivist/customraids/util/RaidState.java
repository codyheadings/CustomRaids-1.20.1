package archivist.customraids.util;

import archivist.customraids.Config;
import archivist.customraids.Customraids;
import archivist.customraids.util.raidcontext.RaidContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class RaidState {

    public final RaidContext context;
    public RaidDefinition definition;
    public int currentWave = 0;
    public final Set<Mob> activeMobs = new HashSet<>();
    public final ServerBossEvent bossBar;
    private static final Random RANDOM = new Random();
    private RaidResult result = RaidResult.ONGOING;
    private boolean finished = false;
    private final UUID id = UUID.randomUUID();
    public static final String RAID_ID_TAG = "customraids:raid_id";
    private boolean glowApplied = false;

    public RaidState(RaidContext context) {
        this.context = context;
        this.bossBar = new ServerBossEvent(
                Component.literal("Raid"),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );

        context.getParticipants().forEach(bossBar::addPlayer);
    }

    public void start(RaidDefinition def) {
        long day = context.level().getDayTime() / 24000L;

        if (finished) return;
        if (context.wasAttemptedToday(day)) return;

        this.result = RaidResult.ONGOING;
        this.definition = def;
        this.currentWave = 0;
        this.updateBossBarText();
        bossBar.setProgress(1.0F);
        bossBar.setVisible(true);
        context.markAttempted(day);

        Customraids.getLOGGER().debug(
                "Spawning first wave!"
        );
        spawnNextWave();
    }

    private void updateBossBarText() {
        if (currentWave < this.definition.waves.size()) {
            long aliveMobs = activeMobs.stream().filter((mob) -> mob != null && mob.isAlive()).count();
            int waveTotalMobs = definition.waves.get(currentWave).count;
            if (aliveMobs != (long)waveTotalMobs) {
                bossBar.setName(Component.literal(definition.name + ": " + aliveMobs + " Remain"));
            } else {
                bossBar.setName(Component.literal(definition.name));
            }

        }
    }

    private void spawnNextWave() {
        Customraids.getLOGGER().debug(
                "Spawning next wave!"
        );
        if (currentWave >= definition.waves.size()) {
            end(true);
            return;
        }

        for (ServerPlayer player : context.getParticipants()) {
            player.level().playSound(player, player.blockPosition(), SoundEvents.RAID_HORN.get(), SoundSource.PLAYERS, 1.0F, 0.5F);
            player.sendSystemMessage(
                    Component.literal("A wave of raiders is approaching!")
            );
        }

        // Spawn mobs relative to base
        activeMobs.removeIf(mob -> mob == null || !mob.isAlive());

        bossBar.setProgress(1.0F);

        WaveDefinition wave = definition.waves.get(currentWave);
        spawnWave(wave.count);
    }

    private void spawnWave(int mobCount){
        BlockPos base = context.getBasePos();
        ServerLevel level = context.level();

        for (int i = 0; i < mobCount; i++) {
            int distance = RANDOM.nextInt(Config.maxSpawnDistance - Config.minSpawnDistance + 1) + Config.minSpawnDistance;
            double angle = RANDOM.nextDouble() * (double)2.0F * Math.PI;
            int xOffset = (int)(Math.cos(angle) * (double)distance);
            int zOffset = (int)(Math.sin(angle) * (double)distance);
            BlockPos spawnPos = base.offset(xOffset, 0, zOffset);
            spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos);
            spawnPos = spawnPos.offset(0,2,0);

            MobEntry mobEntry = definition.getRandomMob(context);
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(mobEntry.id));
            List<MobEffectEntry> effectEntries = mobEntry.effects;
            Mob mob = (Mob) type.create(level);
            if (mob == null) continue;

            DifficultyInstance difficulty = level.getCurrentDifficultyAt(spawnPos); // will come in handy later

            mob.moveTo(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    level.random.nextFloat() * 360F,
                    0
            );

            mob.finalizeSpawn(
                    level,
                    difficulty,
                    MobSpawnType.EVENT,
                    null,
                    null
            );

            for (MobEffectEntry effectEntry : effectEntries) {
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(
                        new ResourceLocation(effectEntry.effect)
                );

                if (effect == null) continue;

                mob.addEffect(new MobEffectInstance(
                        effect,
                        effectEntry.duration,
                        effectEntry.amplifier,
                        effectEntry.ambient,
                        effectEntry.showParticles
                ));
            }

            mob.addTag("customraids:raider");
            mob.addTag(RAID_ID_TAG + "=" + id);

            if (mob instanceof FlyingMob || mob instanceof Slime) {
                mob.setTarget(context.getParticipants().stream().findFirst().orElse(null));
            } else {
                mob.goalSelector.addGoal(
                        2,
                        new MoveToBaseGoal(mob, 1.5, base)
                );
            }

            activeMobs.add(mob);
            level.addFreshEntity(mob);
        }
        this.updateBossBarText();
    }

    public void end(boolean success) {
        if (finished) return;

        for (ServerPlayer player : context.getParticipants()) {
            if (success) {
                player.sendSystemMessage(
                        Component.literal("The raiders have been vanquished!")
                );
                this.result = RaidResult.SUCCESS;
                player.giveExperiencePoints(definition.reward_xp);
            } else {
                player.sendSystemMessage(
                        Component.literal("The raiders could not be stopped...")
                );
                this.result = RaidResult.FAILURE;
            }
            bossBar.removePlayer(player);
        }
        this.finished = true;
        cleanupRaiders();
        bossBar.setVisible(false);
        bossBar.removeAllPlayers();
        RaidManager.onRaidFinished(this);
    }

    public void cleanupRaiders() {
        for (Mob mob : activeMobs) {
            if (mob != null && mob.isAlive()) {
                mob.discard();
            }
        }
        activeMobs.clear();
    }

    public void applyDayGlow() {
        if (glowApplied) return;
        if (!Config.dayGlow) return;

        for (Mob mob : activeMobs) {
            if (mob != null && mob.isAlive()) {
                mob.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING,
                        2000,
                        0,
                        false,
                        false
                ));
            }
        }

        for (ServerPlayer player : this.context.getParticipants()) {
            player.sendSystemMessage(
                    Component.literal("Raiders are revealed as dawn approaches!")
            );
        }

        glowApplied = true;
    }


    private void updateBossBar() {
        int total = definition.waves.get(currentWave).count;
        float progress = (float) activeMobs.size() / total;
        bossBar.setProgress(progress);
    }

    public void onTaggedMobDeath(Mob mob) {
        activeMobs.remove(mob);
        activeMobs.removeIf(m -> m == null || !m.isAlive());
        Customraids.getLOGGER().debug(
                "Tagged mob has been slain:"
        );

        this.updateBossBarText();

        if (activeMobs.isEmpty()) {
            Customraids.getLOGGER().debug(
                    "No more raiders left in this wave!"
            );
            currentWave++;
            spawnNextWave();
        } else {
            Customraids.getLOGGER().debug(
                    "More raiders remain!"
            );
            updateBossBar();
        }
    }

    public void addParticipant(ServerPlayer player) {
        if (isFinished()) return;

        if (context.hasParticipant(player)) return;

        context.addParticipant(player);
        bossBar.addPlayer(player);

        player.sendSystemMessage(
                Component.literal("You joined a raid!")
        );
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isActive() {
        return result == RaidResult.ONGOING;
    }

    public UUID getId() {
        return id;
    }
}