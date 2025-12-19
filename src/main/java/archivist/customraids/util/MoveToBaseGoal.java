package archivist.customraids.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class MoveToBaseGoal extends Goal {
    private final Mob mob;
    private final double speed;
    private final BlockPos basePos;

    public MoveToBaseGoal(Mob mob, double speed, BlockPos basePos) {
        this.mob = mob;
        this.speed = speed;
        this.basePos = basePos;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    public boolean canUse() {
        return this.mob.getTarget() == null;
    }

    @Override
    public void tick() {
        if (mob.getTarget() == null && !mob.getNavigation().isInProgress()) {
            mob.getNavigation().moveTo(
                    basePos.getX() + 0.5,
                    basePos.getY(),
                    basePos.getZ() + 0.5,
                    speed
            );
        }
    }
}