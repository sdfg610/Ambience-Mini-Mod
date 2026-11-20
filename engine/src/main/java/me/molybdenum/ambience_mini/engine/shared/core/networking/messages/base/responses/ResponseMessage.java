package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.responses;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;

public class ResponseMessage extends AmMessage
{
    public Response response;


    public ResponseMessage(int handlerID, byte[] data) {
        this.handlerID = handlerID;
        this.response = new Response(data);
    }

    public ResponseMessage(int handlerID, Text error) {
        this.handlerID = handlerID;
        this.response = new Response(error);
    }


    public ResponseMessage(AmReader reader) {
        this.response = new Response(reader); // Reading and writing of handlerID is handled in AmSerializer
    }


    @Override
    public void writeTo(AmWriter writer) {
        response.writeTo(writer);
    }
}
