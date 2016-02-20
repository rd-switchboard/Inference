package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.rdswitchboard.utils.fuzzy_search.FuzzySearch;

public class MatcherFuzzy extends AbstractMatcher {
	private static final int MIN_DISTANCE = 1;
	private static final double TITLE_DISTANCE = 0.05;
		
	@Override
	public MatcherResult match() {
		MatcherResult result = new MatcherResult();
		result.setLink(getLink());
		result.startWatch();

		try {
			loadCache();
			
			char[] data = null;
			//long beginTime = System.currentTimeMillis();
			for (Map.Entry<String, MatcherNodes> entry : getNodes().entrySet()) {
				MatcherCache cache = getCahce(entry.getKey());
				if (!cache.isFound() && cache.getLevel() < 2) {
					cache.setLevel(2);
				
					if (null == data) 
						data = FuzzySearch.stringToCharArray(
								StringEscapeUtils.unescapeHtml(
										FileUtils.readFileToString(getDataFile()))
												.toLowerCase()				// convert to lower case
												.replaceAll("\u00A0", " "));
					
					if (FuzzySearch.find(
							FuzzySearch.stringToCharArray(entry.getKey()), 
							data, 
							getDistance(entry.getKey().length())) >= 0) 
						cache.setFound(true);
					
					updateCache();
				}
				
				if (cache.isFound()) 
					result.addNodes(entry.getValue().getNodes());
			}
			
			saveCache();
			
			
			/*
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
				return result;*/
		}
		catch(Exception e) {
			e.printStackTrace();
			
			result.setError(e.getMessage());			
		} finally {
			result.stopWatch();
		}
		
		return result;		
	}
	
	private int getDistance(int length) {
		int distance = (int) ((double) length * TITLE_DISTANCE + 0.5);
		return distance < MIN_DISTANCE ? MIN_DISTANCE : distance;			
	}
}