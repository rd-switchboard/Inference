package org.rdswitchboard.utils.orcid;

public class App {

	public static void main(String[] args) {
	
		Orcid orcid = new Orcid();
		OrcidMessage message = orcid.queryId("http://orcid.org/0000-0002-4129-7491", RequestType.works);
		
		System.out.println(message);
	}
}
