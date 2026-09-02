package lol.gzmc.locatebackport.locate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lol.gzmc.locatebackport.api.IStructureProvider;
import lol.gzmc.locatebackport.api.LocateAPI;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.ChunkProviderHell;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;

public class StructureLocator {

    private static final Map<String, String> CANONICAL_NAMES = new LinkedHashMap<String, String>();
    private static final List<String> KNOWN_STRUCTURES = new ArrayList<String>();

    static {
        registerCanonical("village", "Village");
        registerCanonical("stronghold", "Stronghold");
        registerCanonical("mineshaft", "Mineshaft");
        registerCanonical("temple", "Temple");
        registerCanonical("desert_pyramid", "Temple");
        registerCanonical("jungle_pyramid", "Temple");
        registerCanonical("swamp_hut", "Temple");
        registerCanonical("witch_hut", "Temple");
        registerCanonical("fortress", "Fortress");
        registerCanonical("nether_fortress", "Fortress");
        registerCanonical("nether_bridge", "Fortress");
        registerCanonical("monument", "Monument");
        registerCanonical("ocean_monument", "Monument");
        registerCanonical("oceanmonument", "Monument");

        KNOWN_STRUCTURES.add("Village");
        KNOWN_STRUCTURES.add("Stronghold");
        KNOWN_STRUCTURES.add("Mineshaft");
        KNOWN_STRUCTURES.add("Temple");
        KNOWN_STRUCTURES.add("Fortress");
    }

    private static void registerCanonical(String alias, String canonical) {
        CANONICAL_NAMES.put(alias.toLowerCase(Locale.ROOT), canonical);
    }

    public static List<String> getAvailableStructures(World world) {
        List<String> list = new ArrayList<String>(KNOWN_STRUCTURES);

        if (EtFuturumLocator.isLoaded()) {
            for (String ef : EtFuturumLocator.getAvailableStructures()) {
                if (!list.contains(ef)) {
                    list.add(ef);
                }
            }
        }

        if (OreSpawnLocator.isOreSpawnLoaded()) {
            for (String os : OreSpawnLocator.getOreSpawnStructures()) {
                if (!list.contains(os)) {
                    list.add(os);
                }
            }
        }

        for (IStructureProvider provider : LocateAPI.getStructureProviders()) {
            String name = provider.getName();
            if (name != null && !name.isEmpty() && !list.contains(name)) {
                list.add(name);
            }
            if (provider.getAliases() != null) {
                for (String alias : provider.getAliases()) {
                    if (alias != null && !alias.isEmpty() && !list.contains(alias)) {
                        list.add(alias);
                    }
                }
            }
        }

        IChunkProvider provider = getEffectiveChunkProvider(world);
        if (provider != null) {
            for (Field f : provider.getClass().getDeclaredFields()) {
                if (MapGenStructure.class.isAssignableFrom(f.getType())) {
                    String name = f.getType().getSimpleName().replace("MapGen", "");
                    if (!list.contains(name) && !name.isEmpty()) {
                        list.add(name);
                    }
                }
            }
        }
        return list;
    }

    public static String resolveStructureName(String input) {
        if (input == null) {
            return null;
        }
        String clean = input.trim().toLowerCase(Locale.ROOT);
        if (CANONICAL_NAMES.containsKey(clean)) {
            return CANONICAL_NAMES.get(clean);
        }
        for (String known : KNOWN_STRUCTURES) {
            if (known.equalsIgnoreCase(input)) {
                return known;
            }
        }
        for (IStructureProvider p : LocateAPI.getStructureProviders()) {
            if (p.getName() != null && p.getName().equalsIgnoreCase(input)) {
                return p.getName();
            }
            if (p.getAliases() != null) {
                for (String a : p.getAliases()) {
                    if (a.equalsIgnoreCase(input)) {
                        return p.getName();
                    }
                }
            }
        }
        return input;
    }

    public static ChunkPosition findNearestStructure(World world, String name, int x, int y, int z) {
        if (world == null || name == null) {
            return null;
        }

        String resolved = resolveStructureName(name);

        for (IStructureProvider p : LocateAPI.getStructureProviders()) {
            boolean matches = (p.getName() != null && p.getName().equalsIgnoreCase(resolved))
                    || (p.getAliases() != null && p.getAliases().contains(resolved.toLowerCase(Locale.ROOT)));
            if (matches) {
                ChunkPosition pos = p.findNearest(world, resolved, x, y, z, 500);
                if (pos != null) {
                    return pos;
                }
            }
        }

        if (EtFuturumLocator.isLoaded()) {
            ChunkPosition efPos = EtFuturumLocator.findEtFuturumStructure(world, resolved, x, y, z, 500);
            if (efPos != null) {
                return efPos;
            }
        }

        if (OreSpawnLocator.isOreSpawnLoaded()) {
            ChunkPosition osPos = OreSpawnLocator.findOreSpawnStructure(world, resolved, x, y, z, 300);
            if (osPos != null) {
                return osPos;
            }
        }

        IChunkProvider effective = getEffectiveChunkProvider(world);
        if (effective == null) {
            return null;
        }

        MapGenStructure generator = findGenerator(effective, resolved);
        if (generator != null) {
            try {
                return generator.func_151545_a(world, x, y, z);
            } catch (Throwable ignored) {}
        }

        return null;
    }

    private static IChunkProvider getEffectiveChunkProvider(World world) {
        IChunkProvider provider = world.getChunkProvider();
        if (provider instanceof ChunkProviderServer) {
            try {
                Field f = ChunkProviderServer.class.getDeclaredField("currentChunkProvider");
                f.setAccessible(true);
                return (IChunkProvider) f.get(provider);
            } catch (Throwable t1) {
                try {
                    Field f = ChunkProviderServer.class.getDeclaredField("field_73246_d");
                    f.setAccessible(true);
                    return (IChunkProvider) f.get(provider);
                } catch (Throwable ignored) {}
            }
        }
        return provider;
    }

    private static MapGenStructure findGenerator(IChunkProvider provider, String name) {
        String lower = name.toLowerCase(Locale.ROOT);

        if (provider instanceof ChunkProviderGenerate) {
            ChunkProviderGenerate gen = (ChunkProviderGenerate) provider;
            if (lower.contains("village")) return getField(gen, MapGenVillage.class);
            if (lower.contains("stronghold")) return getField(gen, MapGenStronghold.class);
            if (lower.contains("mineshaft")) return getField(gen, MapGenMineshaft.class);
            if (lower.contains("temple") || lower.contains("pyramid") || lower.contains("hut")) {
                return getField(gen, MapGenScatteredFeature.class);
            }
        } else if (provider instanceof ChunkProviderHell) {
            ChunkProviderHell hell = (ChunkProviderHell) provider;
            if (lower.contains("fortress") || lower.contains("bridge")) {
                return getField(hell, MapGenNetherBridge.class);
            }
        }

        for (Field f : provider.getClass().getDeclaredFields()) {
            if (MapGenStructure.class.isAssignableFrom(f.getType())) {
                if (f.getType().getSimpleName().toLowerCase(Locale.ROOT).contains(lower)) {
                    f.setAccessible(true);
                    try {
                        return (MapGenStructure) f.get(provider);
                    } catch (Throwable ignored) {}
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends MapGenStructure> T getField(Object target, Class<T> clazz) {
        for (Field f : target.getClass().getDeclaredFields()) {
            if (clazz.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                try {
                    return (T) f.get(target);
                } catch (Throwable ignored) {}
            }
        }
        return null;
    }
}
