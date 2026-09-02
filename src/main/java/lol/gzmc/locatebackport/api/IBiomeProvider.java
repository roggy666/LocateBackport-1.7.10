package lol.gzmc.locatebackport.api;

import java.util.List;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public interface IBiomeProvider {

    List<String> getSupportedBiomeNames();

    BiomeGenBase resolveBiome(String nameOrId);

    ChunkPosition findNearestBiome(World world, BiomeGenBase biome, int playerX, int playerZ, int maxRadiusBlocks);
}
