package archivist.customraids;

import archivist.customraids.events.RaidReloadListener;
import archivist.customraids.util.RaidManager;
import archivist.customraids.util.RaidState;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.ArrayList;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Customraids.MODID)
public class Customraids {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "customraids";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Customraids() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID)
    public class RaidEvents {

        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;

            for (RaidState raid :
                    new ArrayList<>(RaidManager.getRaidsForPlayer(player))) {

                raid.context.removeParticipant(player);
                raid.bossBar.removePlayer(player);
            }
        }

        @SubscribeEvent
        public static void onMobDeath(LivingDeathEvent event) {
            if (!(event.getEntity() instanceof Mob mob)) return;

            if (!mob.getTags().contains("customraids:raider")) return;

            RaidState raid = RaidManager.getRaidForMob(mob);
            if (raid == null) return;

            raid.onTaggedMobDeath(mob);
        }

        @SubscribeEvent
        public static void onMobAttacked(LivingHurtEvent event) {
            if (!(event.getEntity() instanceof Mob mob)) return;
            if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

            if (!mob.getTags().contains("customraids")) return;

            RaidState raid = RaidManager.getRaidForMob(mob);
            if (raid == null) return;

            raid.addParticipant(player);
        }

        @SubscribeEvent
        public static void onPlayerDeath(LivingDeathEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;

            for (RaidState raid :
                    new ArrayList<>(RaidManager.getRaidsForPlayer(player))) {

                raid.context.removeParticipant(player);
                raid.bossBar.removePlayer(player);
            }
        }

        @SubscribeEvent
        public static void onMobSpawn(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof Mob mob) {
                if (mob.getTags().contains("customraids:raider")) {
                    mob.setPersistenceRequired();
                }
            }
        }

        @SubscribeEvent
        public static void onMobDespawnCheck(MobSpawnEvent.AllowDespawn event) {
            Mob mob = event.getEntity();

            if (!mob.getTags().contains("customraids:raider")) return;

            RaidState raid = RaidManager.getRaidForMob(mob);
            if (raid != null && raid.isActive()) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @Mod.EventBusSubscriber(
            modid = Customraids.MODID,
            bus = Mod.EventBusSubscriber.Bus.FORGE
    )
    public static class ReloadEvents {

        @SubscribeEvent
        public static void onAddReloadListeners(AddReloadListenerEvent event) {

            event.addListener(new RaidReloadListener());

            Customraids.getLOGGER().info("Raid reload listener registered");
        }
    }


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
