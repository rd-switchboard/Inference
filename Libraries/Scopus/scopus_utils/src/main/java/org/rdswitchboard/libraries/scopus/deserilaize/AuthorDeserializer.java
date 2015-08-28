package org.rdswitchboard.libraries.scopus.deserilaize;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rdswitchboard.libraries.scopus.response.Author;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;

public class AuthorDeserializer extends CustomDeserializer<Author> {

	private static final String JSON_AUTHOR_ID = "authid";
	private static final String JSON_AUTHOR_URL = "author-url";
	private static final String JSON_FULL_NAME = "authname";
	private static final String JSON_GIVEN_NAME = "given-name";
	private static final String JSON_FAMILY_NAME = "surname";
	private static final String JSON_INNITIALS = "initials";
	private static final String JSON_STRING = "$";
	private static final String JSON_AFFILIATION_ID = "afid";
	
	private static final String PROPERTY_AUTHOR_ID = "authorId";
	private static final String PROPERTY_AFFILIATION_ID = "affiliationId";
	private static final String PROPERTY_AUTHOR_URL = "authorUrl";
	private static final String PROPERTY_FULL_NAME = "fullName";
	private static final String PROPERTY_GIVEN_NAME = "familyName";
	private static final String PROPERTY_FAMILY_NAME = "familyName";
	private static final String PROPERTY_INITIALS = "initials";
	
	@SuppressWarnings("unchecked")
	@Override
	public Author deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
				
		Map<String, Object> author = new HashMap<String, Object>();
		Map<String, Object> map = (Map<String, Object>) parse(jp);
		
		putIfAbsent(map, author, JSON_AUTHOR_ID, PROPERTY_AUTHOR_ID);
		putIfAbsent(map, author, JSON_AUTHOR_URL, PROPERTY_AUTHOR_URL);
		putIfAbsent(map, author, JSON_FULL_NAME, PROPERTY_FULL_NAME);
		putIfAbsent(map, author, JSON_INNITIALS, PROPERTY_INITIALS);
		putIfAbsent(map, author, JSON_FAMILY_NAME, PROPERTY_FAMILY_NAME);
		putIfAbsent(map, author, JSON_GIVEN_NAME, PROPERTY_GIVEN_NAME);
		
		List<Map<String, Object>> affiliationIds = (List<Map<String, Object>>) map.get(JSON_AFFILIATION_ID);
		if (null != affiliationIds) 
			for (Map<String, Object> affiliationId : affiliationIds)
				putSetString(affiliationId, author, JSON_STRING, PROPERTY_AFFILIATION_ID);
		
		return new Author(
				(String) author.get(PROPERTY_AUTHOR_ID),
				(String) author.get(PROPERTY_AUTHOR_URL),
				(String) author.get(PROPERTY_FULL_NAME),
				(String) author.get(PROPERTY_GIVEN_NAME),
				(String) author.get(PROPERTY_FAMILY_NAME),
				(String) author.get(PROPERTY_INITIALS),
				(Set<String>) author.get(PROPERTY_AFFILIATION_ID));
	}
}

/*private void skipObject(JsonParser jp) throws JsonParseException, IOException {
		System.out.println("====== SKIP OBJECT =======");
		for(JsonToken token = jp.nextToken(); token != null && token != JsonToken.END_OBJECT; token = jp.nextToken()) 
			if (token == JsonToken.START_OBJECT)
				skipObject(jp);
	}
	
	private void parseObject(JsonParser jp, Map<String, String> map) throws IOException {
		System.out.println(jp.getCurrentToken());
		
		System.out.println("====== PARSE OBJECT =======");
		
		if (jp.getCurrentToken() != JsonToken.START_OBJECT)
			throw new IOException("[AuthorDeserializer] Wrong start token: "+jp.getCurrentToken());
		
		for(JsonToken token = jp.nextToken(); token != null && token != JsonToken.END_OBJECT; token = jp.nextToken()) {
			System.out.println(jp.getCurrentToken());
			
			if (token == JsonToken.FIELD_NAME) {
				String fieldName = jp.getCurrentName();
				String propertyName = null;
				if (fieldName.equals(JSON_AUID))
					propertyName = PROPERTY_AUTHOR_ID;
				else if (fieldName.equals(JSON_AUTHOR_URL))
					propertyName = PROPERTY_AUTHOR_URL;
				else if (fieldName.equals(JSON_CE_INDEXED_NAME))
					propertyName = PROPERTY_FULL_NAME;
				else if (fieldName.equals(JSON_CE_INNITIALS))
					propertyName = PROPERTY_INITIALS;
				else if (fieldName.equals(JSON_CE_SURNAME))
					propertyName = PROPERTY_FAMILY_NAME;
				else if (fieldName.equals(JSON_CE_GIVEN_NAME))
					propertyName = PROPERTY_GIVEN_NAME;
				
				// move to the next token
				token = jp.nextToken();
				System.out.println(jp.getCurrentToken());
				
				if (token == JsonToken.START_OBJECT) {
					if (fieldName.equals(JSON_PREFERRED_NAME)) 
						parseObject(jp, map);  // parse embedded object
					 else 
						skipObject(jp);	// ignore embedded object
				} else {
					// extract token value
					String value = jp.getValueAsString();
					
					if (propertyName != null && value != null && !map.containsKey(propertyName)) 
						map.put(propertyName, value);					
				}
				
			} else
				throw new IOException("[AuthorDeserializer] Wrong token: "+jp.getCurrentToken()+", the FIELD_NAME expected");
		}
	}
	
	public void putIfAbsent(Map<String, Object> src, Map<String, Object> dst, String key) {
		if (!src.containsKey(key) && )
	}
	
	@Override
	public Author deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		
		ObjectCodec codec = jp.getCodec();
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) codec.readValues(jp, Map.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>) map.get(JSON_PREFERRED_NAME);
		
		if (null != result) {
			
		}
		
		if (map.containsKey(JSON_AUID))
			result.put(PROPERTY_AUTHOR_ID, map.get(JSON_AUID));
		
		
		Map<String, String> map = new HashMap<String, String>();
		parseObject(jp, map);

		// pass end Object
		System.out.println("====== DONE =======");

		return new Author(
				map.get(PROPERTY_AUTHOR_ID),
				map.get(PROPERTY_AUTHOR_URL),
				map.get(PROPERTY_FULL_NAME),
				map.get(PROPERTY_GIVEN_NAME),
				map.get(PROPERTY_FAMILY_NAME),
				map.get(PROPERTY_INITIALS));
	}*/
	
	

/*	private static final String AUTHOR = "author";
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final CollectionType collectionType =
	        TypeFactory
            .defaultInstance()
            .constructCollectionType(List.class, Author.class);
	
	@Override
	public Author deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		
		ObjectNode objectNode = mapper.readTree(jsonParser);
		JsonNode nodeAuthors = objectNode.get(AUTHOR);
	
		if (null == nodeAuthors 					// if no author node could be found
				|| !nodeAuthors.isArray() 			// or author node is not an array
				|| !nodeAuthors.elements().hasNext()) 	// or author node doesn't contain any authors
			return null;
		
		return mapper.reader(collectionType).readValue(nodeAuthors);
	}*/