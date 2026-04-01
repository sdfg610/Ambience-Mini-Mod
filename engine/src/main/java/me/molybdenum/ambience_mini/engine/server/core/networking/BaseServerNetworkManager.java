package me.molybdenum.ambience_mini.engine.server.core.networking;

import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.areas.AreaOperation;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.bidirectional.*;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client.PutNameCacheMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server.*;
import me.molybdenum.ambience_mini.engine.shared.utils.AmVersion;
import me.molybdenum.ambience_mini.engine.shared.utils.Result;

import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseServerNetworkManager<TServerPlayer>
{
    // Client versioning
    private final Object lock = new Object();
    private final ConcurrentHashMap<TServerPlayer, AmVersion> playerToVersion = new ConcurrentHashMap<>();

    // Core functionality
    private BaseServerCore<TServerPlayer, ?, ?> core = null;


    public void init(BaseServerCore<TServerPlayer, ?, ?> core) {
        if (this.core != null)
            throw new RuntimeException("Multiple calls to 'BaseServerNetworkManager.init'!");
        this.core = core;

        this.core.areaManager.addUpdateListener(this::notifyAreaUpdate);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract void sendToPlayer(AmMessage message, TServerPlayer player);

    protected abstract String getPlayerUUID(TServerPlayer player);


    // -----------------------------------------------------------------------------------------------------------------
    // Message handling
    public void handleMessage(Result<AmMessage> msgRes, TServerPlayer sender) {
        if (!msgRes.isSuccess()) {
            core.logger.error("Error during retrieval of message: {}", msgRes.error);
            return;
        }

        AmMessage message = msgRes.value;
        AmMessage response;
        try {
            if (message instanceof ClientInfoMessage msg)
                response = handleModVersionMessage(msg, sender);

            else if (message instanceof CreateAreaMessage msg)
                response = handleCreateAreaMessage(msg, sender);
            else if (message instanceof PutAreaMessage msg)
                response = handlePutAreaMessage(msg, sender);
            else if (message instanceof DeleteAreaMessage msg)
                response = handleDeleteAreaMessage(msg, sender);
            else if (message instanceof RequestAreasMessage msg)
                response = handleRequestAreasMessage(msg, sender);

            else if (message instanceof GetNameCacheMessage msg)
                response = handleGetNameCacheMessage(msg, sender);

            else {
                core.logger.error("Server could not handle message of type '{}'", message.getClass().getName());
                response = message.failure(AmLang.MSG_UNHANDLED_CLIENT_MESSAGE);
            }
        }
        catch (Exception e) {
            core.logger.error("An exception was thrown during message-handling.", e);
            response = message.failure(AmLang.MSG_MESSAGE_CAUSED_SERVER_ERROR);
        }

        if (response != null && message.hasHandler())
            sendToPlayer(response, sender);
    }


    // Basic info
    private AmMessage handleModVersionMessage(ClientInfoMessage msg, TServerPlayer sender) {
        setPlayerModVersion(sender, AmVersion.ofString(msg.modVersion));
        core.nameCache.putPlayerName(msg.playerUUID, msg.playerName);
        return msg.success();
    }


    // Areas
    private AmMessage handleCreateAreaMessage(CreateAreaMessage msg, TServerPlayer sender) {
        core.areaManager.createArea(msg.area);
        return msg.success();
    }

    private AmMessage handlePutAreaMessage(PutAreaMessage msg, TServerPlayer sender) {
        if (!msg.area.canBeEditedBy(getPlayerUUID(sender)))
            return msg.failure(AmLang.MSG_AREA_CANNOT_EDIT);

        core.areaManager.putArea(msg.area);
        return msg.success();
    }

    private AmMessage handleDeleteAreaMessage(DeleteAreaMessage msg, TServerPlayer sender) {
        Area area = core.areaManager.getAreaById(msg.areaId);
        if (area.canBeEditedBy(getPlayerUUID(sender)))
            return msg.failure(AmLang.MSG_AREA_CANNOT_EDIT);

        core.areaManager.deleteArea(msg.areaId);
        return msg.success();
    }

    private AmMessage handleRequestAreasMessage(RequestAreasMessage msg, TServerPlayer sender) {
        core.areaManager.getAreasVisibleTo(getPlayerUUID(sender)).forEach(
                area -> sendToPlayer(new PutAreaMessage(area, false), sender)
        );

        return msg.success();
    }


    // Name caching
    private AmMessage handleGetNameCacheMessage(GetNameCacheMessage msg, TServerPlayer sender) {
        String uuid = msg.playerUuid;
        String name = core.nameCache.getPlayerName(uuid);
        sendToPlayer(new PutNameCacheMessage(uuid, name), sender);
        return msg.success();
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Notification to players
    public void notifyAreaUpdate(Area area, AreaOperation operation) {
        playerToVersion.forEach((player, version) -> {
            if (version.isGreaterThanOrEqual(AmVersion.V_2_5_0)) {
                switch (operation) {
                    case PUT -> {
                        AmMessage msg = area.canBeSeenBy(getPlayerUUID(player))
                                ? new PutAreaMessage(area)
                                : new DeleteAreaMessage(area.id);
                        sendToPlayer(msg, player);
                    }
                    case DELETE -> sendToPlayer(new DeleteAreaMessage(area.id), player);
                }
            }
        });
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Client info management
    public void setPlayerModVersion(TServerPlayer player, AmVersion version) {
        synchronized (lock) {
            playerToVersion.put(player, version);
        }
    }

    public void renewPlayerModVersion(TServerPlayer oldPlayer, TServerPlayer newPlayer) {
        synchronized (lock) {
            var info = playerToVersion.remove(oldPlayer);
            if (info != null)
                playerToVersion.put(newPlayer, info);
        }
    }

    public void removePlayerModVersion(TServerPlayer player) {
        synchronized (lock) {
            playerToVersion.remove(player);
        }
    }


    public AmVersion getPlayerModVersion(TServerPlayer player) {
        synchronized (lock) {
            return playerToVersion.getOrDefault(player, AmVersion.ZERO);
        }
    }
}
