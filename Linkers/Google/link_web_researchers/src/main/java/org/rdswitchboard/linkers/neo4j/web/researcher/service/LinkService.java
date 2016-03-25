package org.rdswitchboard.linkers.neo4j.web.researcher.service;

import java.util.List;

import org.rdswitchboard.linkers.neo4j.web.researcher.model.Link;

public interface LinkService {
	void save(Link link);
	List<Link> getAllLinks();
}
