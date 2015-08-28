package org.rdswitchboard.libraries.scopus.facet;

public enum FacetType {
	facetTypeExactsrctitle("exactsrctitle"), 
	facetTypeAuId("au-id"),
	facetTypeAuthname("authname"),
	facetTypePubyear("pubyear"),
	facetTypeSubjarea("subjarea"),
	facetTypeLanguage("language"),
	facetTypeAfId("af-id"),
	facetTypeExactkeyword("exactkeyword"),
	facetTypeSrctype("srctype"),
	facetTypeCountry("country"),
	facetTypeAucite("aucite"),
	facetTypeRestype("restype");
	
	private String facetName;
	
	private FacetType(String facetName) {
		this.facetName = facetName;
    }
     
	@Override
    public String toString(){
		return facetName;
    } 
}
