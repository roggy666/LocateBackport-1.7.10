package lol.gzmc.locatebackport.command;

import java.util.ArrayList;
import java.util.List;

import lol.gzmc.locatebackport.locate.BiomeLocator;
import lol.gzmc.locatebackport.locate.StructureLocator;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class CommandLocate extends CommandBase {

    @Override
    public String getCommandName() {
        return "locate";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.locate.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            throw new WrongUsageException("commands.locate.usage");
        }

        World world = sender.getEntityWorld();
        ChunkCoordinates center = sender.getPlayerCoordinates();

        String sub = args[0].toLowerCase();

        if (sub.equals("structure")) {
            if (args.length < 2) {
                throw new WrongUsageException("commands.locate.usage");
            }
            locateStructure(sender, world, center, args[1]);
        } else if (sub.equals("biome")) {
            if (args.length < 2) {
                throw new WrongUsageException("commands.locate.usage");
            }
            locateBiome(sender, world, center, args[1]);
        } else {
            String target = args[0];
            String resolved = StructureLocator.resolveStructureName(target);
            ChunkPosition pos = StructureLocator.findNearestStructure(world, resolved, center.posX, center.posY, center.posZ);

            if (pos != null) {
                sendSuccess(sender, "commands.locate.structure.success", resolved, pos.chunkPosX, -1, pos.chunkPosZ, center);
                return;
            }

            BiomeGenBase biome = BiomeLocator.resolveBiome(target);
            if (biome != null) {
                locateBiome(sender, world, center, target);
                return;
            }

            throw new CommandException("commands.locate.structure.not_found", target);
        }
    }

    private void locateStructure(ICommandSender sender, World world, ChunkCoordinates center, String name) {
        String resolved = StructureLocator.resolveStructureName(name);
        ChunkPosition pos = StructureLocator.findNearestStructure(world, resolved, center.posX, center.posY, center.posZ);

        if (pos == null) {
            throw new CommandException("commands.locate.structure.not_found", name);
        }

        sendSuccess(sender, "commands.locate.structure.success", resolved, pos.chunkPosX, -1, pos.chunkPosZ, center);
    }

    private void locateBiome(ICommandSender sender, World world, ChunkCoordinates center, String name) {
        BiomeGenBase biome = BiomeLocator.resolveBiome(name);
        if (biome == null) {
            throw new CommandException("commands.locate.biome.invalid", name);
        }

        ChunkPosition pos = BiomeLocator.findNearestBiome(world, biome, center.posX, center.posZ, BiomeLocator.DEFAULT_SEARCH_RADIUS);
        if (pos == null) {
            throw new CommandException("commands.locate.biome.not_found", biome.biomeName, BiomeLocator.DEFAULT_SEARCH_RADIUS);
        }

        sendSuccess(sender, "commands.locate.biome.success", biome.biomeName, pos.chunkPosX, -1, pos.chunkPosZ, center);
    }

    private void sendSuccess(ICommandSender sender, String key, String name, int x, int y, int z, ChunkCoordinates from) {
        int dx = x - from.posX;
        int dz = z - from.posZ;
        int distance = MathHelper.floor_double(Math.sqrt(dx * dx + dz * dz));

        String yDisplay = (y >= 0) ? String.valueOf(y) : "~";
        String coordText = "[" + x + ", " + yDisplay + ", " + z + "]";

        ChatComponentText coords = new ChatComponentText(coordText);
        ChatStyle style = new ChatStyle();
        style.setColor(EnumChatFormatting.GREEN);
        String targetName = sender.getCommandSenderName();
        String tpCmd = (targetName != null && !targetName.isEmpty())
                ? "/tp " + targetName + " " + x + " " + yDisplay + " " + z
                : "/tp " + x + " " + yDisplay + " " + z;
        style.setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tpCmd));
        style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentTranslation("chat.coordinates.tooltip")));
        coords.setChatStyle(style);

        ChatComponentTranslation result = new ChatComponentTranslation(key, name, coords, distance);
        sender.addChatMessage(result);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<String>();
            options.add("structure");
            options.add("biome");
            options.addAll(StructureLocator.getAvailableStructures(sender.getEntityWorld()));
            return getListOfStringsMatchingLastWord(args, options.toArray(new String[0]));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("structure")) {
                List<String> structures = StructureLocator.getAvailableStructures(sender.getEntityWorld());
                return getListOfStringsMatchingLastWord(args, structures.toArray(new String[0]));
            } else if (args[0].equalsIgnoreCase("biome")) {
                List<String> biomes = BiomeLocator.getAvailableBiomeNames();
                return getListOfStringsMatchingLastWord(args, biomes.toArray(new String[0]));
            }
        }
        return null;
    }
}
