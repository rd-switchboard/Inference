package org.rdswitchboard.libraries.scopus;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.rdswitchboard.libraries.scopus.deserilaize.AffiliationDeserializer;
import org.rdswitchboard.libraries.scopus.deserilaize.AffiliationsDeserializer;
import org.rdswitchboard.libraries.scopus.deserilaize.AuthorDeserializer;
import org.rdswitchboard.libraries.scopus.deserilaize.AuthorsDeserealizer;
import org.rdswitchboard.libraries.scopus.deserilaize.StringDeserializer;
import org.rdswitchboard.libraries.scopus.deserilaize.StringsDeserializer;
import org.rdswitchboard.libraries.scopus.facet.Facet;
import org.rdswitchboard.libraries.scopus.response.Affiliation;
import org.rdswitchboard.libraries.scopus.response.Author;
import org.rdswitchboard.libraries.scopus.response.SearchResults;
import org.rdswitchboard.libraries.scopus.type.AbstractType;
import org.rdswitchboard.libraries.scopus.type.AbstractViewType;
import org.rdswitchboard.libraries.scopus.type.AuthorFormatType;
import org.rdswitchboard.libraries.scopus.type.ContentType;
import org.rdswitchboard.libraries.scopus.type.ResourceType;
import org.rdswitchboard.libraries.scopus.type.SearchViewType;
import org.rdswitchboard.libraries.scopus.type.SortType;
import org.rdswitchboard.libraries.scopus.type.VersionType;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

public class Scopus {
	private static final String URL_SEARCH_API = "https://api.elsevier.com/content/search/";
	private static final String URL_ABSTRACT_API = "https://api.elsevier.com/content/abstract/";
	
	private static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
	private static final String HTTP_HEADER_API_KEY = "X-ELS-APIKey";
	//private static final String HTTP_HEADER_AUTHTOKEN = "X-ELS-Authtoken";
	private static final String HTTP_HEADER_INSTTOKEN = "X-ELS-Insttoken";
	private static final String HTTP_HEADER_REQ_ID = "X-ELS-ReqId";
	private static final String HTTP_HEADER_RESOURCE_VERSION = "X-ELS-ResourceVersion";
	
	private static final String SEPARATOR = ";";
	private static final String SEPARATOR2 = ",";
	
	private static final ObjectMapper mapper; 
	
	static {
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		//mapper.configure(Feature.UNWRAP_ROOT_VALUE, true);
		
		SimpleModule testModule = new SimpleModule("Scopus", new Version(1, 0, 0, null, "org.grants.scopus", "scopus"))
				.addDeserializer(Affiliation.class, new AffiliationDeserializer())
				.addDeserializer(Affiliation[].class, new AffiliationsDeserializer())
				.addDeserializer(Author.class, new AuthorDeserializer())
				.addDeserializer(Author[].class, new AuthorsDeserealizer())
				.addDeserializer(String.class, new StringDeserializer())
				.addDeserializer(String[].class, new StringsDeserializer());

		mapper.registerModule(testModule);
	}
	
	/**
	 * This represents a unique application developer key providing access to API resources. 
	 * This key can also be submitted as the query string parameter "apiKey"
	 */
	private String apiKey;
	
	/**
	 * This represents a institution token. If provided, this key (in combination with its 
	 * associated APIKey) is used to establish the credentials needed to access content in 
	 * this resource. This token can also be provided through the query string parameter "insttoken".
	 */
	private String instToken;
	
	/**
	 * This header field contains the OAuth bearer access token in which the format of the 
	 * field is @code("Bearer <token>") (where the token represents the end-user session key). 
	 * The presence of a bearer token implies the request will be executed against user-based 
	 * entitlements. The Authorization field overrides X-ELS-Authtoken.
	 */
	private String authorization;
	
	/**
	 * This represents a end-user session. If provided, this token is used to validate the 
	 * credentials needed to access content in this resource. This token can also be submitted 
	 * through the HTTP header "Authorization" or the query string parameter "access_token".
	 */
	
