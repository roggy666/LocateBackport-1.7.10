package lol.gzmc.locatebackport.locate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import lol.gzmc.locatebackport.api.IBiomeProvider;
import lol.gzmc.locatebackport.api.LocateAPI;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public class BiomeLocator {

    public static final int DEFAULT_SEARCH_RADIUS = 6400;

    public static List<String> getAvailableBiomeNames() {
        List<String> list = new ArrayList<String>();
        for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray()) {
            if (b != null && b.biomeName != null && !b.biomeName.isEmpty()) {
                String clean = sanitize(b.biomeName);
                if (!list.contains(clean)) {
                    list.add(clean);
                }
            }
        }
        for (IBiomeProvider provider : LocateAPI.getBiomeProviders()) {
            for (String name : provider.getSupportedBiomeNames()) {
                String clean = sanitize(name);
                if (!list.contains(clean)) {
                    list.add(clean);
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    public static BiomeGenBase resolveBiome(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String clean = name.trim().toLowerCase(Locale.ROOT).replace(" ", "_");

        try {
            int id = Integer.parseInt(clean);
            if (id >= 0 && id < BiomeGenBase.getBiomeGenArray().length) {
                return BiomeGenBase.getBiomeGenArray()[id];
            }
        } catch (NumberFormatException ignored) {}

        for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray()) {
            if (b == null || b.biomeName == null) {
                continue;
            }
            if (sanitize(b.biomeName).equalsIgnoreCase(clean)
                    || b.biomeName.equalsIgnoreCase(name)
                    || b.biomeName.toLowerCase(Locale.ROOT).replace(" ", "").equals(clean.replace("_", ""))) {
                return b;
            }
        }

        for (IBiomeProvider provider : LocateAPI.getBiomeProviders()) {
            BiomeGenBase b = provider.resolveBiome(name);
            if (b != null) {
                return b;
            }
        }

        return null;
    }

    public static ChunkPosition findNearestBiome(World world, BiomeGenBase biome, int x, int z, int radius) {
        if (world == null || biome == null) {
            return null;
        }

        for (IBiomeProvider provider : LocateAPI.getBiomeProviders()) {
            ChunkPosition pos = provider.findNearestBiome(world, biome, x, z, radius);
            if (pos != null) {
                return pos;
            }
        }

        WorldChunkManager wcm = world.getWorldChunkManager();
        if (wcm == null) {
            return null;
        }

        try {
            return wcm.findBiomePosition(x, z, radius, Collections.singletonList(biome), new Random());
        } catch (Throwable t) {
            return null;
        }
    }

    public static String sanitize(String raw) {
        return raw.trim().toLowerCase(Locale.ROOT).replace(" ", "_");
    }
}
