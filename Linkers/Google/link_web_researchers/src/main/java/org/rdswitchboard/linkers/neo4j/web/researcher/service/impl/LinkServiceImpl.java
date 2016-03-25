package org.rdswitchboard.linkers.neo4j.web.researcher.service.impl;

import java.util.List;

import org.rdswitchboard.linkers.neo4j.web.researcher.dao.LinkDAO;
import org.rdswitchboard.linkers.neo4j.web.researcher.model.Link;
import org.rdswitchboard.linkers.neo4j.web.researcher.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("linkService")
public class LinkServiceImpl implements LinkService {
	private LinkDAO linkDao;
	
	public LinkDAO getLinkDao() {
		return this.linkDao;
	}
	
	@Autowired
	public void setLinkDao(LinkDAO linkDao) {
		this.linkDao = linkDao;
	}
		 
	public void save(Link link) {
		if (link.getLinkId() == 0) {
			Link _existing = this.linkDao.selectByLink(link.getLink());
			if (null != _existing) {
				
				link.setLinkId(_existing.getLinkId());
				if (!_existing.isDataEquals(link.getData()) || 
					!_existing.isMetadataEquals(link.getMetadata())) {
					this.linkDao.update(link);
				}
			} else
				this.linkDao.insert(link);
		}
	}
		 
	public List<Link> getAllLinks() {
		return getLinkDao().selectAll();
	}
}
