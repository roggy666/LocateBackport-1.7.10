package lol.gzmc.locatebackport;

import lol.gzmc.locatebackport.api.IBiomeProvider;
import lol.gzmc.locatebackport.api.IStructureProvider;
import lol.gzmc.locatebackport.api.LocateAPI;
import lol.gzmc.locatebackport.command.CommandLocate;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
        modid = LocateBackport.MODID,
        name = "LocateBackport",
        version = "1.0.0",
        acceptableRemoteVersions = "*"
)
public class LocateBackport {

    public static final String MODID = "locatebackport";

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
    }

    @Mod.EventHandler
    public void onIMC(FMLInterModComms.IMCEvent event) {
        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
            if ("registerStructureProvider".equalsIgnoreCase(message.key)) {
                if (message.isStringMessage()) {
                    tryRegisterStructureProvider(message.getStringValue());
                }
            } else if ("registerBiomeProvider".equalsIgnoreCase(message.key)) {
                if (message.isStringMessage()) {
                    tryRegisterBiomeProvider(message.getStringValue());
                }
            }
        }
    }

    private void tryRegisterStructureProvider(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (IStructureProvider.class.isAssignableFrom(clazz)) {
                IStructureProvider inst = (IStructureProvider) clazz.newInstance();
                LocateAPI.registerStructureProvider(inst);
            }
        } catch (Throwable ignored) {
        }
    }

    private void tryRegisterBiomeProvider(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (IBiomeProvider.class.isAssignableFrom(clazz)) {
                IBiomeProvider inst = (IBiomeProvider) clazz.newInstance();
                LocateAPI.registerBiomeProvider(inst);
            }
        } catch (Throwable ignored) {
        }
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandLocate());
    }
}
