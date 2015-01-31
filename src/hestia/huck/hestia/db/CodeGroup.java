package huck.hestia.db;

import java.util.ArrayList;
import java.util.List;

public class CodeGroup {
	private int id;
	private String pattern;
	
	public int id() {
		return id;
	}
	public String pattern() {
		return pattern;
	}
	public int exactCount() {
		int cnt = 0;
		for( int i=0; i<pattern.length(); i++ ) {
			isDigit(pattern.charAt(i));
		}
		return cnt;
	}
	public CodeGroup(int id, String pattern) {
		this.id = id;
		this.pattern = pattern;
	}
	private static boolean isDigit(char ch) {
		switch(ch) {
		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8':	case '9':
			return true;
		}
		return false;
	}
	
	private boolean isChildOf(CodeGroup codeGroup) {
		String aPattern = pattern;
		String bPattern = codeGroup.pattern;
		if( aPattern.equals(bPattern) ) {
			return false;
		}

		int aIdx = aPattern.length()-1;
		int bIdx = bPattern.length()-1;
		while( true ) {
			if( bIdx < 0 ) {
				return true;
			}
			if( aIdx < 0 ) {
				return false;
			}
			char aCh = aPattern.charAt(aIdx);
			char bCh = bPattern.charAt(bIdx);
			boolean aDigit = isDigit(aCh);
			boolean bDigit = isDigit(bCh);
			if( !aDigit && bDigit ) {
				return false;
			}
			if( aDigit && bDigit && aCh != bCh ) {
				return false;
			}
			aIdx--;
			bIdx--;
		}
	}
	
	public static CodeGroupNode findParent(CodeGroupNode s, List<CodeGroupNode> list) {
		for( CodeGroupNode node : list ) {
			if( s.value.isChildOf(node.value) ) {
				return node;
			}
		}
		return null;
	}
	public static class CodeGroupNode {
		public CodeGroup value;
		public CodeGroupNode parent;		
		public List<CodeGroupNode> childList;
	}
	
	public static void main(String... args) throws Exception {
		ArrayList<CodeGroup> list = new ArrayList<>();
		int idx = 0;
		list.add(new CodeGroup(idx++, "1xxx"));
		list.add(new CodeGroup(idx++, "1xx1"));
		list.add(new CodeGroup(idx++, "11x1"));
		list.add(new CodeGroup(idx++, "12x1"));
		list.add(new CodeGroup(idx++, "1231"));
		list.add(new CodeGroup(idx++, "xxx1"));
		list.add(new CodeGroup(idx++, "1xxx"));
		list.add(new CodeGroup(idx++, "21x"));
		list.add(new CodeGroup(idx++, "22x"));
		list.add(new CodeGroup(idx++, "2x3"));
		list.add(new CodeGroup(idx++, "223"));
		
		ArrayList<CodeGroupNode> processedList = new ArrayList<>();
		for( CodeGroup codeGroup : list ) {
			CodeGroupNode nNode = new CodeGroupNode();
			nNode.value = codeGroup;
			nNode.parent = null;
			nNode.childList = new ArrayList<>();
			
			ArrayList<CodeGroupNode> forRemove = new ArrayList<>();
			for( CodeGroupNode processed : processedList ) {
				if( null == nNode.parent && codeGroup.isChildOf(processed.value) ) {
					CodeGroupNode parentNode = processed;
					while( true ) {
						CodeGroupNode newParent = findParent(nNode, parentNode.childList);
						if( null != newParent ) {
							parentNode = newParent;
						} else {
							break;
						}
					}
					parentNode.childList.add(nNode);
					nNode.parent = parentNode;
				} else if( processed.value.isChildOf(codeGroup) ) {
					nNode.childList.add(processed);
					processed.parent = nNode;
					forRemove.add(processed);
				}
			}
			if( null == nNode.parent ) {
				processedList.add(nNode);
			}
			processedList.removeAll(forRemove);
		}
		
		for( CodeGroupNode n : processedList ) {
			printNode(n, 0);
		}
	}
	
	private static void printNode(CodeGroupNode node, int tab) {
		for( int i=0; i<tab; i++ ) {
			System.out.print("\t");
		}
		System.out.println(node.value.pattern());
		for( CodeGroupNode childNode : node.childList ) {
			printNode(childNode, tab+1);
		}
	}
}
