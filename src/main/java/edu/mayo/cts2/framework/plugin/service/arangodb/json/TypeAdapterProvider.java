package edu.mayo.cts2.framework.plugin.service.arangodb.json;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public interface TypeAdapterProvider<T> {

    <R extends JsonSerializer<T> & JsonDeserializer<T>> R getTypeAdapter();

    Class<T> getType();

}