	//private String authtoken;
	
	/**
	 * This is a client-defined request identifier, which will be logged in all trace messages 
	 * of the service. This identifier can be used to track a specific transaction in the 
	 * service's message logs. It will also be returned as an HTTP header in the corresponding 
	 * response. Note that this should be a unique identifier for the client, and used to track 
	 * a single transaction.
	 */
	private String reqId;
	
	
	/**
	 * This represents the boolean search to be executed against the SCIDIR cluster (full 
	 * text articles). There is additional information regarding Search Tips and a list of search 
	 * field descriptions and examples.
	 * 
	 * 		ex. query=heart+attack%20AND%20text(liver) 
	 */
	//private String query;
	
	/**
	 * Represents the date range associated with the search, with the lowest granularity being year.
	 * 
	 * 		ex. date=2002-2007 
	 */
	private String date;
	
	/**
	 * Represents the subject area code associated with the content category desired. Note that 
	 * these subject code mapping vary based upon the environment in which the request is executed.
	 */
	private String subj;
			
	/**
	 * This parameter is used in conjunction with 'subscribed=true' to indicate the user wants to 
	 * search only those documents marked as Open Access (true). When set to 'true', this allows a 
	 * user to run a union search across all subscribed content, in addition to other non-subscribed 
	 * 'open access' documents.
	 * 
	 * 		ex. subscribed=true&oa=true 
	 */
	//;private Boolean openAccessOnly = null;
	
	/**
	 * This parameter is used to suppress the inclusion of top-level navigation links in the response 
	 * payload. 
	 * 
	 * default: false
	 */
	private Boolean suppressNavLinks;
	
	/**
	 * This parameter is used to indicate whether the search is executed against only those sources 
	 * to which a user is subscribed (true/false). The default behavior is against the end-user or 
	 * guest level source list.
	 * True encompasses only the data the requestor is subscribed to will be returned. False indicates 
	 * all sources available in the SD "all" list will be returned, which includes non-subscribed 
	 * content. 
	 * 
	 * default: false
	 */
	//private Boolean subscribed = null;
	
	/**
	 * This parameter controls the default behavior of returning a superseded author profiles. 
	 * Submitting this parameter as false will override the default behavior. This is only 
	 * applicable for searches targeting author identifiers. 
	 * 
	 * default: true
	 */
	private Boolean enableAlias;
	
	/**
	 * This parameter is used to determine whether the author and affiliation details are resolved 
	 * in the aggregate response. The default is true, which also forces additional back-end searches 
	 * -- which can be expensive if the search entries contain a large number of authors. Setting 
	 * resolveGroups to false means the affiliation details will appear in the final response. 
	 * 
	 * default: true
	 */
	private Boolean resolveGroups;
	
	/**
	 * Numeric value representing the results offset (i.e. starting position for the search results). 
	 * The maximum for this value is a system-level default (varies with search cluster) minus the 
	 * number of results requested. If not specified the offset will be set to zero (i.e. first search 
	 * result)
	 * 
	 * 		ex. start=5 
	 */
	private Integer start;
	
	/**
	 * Numeric value representing the maximum number of results to be returned for the search. If not 
	 * provided this will be set to a system default based on service level.
	 * In addition the number cannot exceed the maximum system default - if it does an error will be 
	 * returned.
	 * 
	 * 		ex. count=10 
	 */
	private Integer count;
	
	/**
	 * The type of resource (scopus is default)
	 */
	//private ResourceType resourceType = ResourceType.resourceTypeScopus;
	
	/**
	 * This represents the acceptable mime type format in which the response can be generated. 
	 * This can also be submitted as the query string parameter "httpAccept". This returns the 
	 * response in JSON, ATOM, or XML mark-up.
	 */
	private MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
	
	/**
	 * This alias represents the list of elements that will be returned in the search response. 
	 */
	private SearchViewType searchViewType = SearchViewType.searchViewTypeComplete;
		
