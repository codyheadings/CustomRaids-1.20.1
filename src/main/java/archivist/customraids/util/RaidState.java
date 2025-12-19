package archivist.customraids.util;

import archivist.customraids.Config;
import archivist.customraids.Customraids;
import archivist.customraids.util.raidcontext.RaidContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RaidState {

    public final RaidContext context;
    public RaidDefinition definition;
    public int currentWave = 0;
    public final Set<Mob> activeMobs = new HashSet<>();
    public final ServerBossEvent bossBar;
    private static final Random RANDOM = new Random();

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

        this.definition = def;
        this.currentWave = 0;
        bossBar.setProgress(1.0F);
        bossBar.setVisible(true);

        Customraids.getLOGGER().debug(
                "Spawning first wave!"
        );
        spawnWave();
    }

    private void spawnWave() {
        if (currentWave >= definition.waves.size()) {
            end(true);
            return;
        }

        BlockPos base = context.getBasePos();
        ServerLevel level = context.level();

        for (ServerPlayer player : context.getParticipants()) {
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.RAID_HORN.get(), SoundSource.PLAYERS, 1.0F, 0.5F);
        }

        // Spawn mobs relative to base
        activeMobs.removeIf(mob -> mob == null || !mob.isAlive());

        bossBar.setProgress(1.0F);

        WaveDefinition wave = definition.waves.get(currentWave);
        int mobCount = wave.count;

        for (int i = 0; i < mobCount; i++) {
            int distance = RANDOM.nextInt(Config.maxSpawnDistance - Config.minSpawnDistance + 1) + Config.minSpawnDistance;
            double angle = RANDOM.nextDouble() * (double)2.0F * Math.PI;
            int xOffset = (int)(Math.cos(angle) * (double)distance);
            int zOffset = (int)(Math.sin(angle) * (double)distance);
            BlockPos spawnPos = base.offset(xOffset, 0, zOffset);
            spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos);

            EntityType<?> type = definition.getRandomMob(context);
            Mob mob = (Mob) type.create(level);
            if (mob == null) continue;

            mob.moveTo(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    level.random.nextFloat() * 360F,
                    0
            );

            mob.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(spawnPos),
                    MobSpawnType.EVENT,
                    null,
                    null
            );

            mob.addTag("customraids");
//            mob.addTag("raid_id:" + id);

            mob.goalSelector.addGoal(
                    2,
                    new MoveToBaseGoal(mob, 1.1, base)
            );

            activeMobs.add(mob);
            level.addFreshEntity(mob);
        }
    }

    public void end(boolean success) {
        for (ServerPlayer player : context.getParticipants()) {
            if (success) {
                player.giveExperiencePoints(definition.reward_xp);
            }
            bossBar.removePlayer(player);
        }
    }
}