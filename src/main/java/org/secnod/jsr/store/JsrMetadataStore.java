package org.secnod.jsr.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Collection;

import org.secnod.jsr.JsrMetadata;
import org.secnod.jsr.JsrStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class JsrMetadataStore {

    public static final String FILENAME = "JsrMetadata.json";

    public static Collection<JsrMetadata> loadJson(Reader r) throws IOException {
        JsonDeserializer<JsrStatus> statusDeserializer = new JsonDeserializer<JsrStatus>() {

            @Override
            public JsrStatus deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                return JsrStatus.parse(json.getAsString());
            }

        };
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JsrStatus.class, statusDeserializer)
                .create();
        Type type = new TypeToken<Collection<JsrMetadata>>(){}.getType();
        return gson.fromJson(r, type);
    }

    public static Collection<JsrMetadata> loadJson() throws IOException {
        try (Reader r = new InputStreamReader(JsrMetadataStore.class.getResourceAsStream(FILENAME), "UTF-8")) {
            return loadJson(r);
        }
    }

    public static Collection<JsrMetadata> loadJson(File jsrMetadata) throws IOException, FileNotFoundException {
        try (Reader fileReader = new InputStreamReader(new FileInputStream(jsrMetadata), "UTF-8")) {
            return loadJson(fileReader);
        }
    }

    public static void writeMetadata(Collection<JsrMetadata> metadata, Writer target) {
        JsonSerializer<JsrStatus> statusSerializer = new JsonSerializer<JsrStatus>() {

            @Override
            public JsonElement serialize(JsrStatus src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.label());
            }
        };
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JsrStatus.class, statusSerializer)
                .setPrettyPrinting()
                .create();
        gson.toJson(metadata, target);
    }
}
