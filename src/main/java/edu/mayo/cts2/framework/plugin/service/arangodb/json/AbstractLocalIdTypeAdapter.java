package edu.mayo.cts2.framework.plugin.service.arangodb.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import edu.mayo.cts2.framework.model.extension.LocalIdResource;
import edu.mayo.cts2.framework.plugin.service.arangodb.ArangoDbServiceConstants;

import java.lang.reflect.Type;

public abstract class AbstractLocalIdTypeAdapter<T extends LocalIdResource<R>,R> implements JsonDeserializer<T>, JsonSerializer<T> {

    public static final String LOCAL_ID_PROPERTY = ArangoDbServiceConstants.LOCAL_ID_PROP;

    @Override
    public final T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        R valueSetDefinition = context.deserialize(json, this.getResourceClass());

        return this.toLocalIdResource(json.getAsJsonObject().getAsJsonPrimitive(LOCAL_ID_PROPERTY).getAsString(), valueSetDefinition);
    }

    @Override
    public final JsonElement serialize(T object, Type typeOfT, JsonSerializationContext context) throws JsonParseException {
        JsonObject element = context.serialize(object.getResource()).getAsJsonObject();

        element.addProperty(LOCAL_ID_PROPERTY, object.getLocalID());

        return this.decorate(element, object);
    }

    protected JsonObject decorate(JsonObject jsonObject, T object) {
        return jsonObject;
    }

    protected abstract Class<R> getResourceClass();

    protected abstract T toLocalIdResource(String asString, R valueSetDefinition);

}
