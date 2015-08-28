package org.rdswitchboard.libraries.scopus.deserilaize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonDeserializer;

public abstract class CustomDeserializer<T> extends JsonDeserializer<T>{
	protected List<Object> parseArray(JsonParser jp) throws IOException {
		// check, that we actually have an object
		//if (jp.getCurrentToken() != JsonToken.START_ARRAY)
		//	throw new IOException("Wrong array start token: "+jp.getCurrentToken());
		
		List<Object> list = null;
		for(JsonToken token = jp.nextToken(); token != null && token != JsonToken.END_ARRAY; token = jp.nextToken()) {
			if (token != JsonToken.START_OBJECT)
				throw new IOException("Wrong object start token: "+jp.getCurrentToken());
			
			Object value = parseObject(jp);
			
			if (null == list)
				list = new ArrayList<Object>();
			list.add(value);
		}
		
		return list;
	}
	
	protected Map<String, Object> parseObject(JsonParser jp) throws IOException {
		// check, that we actually have an object
		//if (jp.getCurrentToken() != JsonToken.START_OBJECT)
		//	throw new IOException("Wrong object start token: "+jp.getCurrentToken());
		
		// create empty map
		Map<String, Object> map = null;
		// run the main cicle until we will actually get end of the object
		for(JsonToken token = jp.nextToken(); token != null && token != JsonToken.END_OBJECT; token = jp.nextToken()) {
			// check that next object is a field name
			if (token != JsonToken.FIELD_NAME)
				throw new IOException("Wrong object field token: "+jp.getCurrentToken());
			
			// retrive object field key
			String key = jp.getCurrentName();
			Object value = null;
			// go to the next token
			token = jp.nextToken();
			
			if (token == JsonToken.START_ARRAY)
				value = parseArray(jp);
			else if (token == JsonToken.START_OBJECT)
				value = parseObject(jp);
			else if (token == JsonToken.VALUE_STRING)
				value = jp.getValueAsString();
			else if (token == JsonToken.VALUE_NUMBER_INT)
				value = jp.getValueAsDouble();
			else if (token == JsonToken.VALUE_NUMBER_INT)
				value = jp.getIntValue();
			else if (token == JsonToken.VALUE_TRUE)
				value = true;
			else if (token == JsonToken.VALUE_FALSE)
				value = false;
			else
				value = null;
			
			if (null == map)
				map = new HashMap<String, Object>();
			map.put(key,  value);
		}
		
		return map;
	}
	
	protected Object parse(JsonParser jp) throws IOException {
		Object object = null;
		JsonToken token = jp.getCurrentToken();
		if (token == JsonToken.START_ARRAY)
			object = parseArray(jp);
		else if (token == JsonToken.START_OBJECT)
			object = parseObject(jp);
		else
			throw new IOException("Wrong start token: "+jp.getCurrentToken()+". The START_ARRAY or START_OBJECT expected");	
	
		// close the current object or array
	//	jp.nextToken();
		
		return object;
	}
	
	protected void putIfAbsent(Map<String, Object> src, Map<String, Object> dst, String keySrc, String keyDst) {
		if (!dst.containsKey(keyDst) && src.get(keySrc) != null)
			dst.put(keyDst, src.get(keySrc));
	}
	
	protected void putSetString(Map<String, Object> src, Map<String, Object> dst, String keySrc, String keyDst) throws IOException {
		Object s = src.get(keySrc);
		if (s != null) {
			if (s instanceof String) {
				if (!((String) s).isEmpty()) {
					@SuppressWarnings("unchecked")
					Set<String> set = (Set<String>) dst.get(keyDst);
					if (null == set) {
						set = new HashSet<String>();
						dst.put(keyDst, set);
					}
		
					set.add((String) s);
				}
			}
			else
				throw new IOException("The data is not a string");
		}
	}
}
