package lol.gzmc.locatebackport.locate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import cpw.mods.fml.common.Loader;
import lol.gzmc.locatebackport.api.IStructureProvider;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public class OreSpawnLocator implements IStructureProvider {

    private static Boolean loaded = null;
    private static final Map<String, String> ORESPAWN_STRUCTURES = new LinkedHashMap<String, String>();

    static {
        register("KingAltar", "KingAltar");
        register("the_king", "KingAltar");
        register("theking", "KingAltar");
        register("DiamondTree", "DiamondTree");
        register("diamond_tree", "DiamondTree");
        register("diamondtree", "DiamondTree");
        register("RubyTree", "RubyTree");
        register("ruby_tree", "RubyTree");
        register("rubytree", "RubyTree");
        register("RubyDungeon", "RubyDungeon");
        register("ruby_dungeon", "RubyDungeon");
        register("rubydungeon", "RubyDungeon");

        register("BasiliskMaze", "BasiliskMaze");
        register("KyuubiDungeon", "KyuubiDungeon");
        register("BeeHive", "BeeHive");
        register("ShadowDungeon", "ShadowDungeon");
        register("Alien", "Alien");
        register("EnderKnight", "EnderKnight");
        register("LeonNest", "LeonNest");

        register("OreSpawnCastle", "OreSpawnCastle");
        register("EnderCastle", "EnderCastle");
        register("IncaPyramid", "IncaPyramid");
        register("RobotLab", "RobotLab");
        register("WhiteHouse", "WhiteHouse");
        register("Greenhouse", "Greenhouse");
        register("NightmareRookery", "NightmareRookery");
        register("CephadromeAltar", "CephadromeAltar");
        register("StinkyHouse", "StinkyHouse");
        register("Rainbow", "Rainbow");

        register("HauntedHouse", "HauntedHouse");
        register("BouncyCastle", "BouncyCastle");
        register("GirlfriendIsland", "GirlfriendIsland");
        register("MonsterIsland", "MonsterIsland");
        register("WaterDragonLair", "WaterDragonLair");
        register("GoldFishBowl", "GoldFishBowl");
        register("FrogPond", "FrogPond");
        register("PlayPool", "PlayPool");
        register("RubberDuckyPond", "RubberDuckyPond");

        register("CrystalBattleTower", "CrystalBattleTower");
        register("CrystalHauntedHouse", "CrystalHauntedHouse");
        register("FairyTree", "FairyTree");
    }

    private static void register(String key, String canonical) {
        ORESPAWN_STRUCTURES.put(key.toLowerCase(Locale.ROOT), canonical);
    }

    @Override
    public String getName() {
        return "OreSpawn";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<String>(ORESPAWN_STRUCTURES.keySet());
    }

    @Override
    public ChunkPosition findNearest(World world, String requestedName, int playerX, int playerY, int playerZ, int maxRadiusChunks) {
        return findOreSpawnStructure(world, requestedName, playerX, playerY, playerZ, maxRadiusChunks);
    }

    public static boolean isOreSpawnLoaded() {
        if (loaded == null) {
            loaded = Loader.isModLoaded("OreSpawn") || Loader.isModLoaded("orespawn") || hasClass("danger.orespawn.OreSpawnMain");
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

    public static List<String> getOreSpawnStructures() {
        if (!isOreSpawnLoaded()) {
            return Collections.emptyList();
        }
        List<String> unique = new ArrayList<String>();
        for (String val : ORESPAWN_STRUCTURES.values()) {
            if (!unique.contains(val)) {
                unique.add(val);
            }
        }
        return unique;
    }

    public static ChunkPosition findOreSpawnStructure(World world, String name, int playerX, int playerY, int playerZ, int maxChunkRadius) {
        if (!isOreSpawnLoaded() || world == null || name == null) {
            return null;
        }

        String target = name.toLowerCase(Locale.ROOT).replace("_", "").replace(" ", "");
        int centerChunkX = playerX >> 4;
        int centerChunkZ = playerZ >> 4;
        long worldSeed = world.getSeed();

        Random fmlRandom = new Random(worldSeed);
        long xSeed = fmlRandom.nextLong() >> 2 + 1L;
        long zSeed = fmlRandom.nextLong() >> 2 + 1L;

        for (int r = 0; r <= maxChunkRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) {
                        continue;
                    }
                    int cx = centerChunkX + dx;
                    int cz = centerChunkZ + dz;

                    long chunkSeed = (xSeed * cx + zSeed * cz) ^ worldSeed;
                    Random rand = new Random(chunkSeed);

                    if (matches(target, world, rand, cx, cz)) {
                        return new ChunkPosition(cx * 16 + 8, playerY, cz * 16 + 8);
                    }
                }
            }
        }

        return null;
    }

    private static boolean matches(String target, World world, Random rand, int chunkX, int chunkZ) {
        if (target.contains("king") || target.contains("altar")) {
            return rand.nextInt(2000) == 1;
        }

        if (target.contains("diamondtree") || target.equals("diamond_tree") || target.equals("diamond")) {
            if (rand.nextInt(50) == 0) {
                rand.nextInt(8);
                rand.nextInt(8);
                rand.nextInt(4);
                rand.nextInt(2);
                rand.nextInt(100);
                int j1 = rand.nextInt(100);
                if (j1 == 0) {
                    return rand.nextInt(2) == 0;
                }
            }
            return false;
        }

        if (target.contains("rubytree") || target.equals("ruby_tree") || target.equals("ruby")) {
            if (rand.nextInt(50) == 0) {
                rand.nextInt(8);
                rand.nextInt(8);
                rand.nextInt(4);
                rand.nextInt(2);
                rand.nextInt(100);
                int j1 = rand.nextInt(100);
                if (j1 == 0) {
                    return rand.nextInt(2) == 1;
                }
            }
            return false;
        }

        if (target.contains("rubydungeon") || target.contains("rubybird")) {
            return rand.nextInt(15) == 0;
        }

        if (target.contains("basilisk") || target.contains("maze")) {
            return rand.nextInt(95) == 1 && rand.nextInt(7) == 0;
        }
        if (target.contains("kyuubi")) {
            return rand.nextInt(95) == 1 && rand.nextInt(7) == 1;
        }
        if (target.contains("bee") || target.contains("hive")) {
            return rand.nextInt(95) == 1 && rand.nextInt(7) == 2;
        }
        if (target.contains("shadow")) {
            return rand.nextInt(95) == 1 && rand.nextInt(7) == 3;
        }
        if (target.contains("alien")) {
            return rand.nextInt(95) == 1 && rand.nextInt(7) == 4;
        }
        if (target.contains("knight")) {
            return rand.nextInt(95) == 1 && rand.nextInt(7) == 5;
        }
        if (target.contains("leon")) {
            return rand.nextInt(95) == 1 && rand.nextInt(7) == 6;
        }

        if (target.contains("castle") && !target.contains("bouncy")) {
            if (rand.nextInt(100) == 0) {
                int i = rand.nextInt(19);
                return i < 3 || i == 7;
            }
            return false;
        }
        if (target.contains("endercastle")) {
            return rand.nextInt(100) == 0 && rand.nextInt(19) == 7;
        }
        if (target.contains("inca") || target.contains("pyramid")) {
            return rand.nextInt(100) == 0 && rand.nextInt(19) == 8;
        }
        if (target.contains("robot") || target.contains("lab")) {
            return rand.nextInt(100) == 0 && rand.nextInt(19) == 9;
        }
        if (target.contains("whitehouse")) {
            return rand.nextInt(100) == 0 && rand.nextInt(19) == 16;
        }
        if (target.contains("greenhouse")) {
            return rand.nextInt(100) == 0 && rand.nextInt(19) == 13;
        }
        if (target.contains("nightmare")) {
            return rand.nextInt(100) == 0 && rand.nextInt(19) == 14;
        }
        if (target.contains("cephadrome")) {
            return rand.nextInt(100) == 0 && rand.nextInt(19) == 12;
        }
        if (target.contains("stinky")) {
            return rand.nextInt(100) == 0 && rand.nextInt(19) == 15;
        }
        if (target.contains("rainbow")) {
            return rand.nextInt(100) == 0 && rand.nextInt(19) == 18;
        }

        if (target.contains("haunted")) {
            return rand.nextInt(285) == 0;
        }
        if (target.contains("bouncy")) {
            return rand.nextInt(230) == 0;
        }
        if (target.contains("girlfriend")) {
            return rand.nextInt(6) == 3 && rand.nextInt(300) == 0;
        }
        if (target.contains("monsterisland")) {
            return rand.nextInt(6) == 4;
        }
        if (target.contains("waterdragon") || target.contains("dragonlair")) {
            return rand.nextInt(6) == 1 && rand.nextInt(350) == 0;
        }
        if (target.contains("goldfish") || target.contains("bowl")) {
            return rand.nextInt(6) == 2 && rand.nextInt(300) == 0;
        }
        if (target.contains("frog") || target.contains("pond")) {
            return rand.nextInt(6) == 5;
        }
        if (target.contains("pool") || target.contains("playpool")) {
            return rand.nextInt(6) == 0;
        }
        if (target.contains("rubberducky")) {
            return rand.nextInt(6) == 0;
        }

        if (target.contains("crystaltower") || target.contains("battletower")) {
            return rand.nextInt(4) == 0;
        }
        if (target.contains("fairy") || target.contains("fairytree")) {
            return rand.nextInt(5) == 0;
        }

        return false;
    }
}
