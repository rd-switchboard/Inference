package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.rdswitchboard.utils.fuzzy_search.FuzzySearch;
import org.rdswitchboard.utils.google.cache2.GoogleUtils;
import org.rdswitchboard.utils.google.cache2.Link;

public class MatcherFuzzy  implements Matcher {
	private final String gooleCache;
	private final Link link;
	private final Map<String, Set<Long>> nodes;
	
	private static final int MIN_DISTANCE = 1;
	private static final double TITLE_DISTANCE = 0.05;
	
	public MatcherFuzzy(String gooleCache, Link link, Map<String, Set<Long>> nodes) {
		this.gooleCache = gooleCache;
		this.link = link;
		this.nodes = nodes;
	}
	
	@Override
	public MatcherResult match() {
		try {
			// reset result set
			//result.clear();
			
			MatcherResult result = new MatcherResult();
			result.setLink(link);			
			
			String cacheData = FileUtils.readFileToString(
					new File(GoogleUtils.getDataFolder(gooleCache), link.getData()));
			cacheData = StringEscapeUtils.unescapeHtml(cacheData)
					.toLowerCase()				// convert to lower case
					.replaceAll("\u00A0", " "); // replace all long spaces with simple space
			
			final char[] data = FuzzySearch.stringToCharArray(cacheData);
			
			//long beginTime = System.currentTimeMillis();
			for (Map.Entry<String, Set<Long>> entry : nodes.entrySet()) {
				if (FuzzySearch.find(
						FuzzySearch.stringToCharArray(entry.getKey()), 
						data, 
						getDistance(entry.getKey().length())) >= 0) {
					result.addNodes(entry.getValue());
				}
			}
			
			if (result.hasNodes())
				return result;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;		
	}
	
	private int getDistance(int length) {
		int distance = (int) ((double) length * TITLE_DISTANCE + 0.5);
		return distance < MIN_DISTANCE ? MIN_DISTANCE : distance;			
	}
}