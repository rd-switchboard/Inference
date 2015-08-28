package org.rdswitchboard.libraries.crossref;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Class to parse CrossRef Date object
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class CrossRefDateDeserializer  extends JsonDeserializer<Date> {
	private static final String NODE_TIMESTAMP = "timestamp";
	private static final String NODE_DATE_PARTS = "date-parts";

	@Override
	public Date deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = p.getCodec();
		JsonNode node = oc.readTree(p);
		
		final JsonNode nodeStamp = node.get(NODE_TIMESTAMP);
		if (null != nodeStamp) 
			return new Date(nodeStamp.asLong(0));

		final JsonNode nodeParts = node.get(NODE_DATE_PARTS);
		if (null != nodeParts && nodeParts.isArray()) {
			for (final JsonNode nodePart : nodeParts) {
				if (nodePart.isArray()) {
					switch (nodePart.size()) {
					case 1:
						return new GregorianCalendar(nodePart.get(0).asInt(), 0, 1).getTime();
					case 2:
						return new GregorianCalendar(nodePart.get(0).asInt(), nodePart.get(1).asInt()-1, 1).getTime();
					case 3:
						return new GregorianCalendar(nodePart.get(0).asInt(), nodePart.get(1).asInt()-1, nodePart.get(2).asInt()).getTime();
					}
				}			
			}
		}

		return null;
	}
}
