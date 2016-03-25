package org.rdswitchboard.linkers.neo4j.web.researcher.dao.jdbc.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.rdswitchboard.linkers.neo4j.web.researcher.dao.LinkDAO;
import org.rdswitchboard.linkers.neo4j.web.researcher.model.Link;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("linkDAO")  
@Transactional //(propagation = Propagation.REQUIRED)
public class JdbcLinkDAOImpl implements LinkDAO {
	
	private static final String QUERY_SELECT_ALL = "select l from Link l";
	private static final String QUERY_SELECT_BY_LINK = "select l from Link l WHERE l.link = :link";
	
	@PersistenceContext
	private EntityManager entityManager;
	 
	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	/*@Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }*/
    
	public void insert(Link link) {
		this.entityManager.persist(link);
	}
	
	public Link update(Link link) {
		return this.entityManager.merge(link);
	}
	
	public Link selectByLink(String link) {
		 return  entityManager
				 .createQuery(QUERY_SELECT_BY_LINK, Link.class)
				 .setParameter("link", link)
				 .getSingleResult();
		
/*		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Link> criteria = builder.createQuery(Link.class);
		Root<Link> linkRoot = criteria.from( Link.class );
		criteria.select( linkRoot );
		criteria.where( builder.equal(linkRoot.get( ), y))*/
	}
	
	public List<Link> selectAll() {
		return entityManager
				.createQuery(QUERY_SELECT_ALL, Link.class)
				.getResultList();
	}
}
