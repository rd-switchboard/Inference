package org.rdswitchboard.linkers.neo4j.web.researcher.dao;

import java.util.List;

import org.rdswitchboard.linkers.neo4j.web.researcher.model.Link;

public interface LinkDAO {
	void insert(Link link);
	Link update(Link link);
	Link selectByLink(String link);
	List<Link> selectAll();
}
