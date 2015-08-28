package org.rdswitchboard.libraries.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphProperties {
	protected Map<String, Set<Object>> properties = new HashMap<String, Set<Object>>();
	
	public GraphProperties() {
		
	}
	
	public GraphProperties(Map<String, Object> properties) {
		// Add Properties will work faster
		addProperties(properties);
	}

	public boolean hasProperty(String key) {
		return properties.containsKey(key);
	}

	
	public Object getProperty(String key) {
		return getProperty(properties.get(key));
	}
	
	public void setProperty(String key, Object value) {
		if (null != value) {
			Set<Object> set = properties.get(key);
			if (null == set) 
				properties.put(key, set = new HashSet<Object>());
			else
				set.clear();
			
			setProperty(set, value);
		} else
			properties.remove(key);			
	}
	
	public void setPropertyOnce(String key, Object value) {
		if (null != value) {
			Set<Object> set = properties.get(key);
			if (null == set) {
				properties.put(key,  set = new HashSet<Object>());
				setProperty(set, value);
			} 
		} 			
	}
	
	public void addProperty(String key, Object value) {
		if (null != value) {
			Set<Object> set = properties.get(key);
			if (null == set) {
				set = new HashSet<Object>();
				properties.put(key, set);
			} 
			
			setProperty(set, value);
		} 			
	}
	
	public Map<String, Object> getProperties() {
		Map<String, Object> result = new HashMap<String, Object>();
		for (Map.Entry<String, Set<Object>> entry : properties.entrySet()) {
			Object value = getProperty(entry.getValue());
			if (null != value)
				result.put(entry.getKey(), value);
		}
		
		return result;
	}
	
	public void setProperties(Map<String, Object> map) {
		if (null != map) 
			for (Map.Entry<String, Object> entry : map.entrySet()) 
				setProperty(entry.getKey(), entry.getValue());
	}

	public void addProperties(Map<String, Object> map) {
		if (null != map) 
			for (Map.Entry<String, Object> entry : map.entrySet()) 
				addProperty(entry.getKey(), entry.getValue());
	}

	protected static Object getProperty(Set<Object> set) {
		if (null == set)
			return null;
		int size = set.size();
		if (0 == size)
			return null;
		
		Object element = set.iterator().next();
		if (1 == size)
			return element;
		if (element instanceof Boolean)
			return set.toArray(new Boolean[size]);
		if (element instanceof Byte)
			return set.toArray(new Byte[size]);
		if (element instanceof Short)
			return set.toArray(new Short[size]);
		if (element instanceof Integer)
			return set.toArray(new Integer[size]);
		if (element instanceof Long)
			return set.toArray(new Long[size]);
		if (element instanceof Float)
			return set.toArray(new Float[size]);
		if (element instanceof Double)
			return set.toArray(new Double[size]);
		if (element instanceof String)
			return set.toArray(new String[size]);
		
		throw new ClassCastException("Unable to convert Property Array, they property type: " + element.getClass() + " is not supported");
	}

	protected static void setProperty(Set<Object> set, Object value) {
		if (value instanceof String[]) 
			set.addAll(Arrays.asList((String[]) value));
		else if (value instanceof Boolean[])
			set.addAll(Arrays.asList((Boolean[]) value));
		else if (value instanceof Byte[])
			set.addAll(Arrays.asList((Byte[]) value));
		else if (value instanceof Short[])
			set.addAll(Arrays.asList((Short[]) value));
		else if (value instanceof Integer[])
			set.addAll(Arrays.asList((Integer[]) value));
		else if (value instanceof Long[])
			set.addAll(Arrays.asList((Long[]) value));
		else if (value instanceof Float[])
			set.addAll(Arrays.asList((Float[]) value));
		else if (value instanceof Double[])
			set.addAll(Arrays.asList((Double[]) value));
		/*else if (value instanceof Object[])
			set.addAll(Arrays.asList((Object[]) value));*/
		else if (value instanceof Collection<?>)
			set.addAll((Collection<?>) value);
		else if (value instanceof Map<?,?>)
			throw new IllegalArgumentException("Maps as Parameters are not supported");
		else if (value.getClass().isArray())
			throw new IllegalArgumentException("Array myst be of Primitive type");
		else
			set.add(value);
	}
		
	@Override
	public GraphProperties clone() {
		GraphProperties node = new GraphProperties();
		if (null != properties)
			for (Map.Entry<String, Set<Object>> entry : properties.entrySet()) 
				node.setProperty(entry.getKey(), new HashSet<Object>(entry.getValue()));
		return node;
	}
	
	public GraphProperties clone(String ... keys) {
		GraphProperties node = new GraphProperties();
		if (null != properties)
		for (String key : keys) {
			Set<Object> property = properties.get(key);
			if (null != property)
				node.setProperty(key, new HashSet<Object>(property));
		}
		return node;
	}

	@Override
	public String toString() {
		return this.properties.toString();
	}	
}
