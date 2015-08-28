package org.rdswitchboard.libraries.scopus.type;

public enum AbstractType {
	abstractTypeScopusId("scopus_id"),
	abstractTypeEid("eid"),
	abstractTypeDoi("doi"),
	abstractTypePii("pii"),
	abstractTypePubmedId("pubmed_id"),
	abstractTypePui("pui"),
	abstractTypeGroupId("group_id");

	private String abstractName;
	
	private AbstractType(String abstractName) {
		this.abstractName = abstractName;
    }
     
	@Override
    public String toString(){
		return abstractName;
    } 
}
