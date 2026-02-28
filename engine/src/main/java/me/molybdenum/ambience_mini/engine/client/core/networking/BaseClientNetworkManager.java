package me.molybdenum.ambience_mini.engine.client.core.networking;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.bidirectional.*;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client.FailureMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client.MobTargetMessage;

public abstract class BaseClientNetworkManager
{
    @SuppressWarnings("rawtypes")
    private BaseClientCore core = null;


    @SuppressWarnings("rawtypes")
    public void init(BaseClientCore core) {
        if (this.core != null)
            throw new RuntimeException("Multiple calls to 'BaseClientNetworkManager.init'!");
        this.core = core;
    }


    public abstract void sendToServer(AmMessage message);


    public void handleMessage(AmMessage message) {
        if (message instanceof FailureMessage msg)
            handleFailureMessage(msg);
        else if (message instanceof MobTargetMessage msg)
            handleMobTargetMessage(msg);
        else if (message instanceof PutAreaMessage msg)
            handlePutAreaMessage(msg);
        else if (message instanceof DeleteAreaMessage msg)
            handleDeleteAreaMessage(msg);
        else {
            core.notification.printToChat(AmLang.MSG_UNHANDLED_MESSAGE);
            core.logger.error("Client network handler could not handle message of type: {}", message.getClass().getName());
        }
    }

    private void handleFailureMessage(FailureMessage msg) {
        // TODO
    }

    private void handleMobTargetMessage(MobTargetMessage msg) {
        if (msg.isTargetingPlayer)
            core.combatState.tryAddCombatantById(msg.entityID, false);
        else
            core.combatState.removeCombatant(msg. entityID);
    }

    private void handlePutAreaMessage(PutAreaMessage msg) {
        // TODO:
    }

    private void handleDeleteAreaMessage(DeleteAreaMessage msg) {
        // TODO:
    }
}
