package org.rdswitchboard.libraries.scopus.deserilaize;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rdswitchboard.libraries.scopus.response.Affiliation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;

public class AffiliationDeserializer extends CustomDeserializer<Affiliation>  {
	private static final String JSON_CITY = "affiliation-city";
	private static final String JSON_COUNTRY = "affiliation-country";
	private static final String JSON_URL = "affiliation-url";
	private static final String JSON_NAME = "affilname";
	private static final String JSON_ID = "afid";
	private static final String JSON_VARIANT = "name-variant";
	private static final String JSON_STRING = "$";
	
	private static final String PROPERTY_CITY = "city";
	private static final String PROPERTY_COUNTRY = "country";
	private static final String PROPERTY_URL = "url";
	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_ID = "id";
	private static final String PROPERTY_ALTERNATIVE = "alternative";
		
	@SuppressWarnings("unchecked")
	@Override
	public Affiliation deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		
		Map<String, Object> affiliation = new HashMap<String, Object>();
		Map<String, Object> map = (Map<String, Object>) parse(jp);
		
		putIfAbsent(map, affiliation, JSON_ID, PROPERTY_ID);
		putIfAbsent(map, affiliation, JSON_NAME, PROPERTY_NAME);
		putIfAbsent(map, affiliation, JSON_CITY, PROPERTY_CITY);
		putIfAbsent(map, affiliation, JSON_COUNTRY, PROPERTY_COUNTRY);
		putIfAbsent(map, affiliation, JSON_URL, PROPERTY_URL);
		
		Object variants = map.get(JSON_VARIANT);
		if (null != variants) {
			if (variants instanceof Map) {
				putSetString((Map<String, Object>) variants, affiliation, JSON_STRING, PROPERTY_ALTERNATIVE);
			} else if (variants instanceof List) {
				for (Map<String, Object> variant : (List<Map<String, Object>>) variants) {
					putSetString(variant, affiliation, JSON_STRING, PROPERTY_ALTERNATIVE);
				}
			} else 
				throw new IOException("Unsupported variant type: " + variants.getClass().getName());
		}
	
		return new Affiliation(
				(String) affiliation.get(PROPERTY_ID),
				(String) affiliation.get(PROPERTY_NAME),
				(String) affiliation.get(PROPERTY_CITY),
				(String) affiliation.get(PROPERTY_COUNTRY),
				(String) affiliation.get(PROPERTY_URL),
				(Set<String>) affiliation.get(PROPERTY_ALTERNATIVE));
	}
}
