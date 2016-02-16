package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

public class MatcherSimple extends AbstractMatcher {
	@Override
	public MatcherResult match() {
		try {
			// reset result set
			//result.clear();
			
			loadCache();
			
			MatcherResult result = new MatcherResult();
			result.setLink(getLink());			
			
			String cacheData = null;
			//long beginTime = System.currentTimeMillis();
			for (Map.Entry<String, MatcherNodes> entry : getNodes().entrySet()) {
				MatcherCache cache = getCahce(entry.getKey());
				if (null != cache) {
					if (cache.isFound()) {
						result.addNodes(entry.getValue().getNodes());
						continue;
					} else if (cache.getLevel() >= 1)
						continue;
				}
				
				if (null == cacheData)
					cacheData = StringEscapeUtils.unescapeHtml(FileUtils.readFileToString(getDataFile()))
						.toLowerCase()				// convert to lower case
						.replaceAll("\u00A0", " "); // replace all long spaces with simple space
					
				if (cacheData.contains(entry.getKey())) {
					result.addNodes(entry.getValue().getNodes());
					addCahce(entry.getKey(), 1, true); 
				} else
					addCahce(entry.getKey(), 1, false);
			}
			
			saveCache();
			
			if (result.hasNodes())
				return result;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;		
	}

}
