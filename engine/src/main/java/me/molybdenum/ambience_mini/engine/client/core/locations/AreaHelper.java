package me.molybdenum.ambience_mini.engine.client.core.locations;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.Cube;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.bidirectional.DeleteAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.bidirectional.PutAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server.CreateAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.AmVersion;

import java.util.function.Supplier;

public class AreaHelper {
    private final ClientAreaManager areaManager;
    private final BaseClientNetworkManager networkManager;
    private final ServerSetup serverSetup;

    private final Supplier<String> getPlayerUUID;
    private final Supplier<Cube> getSelectedCube;


    @SuppressWarnings("rawtypes")
    public AreaHelper(BaseClientCore core, Supplier<Cube> getSelectedCube) {
        this.serverSetup = core.serverSetup;
        this.areaManager = core.areaManager;
        this.networkManager = core.networkManager;
        this.getPlayerUUID = core.nameCache::getCurrentPlayerUUID;
        this.getSelectedCube = getSelectedCube;
    }


    public String getPlayerUUID() {
        return getPlayerUUID.get();
    }

    public Cube getSelectedCube() {
        return getSelectedCube.get();
    }


    public void submitArea(Area area, Runnable onSuccess, Runnable onFailure) {
        // TODO: Handle local areas
        // TODO: Handle a non-local area being updated to local and vice versa.

        if (!area.owner.isLocal() && serverSetup.serverVersion.isGreaterThanOrEqual(AmVersion.V_2_5_0)) {
            AmMessage message = area.isNew()
                    ? new CreateAreaMessage(area)
                    : new PutAreaMessage(area);
            networkManager.sendToServer(message, onSuccess, onFailure);
        }
    }

    public void deleteArea(int id, Runnable onSuccess, Runnable onFailure) {
        // TODO: Handle local areas

        if (!areaManager.getAreaById(id).owner.isLocal() && serverSetup.serverVersion.isGreaterThanOrEqual(AmVersion.V_2_5_0)) {
            AmMessage message = new DeleteAreaMessage(id);
            networkManager.sendToServer(message, onSuccess, onFailure);
        }
    }
}