	/**
	 * This alias represents the list of elements that will be returned in the abstract response. 
	 */
	private AbstractViewType abstractViewType = AbstractViewType.abstractViewTypeFull;
	
	/**
	 * This parameter is used to filter specific categories of content that should be searched/returned. 
	 */
	private ContentType contentType;
	
	/**
	 * This parameter is used to request the format of the author identifiers and names in the final 
	 * response. When set to source the author (and affiliation) identifiers are returned in a 
	 * pipe-delimited format, as are the author names. This can be used to optimize the search 
	 * performance when a large number of results with a large number of authors is returned.
	 * 
	 *  options: source
	 */
	private AuthorFormatType authorFormatType;
	
	/**
	 * This alias represents the name of specific fields that should be returned. The list of 
	 * fields include each of the primary fields returned in the response payload (see view).
	 * Multiple fields can be specified, delimited by commas. Note that specifying this parameter 
	 * overrides the view parameter.
	 * 
	 * 		ex. field=url,identifier,description 
	 */
	private Set<String> fields;
	
	/**
	 * Represents the version of the resource that should be received. Multiple attributes can 
	 * be submitted by separating with commas or semicolons. Options include:
	 * 		facetexpand - adds new fields under each facet returned (where applicable)
	 * 		allexpand - (same as facetexpand)
	 * 		new - returns the most recent and prototyped features 
	 */
	private Set<VersionType> resourceVersion;
	
	/**
	 * Represents the sort field name and order. A plus in front of the sort field name indicates
	 * ascending order, a minus indicates descending order. If sort order is not specified 
	 * (i.e. no + or -) then the order defaults to ascending (ASC).
	 * 
	 * Up to three fields can be specified, each delimited by a comma. The precedence is determined 
	 * by their order (i.e. first is primary, second is secondary, and third is tertiary).
	 * 
	 * 		+/-{field name}[,+/-{field name}
	 * 
	 * 		ex. sort=+coverDate,-publicationName 
	 */
	private Set<SortType> sortOptions;
	
	/**
	 * Represents the navigator that should be included in the search results. One or more navigators 
	 * can be specified on the request, delimited by a semicolon Different dimensions of the navigator 
	 * will be represented within parentheses. 
	 */
	private List<Facet> facets;
	
	/**
	 * Applicable only to REF view. Numeric value representing the results offset (i.e. starting position for the resolved references).

		ex. startref=5 
	 */
	private Integer startRef = null;
	
	/**
	 * Applicable only to REF view. Numeric value representing the maximum number of resolved references to be returned. If not provided this will be set to a system default based on service level.

	ex. refcount=10 
	 */
	private Integer refCount = null;
	
	/**
	 * Class constructor for a scopus object
	 * @param apiKey
	 * @param instToken
	 */
	public Scopus(String apiKey, String instToken) {
		this.apiKey = apiKey;
		this.instToken = instToken;
	}
	
