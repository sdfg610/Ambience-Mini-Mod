package me.molybdenum.ambience_mini.v1_20_1.server.core;

import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.server.core.flags.FlagManager;
import me.molybdenum.ambience_mini.engine.server.core.locations.ServerAreaManager;
import me.molybdenum.ambience_mini.engine.server.core.util.ServerNameCache;
import me.molybdenum.ambience_mini.v1_20_1.server.core.locations.StructureReader;
import me.molybdenum.ambience_mini.v1_20_1.server.core.networking.ServerNetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.nio.file.Path;

public class ServerCore extends BaseServerCore<
        ServerPlayer,
        ServerNetworkManager,
        StructureReader,
        ServerAreaManager
> {
    private final MinecraftServer server;


    public ServerCore(
            MinecraftServer server,
            Logger logger,
            ServerNameCache nameCache,
            ServerAreaManager areaManager,
            StructureReader structureReader,
            FlagManager flagManager,
            ServerNetworkManager networkManager
    ) {
        super(logger, nameCache, areaManager, structureReader, flagManager, networkManager);

        this.server = server;
    }


    @Override
    public Path getWorldRootPath() {
        return server.getWorldPath(LevelResource.ROOT).normalize();
    }
}
