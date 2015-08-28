package org.rdswitchboard.libraries.scopus.deserilaize;

import java.io.IOException;
import java.util.List;

import org.rdswitchboard.libraries.scopus.response.Affiliation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.Lists;

public class AffiliationsDeserializer extends JsonDeserializer<Affiliation[]> {

	@Override
	public Affiliation[] deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		List<Affiliation> list = null;
		
		if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
			list = Lists.newArrayList(jp.readValueAs(Affiliation.class));			
		} else if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
			jp.nextToken();
				
			list = Lists.newArrayList(jp.readValuesAs(Affiliation.class));
			
			if (jp.getCurrentToken() != JsonToken.END_ARRAY)
				throw new IOException("Wrong affiliation array end token: "+jp.getCurrentToken());
		} else 
			throw new IOException("Wrong affiliation start token: "+jp.getCurrentToken()+", the START_OBJECT or START_ARRAY expected");
		
		return list.toArray(new Affiliation[list.size()]);
	}

}
