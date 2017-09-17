package org.secnod.jsr.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;

import org.secnod.jsr.Jsr;
import org.secnod.jsr.JsrId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

public class JsrDataStore {
    public static final String FILENAME = "JsrData.json";
    public final Collection<Jsr> jsrs;

    public JsrDataStore(Collection<Jsr> jsrs) {
        this.jsrs = jsrs;
    }

    public static Collection<Jsr> loadJson(Reader r) throws IOException {
        JsonDeserializer<JsrId> idDeserializer = new JsonDeserializer<JsrId>() {

            @Override
            public JsrId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                JsonPrimitive value = json.getAsJsonPrimitive();
                if (value.isNumber()) {
                    return JsrId.of(json.getAsInt());
                } else {
                    return JsrId.of(value.getAsString());
                }
            }
        };

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(JsrId.class, idDeserializer);
        Gson gson = builder.create();

        Type collectionType = new TypeToken<Collection<Jsr>>(){}.getType();
        Collection<Jsr> jsrsFromDisk = gson.fromJson(r, collectionType);
        return jsrsFromDisk;
    }

    public static Collection<Jsr> loadJson() throws IOException {
        try (Reader r = new InputStreamReader(JsrDataStore.class.getResourceAsStream(FILENAME), "UTF-8")) {
            return loadJson(r);
        }
    }

    public static Collection<Jsr> loadJson(File jsrData) throws IOException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(jsrData), "UTF-8")) {
            return loadJson(fileReader);
        }
    }
}
