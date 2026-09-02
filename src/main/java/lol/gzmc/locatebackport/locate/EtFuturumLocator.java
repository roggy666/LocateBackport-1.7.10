package lol.gzmc.locatebackport.locate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cpw.mods.fml.common.Loader;
import lol.gzmc.locatebackport.api.IStructureProvider;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public class EtFuturumLocator implements IStructureProvider {

    private static Boolean loaded = null;
    private static final List<String> STRUCTURES = new ArrayList<String>();
    private static final List<String> ALIASES = new ArrayList<String>();
    private static final List<BiomeGenBase> VALID_OCEAN_BIOMES = Arrays.asList(
            BiomeGenBase.ocean, BiomeGenBase.deepOcean, BiomeGenBase.river,
            BiomeGenBase.frozenOcean, BiomeGenBase.frozenRiver
    );

    static {
        STRUCTURES.add("Monument");
        STRUCTURES.add("OceanMonument");
        STRUCTURES.add("MesaMineshaft");

        ALIASES.add("monument");
        ALIASES.add("oceanmonument");
        ALIASES.add("ocean_monument");
        ALIASES.add("mesamineshaft");
        ALIASES.add("mesa_mineshaft");
    }

    @Override
    public String getName() {
        return "Monument";
    }

    @Override
    public List<String> getAliases() {
        return ALIASES;
    }

    @Override
    public ChunkPosition findNearest(World world, String requestedName, int playerX, int playerY, int playerZ, int maxRadiusChunks) {
        return findEtFuturumStructure(world, requestedName, playerX, playerY, playerZ, maxRadiusChunks);
    }

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = Loader.isModLoaded("etfuturum") || hasClass("ganymedes01.etfuturum.EtFuturum");
        }
        return loaded;
    }

    private static boolean hasClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static List<String> getAvailableStructures() {
        if (!isLoaded()) {
            return Collections.emptyList();
        }
        return STRUCTURES;
    }

    public static ChunkPosition findEtFuturumStructure(World world, String name, int playerX, int playerY, int playerZ, int maxChunkRadius) {
        if (!isLoaded() || world == null || name == null) {
            return null;
        }

        String target = name.toLowerCase(Locale.ROOT).replace("_", "");

        if (target.contains("monument") || target.contains("oceanmonument")) {
            return findOceanMonument(world, playerX, playerZ, maxChunkRadius);
        }

        return null;
    }

    public static ChunkPosition findOceanMonument(World world, int playerX, int playerZ, int maxChunkRadius) {
        int centerChunkX = playerX >> 4;
        int centerChunkZ = playerZ >> 4;
        int spacing = 32;
        int separation = 5;

        int maxCells = (maxChunkRadius / spacing) + 2;
        for (int r = 0; r <= maxCells; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) {
                        continue;
                    }

                    int cellX = (centerChunkX >= 0 ? centerChunkX : centerChunkX - (spacing - 1)) / spacing + dx;
                    int cellZ = (centerChunkZ >= 0 ? centerChunkZ : centerChunkZ - (spacing - 1)) / spacing + dz;

                    Random random = world.setRandomSeed(cellX, cellZ, 10387313);
                    int spawnChunkX = cellX * spacing + (random.nextInt(spacing - separation) + random.nextInt(spacing - separation)) / 2;
                    int spawnChunkZ = cellZ * spacing + (random.nextInt(spacing - separation) + random.nextInt(spacing - separation)) / 2;

                    int posX = spawnChunkX * 16 + 8;
                    int posZ = spawnChunkZ * 16 + 8;

                    WorldChunkManager wcm = world.getWorldChunkManager();
                    if (wcm != null) {
                        BiomeGenBase at = wcm.getBiomeGenAt(posX, posZ);
                        if (at == BiomeGenBase.deepOcean) {
                            if (wcm.areBiomesViable(posX, posZ, 29, VALID_OCEAN_BIOMES)) {
                                return new ChunkPosition(posX, 56, posZ);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
