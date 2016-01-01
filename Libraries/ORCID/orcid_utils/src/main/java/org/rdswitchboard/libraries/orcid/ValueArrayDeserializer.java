package org.rdswitchboard.libraries.orcid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

public class ValueArrayDeserializer extends JsonDeserializer<List<String>> {

	private static final String VALUE = "value";
	
	
	@Override
	public List<String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);
		
		if (node.isNull())
			return null;
	
		if (node.isArray()) {
			List<String> list = new ArrayList<String>();
			for (final JsonNode name : node) {
		        if (name.isObject()) {
		        	final JsonNode value = name.get(VALUE);
		        	
		        	if (null != value && value.isValueNode()) 
						return null;
					
		        	list.add(value.asText());
		        }
		    }
			
			return list;
		}
		
		throw new JsonMappingException("Unable to parse value, the node type is " + node.getNodeType());		
	}
}

