package archivist.customraids.util;

import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.function.ToIntFunction;

public class Selector{
    public static <T> T weightedRandom(
            List<T> entries,
            ToIntFunction<T> weightFunc,
            RandomSource random
    ) {
        int totalWeight = 0;

        for (T entry : entries) {
            int w = weightFunc.applyAsInt(entry);
            if (w > 0) {
                totalWeight += w;
            }
        }

        if (totalWeight <= 0) return null;

        int roll = random.nextInt(totalWeight);

        for (T entry : entries) {
            int w = weightFunc.applyAsInt(entry);
            if (w <= 0) continue;

            roll -= w;
            if (roll < 0) {
                return entry;
            }
        }

        return null;
    }

}
