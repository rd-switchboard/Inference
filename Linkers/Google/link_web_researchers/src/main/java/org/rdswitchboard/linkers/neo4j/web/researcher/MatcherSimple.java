package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

public class MatcherSimple extends AbstractMatcher {
	@Override
	public MatcherResult match() {
		MatcherResult result = new MatcherResult();
		result.setLink(getLink());
		result.startWatch();

		try {
			loadCache();
			
			String cacheData = null;
			//long beginTime = System.currentTimeMillis();
			for (Map.Entry<String, MatcherNodes> entry : getNodes().entrySet()) {
				MatcherCache cache = getCahce(entry.getKey());
				if (!cache.isFound() && cache.getLevel() < 1) {
					cache.setLevel(1);
				
					if (null == cacheData)
						cacheData = StringEscapeUtils.unescapeHtml(FileUtils.readFileToString(getDataFile()))
							.toLowerCase()				// convert to lower case
							.replaceAll("\u00A0", " "); // replace all long spaces with simple space
					
					if (cacheData.contains(entry.getKey())) 
						cache.setFound(true);
					
					updateCache();
				}
				
				if (cache.isFound()) 
					result.addNodes(entry.getValue().getNodes());
			}
			
			saveCache();
		} catch(Exception e) {
			e.printStackTrace();
			
			result.setError(e.getMessage());			
		} finally {
			result.stopWatch();
		}
		
		return result;		
	}

}
