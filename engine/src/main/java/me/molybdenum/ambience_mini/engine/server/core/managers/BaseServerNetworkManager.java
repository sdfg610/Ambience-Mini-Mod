package me.molybdenum.ambience_mini.engine.server.core.managers;

import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.bidirectional.*;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server.*;
import me.molybdenum.ambience_mini.engine.shared.utils.AmVersion;

public abstract class BaseServerNetworkManager<TServerPlayer>
{
    private BaseServerCore<TServerPlayer, ?, ?> core = null;


    public void init(BaseServerCore<TServerPlayer, ?, ?> core) {
        if (this.core != null)
            throw new RuntimeException("Multiple calls to 'BaseServerNetworkManager.init'!");
        this.core = core;
    }


    public abstract void sendToPlayer(AmMessage message, TServerPlayer player);


    public void handleMessage(AmMessage message, TServerPlayer sender) {
        if (message instanceof ModVersionMessage msg)
            handleModVersionMessage(msg, sender);
        else if (message instanceof CreateAreaMessage msg)
            handleCreateAreaMessage(msg, sender);
        else if (message instanceof PutAreaMessage msg)
            handlePutAreaMessage(msg, sender);
        else if (message instanceof DeleteAreaMessage msg)
            handleDeleteAreaMessage(msg, sender);
        else {
            core.logger.error("Server could not handle message of type '{}'", message.getClass().getName());
            // TODO: Error response to client
        }
    }


    private void handleModVersionMessage(ModVersionMessage msg, TServerPlayer sender) {
        AmVersion.tryOfString(msg.modVersion).ifPresentOrElse(
                version -> core.clientManager.setPlayer(sender, version),
                () -> {
                    core.logger.error("Server got malformed mod-version message: '{}'", msg.modVersion);
                    // TODO: Error response to client
                }
        );

    }

    private void handleCreateAreaMessage(CreateAreaMessage msg, TServerPlayer sender) {
        // TODO
    }

    private void handlePutAreaMessage(PutAreaMessage msg, TServerPlayer sender) {
        // TODO
    }

    private void handleDeleteAreaMessage(DeleteAreaMessage msg, TServerPlayer sender) {
        // TODO
    }
}
