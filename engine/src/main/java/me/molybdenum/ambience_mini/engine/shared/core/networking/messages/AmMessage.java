package me.molybdenum.ambience_mini.engine.shared.core.networking.messages;

import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.FailureMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.SuccessMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.ResponseMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.helper.HelperWriter;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;

import java.util.List;


public abstract class AmMessage implements AmSerializable {
    public int handlerID = Integer.MIN_VALUE;


    public boolean hasHandler() {
        return handlerID != Integer.MIN_VALUE;
    }


    // Old responses
    public FailureMessage failure(AmLang key, String... args) {
        return failure(Text.ofTranslatable(key, args));
    }

    public FailureMessage failure(Text message) {
        return new FailureMessage(handlerID, message);
    }

    public SuccessMessage success() {
        return new SuccessMessage(handlerID);
    }


    // New responses
    public ResponseMessage failWith(AmLang key, String... args) {
        return failWith(Text.ofTranslatable(key, args));
    }

    public ResponseMessage failWith(String text) {
        return failWith(Text.ofLiteral(text));
    }

    public ResponseMessage failWith(Text text) {
        return new ResponseMessage(handlerID, text);
    }


    public ResponseMessage succeedWith(byte[] data) {
        return new ResponseMessage(handlerID, data);
    }

    public <T extends AmSerializable> ResponseMessage succeedWith(T data) {
        return succeedWith(data.toBytes());
    }

    public <T extends AmSerializable> ResponseMessage succeedWith(List<T> list) {
        var writer = new HelperWriter();
        writer.writeList(list);
        return succeedWith(writer.getBytes());
    }
}
