package archivist.customraids;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Customraids.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue MULTIPLAYER = BUILDER.comment("Whether raids will be per-player or per-server").define("multiplayer", false);

    private static final ForgeConfigSpec.BooleanValue DAYGLOW = BUILDER.comment("Raiders will glow as day approaches").define("dayGlow", true);

    //private static final ForgeConfigSpec.BooleanValue FAILONLOGOUT = BUILDER.comment("Players fail an active raid if they log out").define("failOnLogout", true);

    private static final ForgeConfigSpec.IntValue RAID_INTERVAL = BUILDER.comment("The number of days between raids").defineInRange("raidInterval", 7, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue MIN_DISTANCE = BUILDER.comment("Minimum distance (blocks) that a raider can spawn").defineInRange("minSpawnDistance", 52, 1, 160);

    private static final ForgeConfigSpec.IntValue MAX_DISTANCE = BUILDER.comment("Maximum distance (blocks) that a raider can spawn").defineInRange("maxSpawnDistance", 84, 1, 512);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean multiplayer;
    public static boolean dayGlow;
    //public static boolean failOnLogout;
    public static int raidInterval;
    public static int minSpawnDistance;
    public static int maxSpawnDistance;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        multiplayer = MULTIPLAYER.get();
        dayGlow = DAYGLOW.get();
        //failOnLogout = FAILONLOGOUT.get();
        raidInterval = RAID_INTERVAL.get();
        minSpawnDistance = MIN_DISTANCE.get();
        maxSpawnDistance = MAX_DISTANCE.get();
    }
}
