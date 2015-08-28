package org.rdswitchboard.importers.figshare;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.rdswitchboard.utils.neo4j.Neo4jUtils;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.rdswitchboard.importers.figshare.objects.Article;
import org.rdswitchboard.importers.figshare.objects.Author;
import org.rdswitchboard.importers.figshare.objects.Link;
import org.rdswitchboard.importers.figshare.objects.Researcher;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Importer {
	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_FIGSHARE_ID = "figshare_id";
	private static final String PROPERTY_JOB_TITLE = "job_title";
	private static final String PROPERTY_FACEBOOK = "facebook";
	private static final String PROPERTY_LINKEDIN = "linkedin";
	private static final String PROPERTY_TWITTER = "twitter";
	private static final String PROPERTY_ORCID = "orcid";

	private static final String PROPERTY_ARTICLE_ID = "article_id";
	private static final String PROPERTY_DOI = "doi";
	private static final String PROPERTY_AUTHORS = "authors";
	private static final String PROPERTY_LINKS = "links";
	private static final String PROPERTY_PUBLISHED_DATE = "published_date";
	private static final String PROPERTY_TITLE = "title";
	private static final String PROPERTY_TYPE = "type";
	private static final String PROPERTY_URL = "url";
	
	
	private File figshare;
	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	private RestIndex<Node> indexResearcher;
	private RestIndex<Node> indexPublication;
	private ObjectMapper mapper; 
	
	private static enum Labels implements Label {
		FigShare, Researcher, Publication
	}
	
	private static enum RelTypes implements RelationshipType {
		author
	}
	
	private TypeReference<Map<String, Researcher>> refResearchers = new TypeReference<Map<String, Researcher>>() {};
	
	public Importer(final String neo4jUrl, final String figshareUri) {
		System.out.println("Source file: " + figshareUri);
		System.out.println("Target Neo4j: " + neo4jUrl);
	
		// setup Object mapper
		mapper = new ObjectMapper(); 
		
		// connect to graph database
		graphDb = new RestAPIFacade(neo4jUrl);  
				
		// Create cypher engine
		engine = new RestCypherQueryEngine(graphDb);  
		
		Neo4jUtils.createConstraint(engine, Labels.FigShare, Labels.Researcher);
		Neo4jUtils.createConstraint(engine, Labels.FigShare, Labels.Publication);
		
		indexResearcher = Neo4jUtils.getIndex(graphDb, Labels.FigShare, Labels.Researcher);
		indexPublication = Neo4jUtils.getIndex(graphDb, Labels.FigShare, Labels.Researcher);
		
		// Set figshare file
		figshare = new File(figshareUri);	
	}

	public void process() throws JsonParseException, JsonMappingException, IOException {
		importAuthros();
	}

	private void importAuthros() throws JsonParseException, JsonMappingException, IOException {
		Map<String, Researcher> researchers = mapper.readValue(figshare, refResearchers);
		for (Researcher researcher : researchers.values()) {
			RestNode nodeResearcher = getOrCreateResearcher(researcher);
			
			for (Article article : researcher.getArticles()) {
				RestNode nodePublication = getOrCreatePublication(article);
				
				Neo4jUtils.createUniqueRelationship(graphDb, nodeResearcher, nodePublication, RelTypes.author, null);
			}
		}
	}
	
	private RestNode getOrCreateResearcher(Researcher researcher) {
		String key = "http://figshare.com/authors/" + researcher.getName().replace(' ', '_') + "/" + researcher.getId();
		System.out.println("Creating Researcher: " + key);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(PROPERTY_FIGSHARE_ID, researcher.getId());
		if (!StringUtils.isEmpty(researcher.getName()))
			map.put(PROPERTY_NAME, researcher.getName());
		if (!StringUtils.isEmpty(researcher.getJobTitle()))
			map.put(PROPERTY_JOB_TITLE, researcher.getJobTitle());
		if (!StringUtils.isEmpty(researcher.getFacebook()))
			map.put(PROPERTY_FACEBOOK, researcher.getFacebook());
		if (!StringUtils.isEmpty(researcher.getLinkedin()))
			map.put(PROPERTY_LINKEDIN, researcher.getLinkedin());
		if (!StringUtils.isEmpty(researcher.getTwitter()))
			map.put(PROPERTY_TWITTER, researcher.getTwitter());
		if (!StringUtils.isEmpty(researcher.getOrcid()))
			map.put(PROPERTY_ORCID, researcher.getOrcid());
				
		return Neo4jUtils.createUniqueNode(graphDb, indexResearcher, 
				Labels.FigShare, Labels.Researcher, key, map);
	}

	private RestNode getOrCreatePublication(Article article) {
		String key = article.getDoi();
		System.out.println("Creating Publication: " + key);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(PROPERTY_DOI, article.getDoi());
		map.put(PROPERTY_ARTICLE_ID, article.getArticleId());
		
		Set<String> authors = new HashSet<String>();
		for (Author author : article.getAuthors()) 
			authors.add(author.getName());
		if (!authors.isEmpty())
			map.put(PROPERTY_AUTHORS, authors);
		
		Set<String> links = new HashSet<String>();
		for (Link link : article.getLinks())
			links.add(link.getLink());
		if (!links.isEmpty())
			map.put(PROPERTY_LINKS, links);

		if (!StringUtils.isEmpty(article.getPublishedDate()))
			map.put(PROPERTY_PUBLISHED_DATE, article.getPublishedDate());
		if (!StringUtils.isEmpty(article.getTitle()))
			map.put(PROPERTY_TITLE, article.getTitle());
		if (!StringUtils.isEmpty(article.getType()))
			map.put(PROPERTY_TYPE, article.getType());
		if (!StringUtils.isEmpty(article.getUrl()))
			map.put(PROPERTY_URL, article.getUrl());

		return Neo4jUtils.createUniqueNode(graphDb, indexPublication, 
				Labels.FigShare, Labels.Publication, key, map);
	}
	
}
