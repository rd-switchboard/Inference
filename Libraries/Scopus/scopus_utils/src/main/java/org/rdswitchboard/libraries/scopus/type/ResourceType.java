package org.rdswitchboard.libraries.scopus.type;

public enum ResourceType {
	resourceTypeScopus("scopus"),
	resourceTypeScidir("scidir");
	
	private String resourceName;
	
	private ResourceType(String resourceName) {
		this.resourceName = resourceName;
    }
     
	@Override
    public String toString(){
		return resourceName;
    } 
}
