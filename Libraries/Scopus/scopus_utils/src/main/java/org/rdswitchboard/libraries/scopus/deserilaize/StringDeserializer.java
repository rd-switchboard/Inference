package org.rdswitchboard.libraries.scopus.deserilaize;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class StringDeserializer extends JsonDeserializer<String> {
	private static final String JSON_STRING = "$";
	
	@Override
	public String deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		if (jp.getCurrentToken() == JsonToken.VALUE_STRING)
			return jp.getValueAsString();
		else if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
			String result = null;
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				if (null == result
						&& jp.getCurrentToken() == JsonToken.FIELD_NAME 
						&& jp.getCurrentName().equals(JSON_STRING)) { 
					if (jp.nextToken() != JsonToken.VALUE_STRING)
						throw new IOException("Wrong string token: "+jp.getCurrentToken());
					
					result = jp.getValueAsString();					
				}
			}
			
			return result;
		} else 
			throw new IOException("Wrong string start token: "+jp.getCurrentToken()+"the START_OBJECT or VALUE_STRING expected");
	}
}
