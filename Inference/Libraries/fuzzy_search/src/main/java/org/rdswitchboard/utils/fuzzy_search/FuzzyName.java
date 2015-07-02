package org.rdswitchboard.utils.fuzzy_search;

/*
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class FuzzyName {
	private static final String SPLIT = "[\\W]";
	
	private final String name;
	private final String nameLowerCase;
	private final String[] nameParts;
	
	public FuzzyName(String name) {
		this.name = name;
		this.nameLowerCase = name.toLowerCase();
		this.nameParts = this.nameLowerCase.split(SPLIT);
	}
	
	public String getName() {
		return name;
	}

	public String getNameLowerCase() {
		return nameLowerCase;
	}

	public String[] getNameParts() {
		return nameParts;
	}
	
	public boolean equals(FuzzyName n) {
		return nameLowerCase.equals(n.nameLowerCase);
	}
	
	public boolean similar(FuzzyName n) {
		// convert name parst into dynamic arrays
		List<String> n1 = Arrays.asList(nameParts);
		List<String> n2 = Arrays.asList(n.nameParts);

		// if one of lists is empty or contains only one item, return false, because matching will be impossible
		if (n1.size() < 2 || n2.size() < 2)
			return false;
		
		// First check all full name parts and remove them if found
		for (Iterator<String> i1 = n1.iterator(); i1.hasNext(); ) {
			String s1 = i1.next();
			for (Iterator<String> i2 = n2.iterator(); i2.hasNext(); ) {
				String s2 = i2.next();
				if (s1.equals(s2)) {
					i1.remove();
					i2.remove();
					
					break;
				}
			}
		}

		// If one of the lists become empty at this stage, that means that all of it's parts has been matched
		if (n1.isEmpty() || n2.isEmpty())
			return true;
		
		// Now check other parts, if existing
		for (Iterator<String> i1 = n1.iterator(); i1.hasNext(); ) {
			String s1 = i1.next();
			for (Iterator<String> i2 = n2.iterator(); i2.hasNext(); ) {
				String s2 = i2.next();
				if (s1.equals(s2)) {
					i1.remove();
					i2.remove();
					
					break;
				}
			}
		}
		
		return false;
	}
}*/
