package lol.gzmc.locatebackport.api;

import java.util.List;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public interface IStructureProvider {

    String getName();

    List<String> getAliases();

    ChunkPosition findNearest(World world, String requestedName, int playerX, int playerY, int playerZ, int maxRadiusChunks);
}
