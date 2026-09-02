package lol.gzmc.locatebackport.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocateAPI {

    private static final List<IStructureProvider> STRUCTURE_PROVIDERS = new ArrayList<IStructureProvider>();
    private static final List<IBiomeProvider> BIOME_PROVIDERS = new ArrayList<IBiomeProvider>();

    public static synchronized void registerStructureProvider(IStructureProvider provider) {
        if (provider != null && !STRUCTURE_PROVIDERS.contains(provider)) {
            STRUCTURE_PROVIDERS.add(provider);
        }
    }

    public static synchronized void registerBiomeProvider(IBiomeProvider provider) {
        if (provider != null && !BIOME_PROVIDERS.contains(provider)) {
            BIOME_PROVIDERS.add(provider);
        }
    }

    public static List<IStructureProvider> getStructureProviders() {
        return Collections.unmodifiableList(STRUCTURE_PROVIDERS);
    }

    public static List<IBiomeProvider> getBiomeProviders() {
        return Collections.unmodifiableList(BIOME_PROVIDERS);
    }
}
