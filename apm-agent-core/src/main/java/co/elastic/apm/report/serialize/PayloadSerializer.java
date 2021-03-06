package co.elastic.apm.report.serialize;

import co.elastic.apm.impl.payload.Payload;
import okio.BufferedSink;

import java.io.IOException;

public interface PayloadSerializer {
    void serializePayload(BufferedSink sink, Payload payload) throws IOException;
}
