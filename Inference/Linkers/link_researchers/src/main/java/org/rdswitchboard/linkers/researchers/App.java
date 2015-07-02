package org.rdswitchboard.linkers.researchers;

public class App {
	private static final String NEO4J_FOLDER = "neo4j";	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String dbFolder = NEO4J_FOLDER;
		if (args.length > 0 && !args[0].isEmpty())
			dbFolder = args[0];
					
		try {
			Linker linker = new Linker(dbFolder);
			linker.process();			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
