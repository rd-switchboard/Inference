package org.rdswitchboard.libraries.scopus.deserilaize;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.Lists;

public class StringsDeserializer extends JsonDeserializer<String[]> {
	@Override
	public String[] deserialize(JsonParser jp, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		List<String> list = null;
		// If we have an string, just create an Date object from it
		if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
			list = Lists.newArrayList(jp.getValueAsString());
		} else if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
			jp.nextToken();
			
			list = Lists.newArrayList(jp.readValuesAs(String.class));
			
			if (jp.getCurrentToken() != JsonToken.END_ARRAY)
				throw new IOException("Wrong String array end token: "+jp.getCurrentToken());
		} else
			throw new IOException("Wrong String array start token: "+jp.getCurrentToken()+", the START_ARRAY or VALUE_STRING extected");
		
		return list.toArray(new String[list.size()]);
	}
}
