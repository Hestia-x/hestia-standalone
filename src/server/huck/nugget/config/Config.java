package huck.nugget.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class Config {
	private LinkedHashSet<String> nameSet = new LinkedHashSet<String>();
	private LinkedHashMap<String, ArrayList<Config>> childMap = new LinkedHashMap<>();
	private LinkedHashMap<String, ArrayList<String>> valueMap = new LinkedHashMap<>();

	public void setValue(String name, String value) {
		ArrayList<String> valueList = new ArrayList<>();
		valueList.add(value);
		childMap.remove(name);
		valueMap.put(name, valueList);
		nameSet.add(name);
	}
	public void addValue(String name, String value) {
		ArrayList<String> valueList = valueMap.get(name);
		if( null == valueList ) {
			setValue(name, value);
		} else {
			valueList.add(value);
		}
	}
	public String getValue(String name) {
		ArrayList<String> valueList = valueMap.get(name);
		if( null == valueList ) return null;
		else return valueList.get(0);
	}
	public Collection<String> getValues(String name) {
		ArrayList<String> valueList = valueMap.get(name);
		if( null == valueList ) return Collections.emptyList();
		else return Collections.unmodifiableCollection(valueList);
	}
	public Collection<String> getValueNames() {
		return Collections.unmodifiableCollection(valueMap.keySet());
	}

	public void setChild(String name, Config child) {
		ArrayList<Config> childList = new ArrayList<>();
		childList.add(child);
		valueMap.remove(name);
		childMap.put(name, childList);
		nameSet.add(name);
	}
	public void addChild(String name, Config child) {
		ArrayList<Config> childList = childMap.get(name);
		if( null == childList ) {
			setChild(name, child);
		} else {
			childList.add(child);
		}
	}
	public Config getChild(String name) {
		ArrayList<Config> childList = childMap.get(name);
		if( null == childList ) return null;
		else return childList.get(0);
	}
	public Collection<Config> getChildren(String name) {
		ArrayList<Config> childList = childMap.get(name);
		if( null == childList ) return Collections.emptyList();
		else return Collections.unmodifiableCollection(childList);
	}
	public Collection<String> getChildNames() {
		return Collections.unmodifiableCollection(childMap.keySet());
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		toString(buf, 0);
		return buf.toString();
	}
	
	private void toString(StringBuffer buf, int indent) {
		int maxNameLen = 0; 
		for( String name : nameSet ) {
			maxNameLen = Math.max(name.length(), maxNameLen);
		}
		String[] space = new String[maxNameLen];
		space[0] = "";
		for( int i=1; i<maxNameLen; i++ ) {
			space[i] = space[i-1] + " ";
		}
		String tab = "";
		for( int i=0; i<indent; i++ ) {
			tab += "\t";
		}
		for( String name : nameSet ) {
			if( valueMap.containsKey(name) ) {
				ArrayList<String> valueList = valueMap.get(name);
				buf.append(tab).append(name).append(space[maxNameLen-name.length()]);
				if( 3 < valueList.size() ) {
					buf.append(" [\n");
					for( String value : valueList ) {
						buf.append(tab).append("\t").append(value).append("\n");
					}
					buf.append(tab).append("]\n");
				} else {
					for( String value : valueList ) {
						buf.append(" ").append(value);
					}
					buf.append("\n");
				}
			} else if( childMap.containsKey(name) ) {
				ArrayList<Config> childList = childMap.get(name);
				buf.append(tab).append(name);
				if( 1 < childList.size() ) {
					buf.append(" [\n");
					for( Config child : childList ) {
						buf.append(tab).append("\t").append("{\n");
						child.toString(buf, indent+2);
						buf.append(tab).append("\t").append("}\n");
					}
					buf.append(tab).append("]\n");
				} else {
					buf.append(" {\n");
					childList.get(0).toString(buf, indent+1);
					buf.append(tab).append("}\n");
				}
			}			
		}

	}
}
