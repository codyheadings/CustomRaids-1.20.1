package archivist.customraids.events;

import archivist.customraids.util.RaidDefinition;
import archivist.customraids.util.RaidRegistry;
import archivist.customraids.util.RaidValidator;
import archivist.customraids.util.WaveDefinition;
import archivist.customraids.Customraids;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class RaidReloadListener
        extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    public static final Map<ResourceLocation, RaidDefinition> RAIDS = new HashMap<>();

    public RaidReloadListener() {
        super(GSON, "raids");
        Customraids.getLOGGER().info("RaidReloadListener constructed");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> jsons,
            ResourceManager manager,
            ProfilerFiller profiler
    ) {
        Customraids.getLOGGER().info("RaidReloadListener.apply called with {} raid files", jsons.size());
        RAIDS.clear();
        RaidRegistry.clear();

        jsons.forEach((id, element) -> {
            try {
                RaidDefinition raid =
                        GSON.fromJson(element, RaidDefinition.class);

                RaidValidator.validate(id, raid);
                RaidRegistry.register(id, raid);
                RAIDS.put(id, raid);
                Customraids.getLOGGER().debug(
                        "Successfully loaded raid {}",
                        id
                );

            } catch (Exception e) {
                Customraids.getLOGGER().error(
                        "Failed to load raid {}",
                        id, e
                );
            }
        });
    }
}