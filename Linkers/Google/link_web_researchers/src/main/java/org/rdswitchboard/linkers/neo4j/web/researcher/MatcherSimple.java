package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.rdswitchboard.utils.google.cache2.Link;
import org.rdswitchboard.utils.google.cache2.GoogleUtils;

public class MatcherSimple implements Matcher {
	private final String gooleCache;
	private final Link link;
	private final Map<String, Set<Long>> nodes;
	
	public MatcherSimple(String gooleCache, Link link, Map<String, Set<Long>> nodes) {
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
			
			//long beginTime = System.currentTimeMillis();
			for (Map.Entry<String, Set<Long>> entry : nodes.entrySet()) {
				if (cacheData.contains(entry.getKey())) {
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

}
