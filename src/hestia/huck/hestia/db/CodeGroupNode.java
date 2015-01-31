package huck.hestia.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CodeGroupNode {
	private String name;
	private String pattern;
	private Integer value;
	private CodeGroupNode parent;
	private ArrayList<CodeGroupNode> childList;
	
	private CodeGroupNode(String name, String pattern, Integer value) {
		this.name = name;
		this.pattern = pattern;
		this.value = value;
		this.parent = null;
		this.childList = new ArrayList<>();
	}
	
	public static CodeGroupNode ofDebitCode(DebitCode debitCode, int value) {
		return new CodeGroupNode(debitCode.name(), ""+debitCode.id(), value);
	}
	public static CodeGroupNode ofCreditCode(CreditCode creditCode, int value) {
		return new CodeGroupNode(creditCode.name(), ""+creditCode.id(), value);
	}
	public static CodeGroupNode ofCodeGroup(CodeGroup codeGroup) {
		return new CodeGroupNode(codeGroup.name(), codeGroup.pattern(), null);
	}
	public String name() {
		return name;
	}
	public String pattern() {
		return pattern;
	}
	public int value() {
		if( null == value ) {
			return childList.stream().collect(Collectors.summingInt(a->a.value()));
		} else {
			return value;
		}
	}
	
	public static List<CodeGroupNode> buildCodeGroupTree(List<CodeGroupNode> nodeList) {
		ArrayList<CodeGroupNode> result = new ArrayList<>();
		for( CodeGroupNode nNode : nodeList ) {
			ArrayList<CodeGroupNode> forRemove = new ArrayList<>();
			for( CodeGroupNode processed : result ) {
				if( null == nNode.parent && nNode.isChildOf(processed) ) {
					CodeGroupNode parentNode = processed;
					while( true ) {
						CodeGroupNode newParent = parentNode.findNextParent(nNode);
						if( null != newParent ) {
							parentNode = newParent;
						} else {
							break;
						}
					}
					parentNode.childList.add(nNode);
					nNode.parent = parentNode;
				} else if( processed.isChildOf(nNode) ) {
					nNode.childList.add(processed);
					processed.parent = nNode;
					forRemove.add(processed);
				}
			}
			if( null == nNode.parent ) {
				result.add(nNode);
			}
			result.removeAll(forRemove);
		}
		return Collections.unmodifiableList(result);
	}
	
	private static boolean isDigit(char ch) {
		switch(ch) {
		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8':	case '9':
			return true;
		}
		return false;
	}
	private CodeGroupNode findNextParent(CodeGroupNode target) {
		for( CodeGroupNode node : childList ) {
			if( target.isChildOf(node) ) {
				return node;
			}
		}
		return null;	
	}
	private boolean isChildOf(CodeGroupNode node) {
		if( null != node.value ) {
			return false;
		}
		String aPattern = pattern;
		String bPattern = node.pattern;
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
}
