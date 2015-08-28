package org.rdswitchboard.libraries.scopus.facet;


public class Facet {
	private static final String FIELD_COUNT = "count";
	private static final String FIELD_SORT = "sort";
	private static final String FIELD_PREFIX = "prefix";
	
	/**
	 * Facet Type (can not be null)
	 */
	private FacetType facetType;
	
	/**
	 * the number of "buckets" to include (i.e. how many navigator entries)
	 */
	private FacetSortType facetSortType;

	/**
	 * how the navigators should be sorted. 
	 * Options include 
	 * 		na (Modifier name, ascending), 
	 * 		fd (Modifier frequency, descending), and 
	 * 		fdna (Modifier frequency descending, secondary sort through unity by name, ascending).
	 */
	private Integer count;

	/**
	 * filters the facet values to only those matching the prefix specified (not applicable for 
	 * numeric values).
	 */
	private String prefix;
	
	public Facet(FacetType facetType) {
		this.facetType = facetType;
	}
	
	public Facet(FacetType facetType, Integer count, FacetSortType facetSortType, String prefix) {
		this.facetType = facetType;
		this.facetSortType = facetSortType;
		this.count = count;
		this.prefix = prefix;
	}
	
	public FacetType getFacetType() {
		return facetType;
	}
	
	public void setFacetType(FacetType facetType) {
		this.facetType = facetType;
	}
	
	public FacetSortType getFacetSortType() {
		return facetSortType;
	}
	
	public void setFacetSortType(FacetSortType facetSortType) {
		this.facetSortType = facetSortType;
	}
	
	public Integer getCount() {
		return count;
	}
	
	public void setCount(Integer count) {
		this.count = count;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public String toString() {
		if (null == facetType)
			return null; // unable to build a facet
		
		StringBuilder sb = new StringBuilder();
		if (null != count) {
			sb.append(FIELD_COUNT);
			sb.append("=");
			sb.append(count);
		}
		
		if (null != facetSortType) {
			if (sb.length() > 0)
				sb.append(",");
			sb.append(FIELD_SORT);
			sb.append("=");
			sb.append(facetSortType.toString());
		}
		
		if (null != prefix) {
			if (sb.length() > 0)
				sb.append(",");
			sb.append(FIELD_PREFIX);
			sb.append("=");
			sb.append(prefix);
		}
		
		String facet = facetType.toString();
		if (sb.length() > 0)
			facet +="(" + sb.toString() + ")";
		
		return facet;
	}
	
}

