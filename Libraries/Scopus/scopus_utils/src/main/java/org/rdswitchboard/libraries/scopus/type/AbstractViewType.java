package org.rdswitchboard.libraries.scopus.type;

public enum AbstractViewType {
	abstractViewTypeMeta("META"), 
	abstractViewTypeMetaAbs("META_ABS"),
	abstractViewTypeFull("FULL"),
	abstractViewTypeRef("REF"),
	abstractViewTypeEntitled("ENTITLED");
	
	private String abstractViewName;
	
	private AbstractViewType(String abstractViewName) {
		this.abstractViewName = abstractViewName;
    }
     
	@Override
    public String toString(){
		return abstractViewName;
    } 

}