	public SearchResults parseSearchResult(String json) {
		try {
			return mapper.readValue(json, SearchResults.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public SearchResults parseSearchResult(File fileJson) {
		try {
			return mapper.readValue(fileJson, SearchResults.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * Abstract record parsing is disabled for now, since we do not know, can we use it or not 
	 * and how we gonna use it if we will
	 */
	
	/*
	public AbstractResponse parseAbstractResponse(String json) {
		try {
			return mapper.readValue(json, AbstractResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public AbstractResponse parseAbstractResponse(File fileJson) {
		try {
			return mapper.readValue(fileJson, AbstractResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}*/
		
	public SearchResults search(ResourceType resourceType, String query) throws UnsupportedEncodingException {
		return parseSearchResult(searchString(resourceType, query));
	}
	
	public String searchString(ResourceType resourceType, String query) throws UnsupportedEncodingException {
		StringBuilder q = new StringBuilder();
		
		q.append(URL_SEARCH_API);
		q.append(resourceType.toString());
		q.append("?query=");
		q.append(URLEncoder.encode(query, StandardCharsets.UTF_8.name()));
		
		if (null != fields && !fields.isEmpty()) {
			StringBuilder sb = null;
			for (String field : fields) {
				if (null == sb)
					sb = new StringBuilder();
				else
					sb.append(SEPARATOR2);
				sb.append(field);
			}
			
			if (null != sb) {
				q.append("&field=");
				q.append(sb.toString());
			}				
		} else if (null != searchViewType) {
			q.append("&view=");
			q.append(searchViewType.toString());
		}
		
		if (null != suppressNavLinks) {
			q.append("&suppressNavLinks=");
			q.append(suppressNavLinks);
		}
		
		if (null != date) {
			q.append("&date=");
			q.append(date);
		}

		if (null != start) {
			q.append("&start=");
			q.append(start);
		}
		
		if (null != count) {
			q.append("&count=");
			q.append(count);
		}
		
		if (null != sortOptions) {
			StringBuilder sb = null;
			for (SortType sortType : sortOptions) {
				if (null == sb)
					sb = new StringBuilder();
				else
					sb.append(SEPARATOR2);
				
				sb.append(sortType.toString());
			}
			
			if (null != sb) {
				q.append("&sort=");
				q.append(sb.toString());
			}
		}
		
		if (null != contentType) {
			q.append("&content=");
			q.append(contentType.toString());
		}
		
		if (null != subj) {
			q.append("&subj=");
			q.append(subj);
		}
		
		if (null != enableAlias) {
			q.append("&alias=");
			q.append(enableAlias);
		}

		if (null != resolveGroups) {
			q.append("&resolveGroups=");
			q.append(resolveGroups);
		}
		
		if (null != authorFormatType) {
			q.append("&authorFormat=");
			q.append(authorFormatType.toString());
		}
			
		if (null != facets) {
			StringBuilder sb = null;
			for (Facet facet : facets) {
				if (null == sb)
					sb = new StringBuilder();
				else
					sb.append(SEPARATOR);
				
				sb.append(facet.toString());
			}
			
			if (null != sb) {
				q.append("&facets=");
				q.append(sb.toString());
			}
		}
		
		String url = q.toString();
		System.out.println("Downloading: "+ url);
		
		 Builder builder = Client
				.create()
				.resource( url )
				.type( MediaType.APPLICATION_JSON )
				.accept( mediaType );
				
		// set authorization (if any)
		if (null != authorization)
			builder = builder.header(HTTP_HEADER_AUTHORIZATION, authorization);
		if (null != apiKey)
			builder = builder.header(HTTP_HEADER_API_KEY, apiKey);
		if (null != instToken)
			builder = builder.header(HTTP_HEADER_INSTTOKEN, instToken);
		if (null != reqId)
			builder = builder.header(HTTP_HEADER_REQ_ID, reqId);
		if (null != resourceVersion && !resourceVersion.isEmpty()) {
			StringBuilder sb = null;
			for (VersionType versionType : resourceVersion) {
				if (null == sb)
					sb = new StringBuilder();
				else
					sb.append(SEPARATOR);
				sb.append(versionType.toString());
			}
			builder = builder.header(HTTP_HEADER_RESOURCE_VERSION, sb.toString());
		}
		
				
		ClientResponse response = builder.get( ClientResponse.class );
		
		if (response.getStatus() == 200) 
			return response.getEntity( String.class );
		
		System.out.println("Invalid Scopus response status: " + response.getStatus());
		System.out.println("Entity: " + response.getEntity( String.class ));
		
		return null;
	}
	
	public String abstractString(AbstractType abstractType, String id) {
		StringBuilder q = new StringBuilder();
		
		q.append(URL_ABSTRACT_API);
		q.append(abstractType.toString());
		q.append("/");
		q.append(id);
		q.append("?");
		
		if (null != fields && !fields.isEmpty()) {
			
			StringBuilder sb = null;
			for (String field : fields) {
				if (null == sb)
					sb = new StringBuilder();
				else
					sb.append(SEPARATOR2);
				sb.append(field);
			}
			
			if (null != sb) {
				q.append("&field=");
				q.append(sb.toString());
			}				
		} else if (null != abstractViewType) {
			q.append("&view=");
			q.append(abstractViewType.toString());
		}
		
		if (null != startRef)
		{
			q.append("&startref=");
			q.append(startRef);
		}
		
		if (null != refCount) {
			q.append("&refcount=");
			q.append(refCount);
		}
		
		String url = q.toString();
		System.out.println("Downloading: "+ url);
		
		Builder builder = Client
				.create()
				.resource( url )
				.type( MediaType.APPLICATION_JSON )
				.accept( mediaType );
				
		// set authorization (if any)
		if (null != authorization)
			builder = builder.header(HTTP_HEADER_AUTHORIZATION, authorization);
		if (null != apiKey)
			builder = builder.header(HTTP_HEADER_API_KEY, apiKey);
		if (null != instToken)
			builder = builder.header(HTTP_HEADER_INSTTOKEN, instToken);
		if (null != reqId)
			builder = builder.header(HTTP_HEADER_REQ_ID, reqId);
		if (null != resourceVersion && !resourceVersion.isEmpty()) {
			StringBuilder sb = null;
			for (VersionType versionType : resourceVersion) {
				if (null == sb)
					sb = new StringBuilder();
				else
					sb.append(SEPARATOR);
				sb.append(versionType.toString());
			}
			builder = builder.header(HTTP_HEADER_RESOURCE_VERSION, sb.toString());
		}
		
				
		ClientResponse response = builder.get( ClientResponse.class );
		
		if (response.getStatus() == 200) 
			return response.getEntity( String.class );
		else {
			System.out.println("Invalid Scopus response status: " + response.getStatus());
			return null;
		}
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getInstToken() {
		return instToken;
	}

	public void setInstToken(String instToken) {
		this.instToken = instToken;
	}

	public String getAuthorization() {
		return authorization;
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	public String getReqId() {
		return reqId;
	}

	public void setReqId(String reqId) {
		this.reqId = reqId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getSubj() {
		return subj;
	}

	public void setSubj(String subj) {
		this.subj = subj;
	}

	public Boolean getSuppressNavLinks() {
		return suppressNavLinks;
	}

	public void setSuppressNavLinks(Boolean suppressNavLinks) {
		this.suppressNavLinks = suppressNavLinks;
	}

	public Boolean getEnableAlias() {
		return enableAlias;
	}

	public void setEnableAlias(Boolean enableAlias) {
		this.enableAlias = enableAlias;
	}

	public Boolean getResolveGroups() {
		return resolveGroups;
	}

	public void setResolveGroups(Boolean resolveGroups) {
		this.resolveGroups = resolveGroups;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	/*
	public AcceptType getAcceptType() {
		return acceptType;
	}

	public void setAcceptType(AcceptType acceptType) {
		this.acceptType = acceptType;
	}

	public SearchViewType getViewType() {
		return viewType;
	}

	public void setViewType(SearchViewType viewType) {
		this.viewType = viewType;
	}*/

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public AuthorFormatType getAuthorFormatType() {
		return authorFormatType;
	}

	public void setAuthorFormatType(AuthorFormatType authorFormatType) {
		this.authorFormatType = authorFormatType;
	}

	public Set<String> getFields() {
		return fields;
	}

	public void setFields(Set<String> fields) {
		this.fields = fields;
	}

	public Set<VersionType> getResourceVersion() {
		return resourceVersion;
	}

	public void setResourceVersion(Set<VersionType> resourceVersion) {
		this.resourceVersion = resourceVersion;
	}

	public Set<SortType> getSortOptions() {
		return sortOptions;
	}

	public void setSortOptions(Set<SortType> sortOptions) {
		this.sortOptions = sortOptions;
	}

	public List<Facet> getFacets() {
		return facets;
	}

	public void setFacets(List<Facet> facets) {
		this.facets = facets;
	}
	
}
