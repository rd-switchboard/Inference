package org.rdswitchboard.libraries.graph.interfaces;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.rdswitchboard.libraries.graph.Graph;

public interface GraphCrosswalk {
	void setSource(String source);
	String getSource();
	Graph process(InputStream xml) throws Exception;
}
