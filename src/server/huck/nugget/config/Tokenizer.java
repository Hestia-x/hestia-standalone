package huck.nugget.config;

class Tokenizer {
	private CharSequence src;
	private int curIdx;	
	private String next;
	
	public Tokenizer(CharSequence src) {
		this.src = src;
		this.curIdx = 0;
		this.next = null;
		next();
	}
	
	public String top() {
		return next;
	}
	
	public String pop() {
		String result = next;
		next();
		return result;
	}
	
	private void next() {
		boolean isNewLine = false;
		for( ; curIdx < src.length(); curIdx++ ) {
			char ch = src.charAt(curIdx);
			if( '\n' == ch ) {
				isNewLine = true;
			}
			if( 0 > " \t\r\n".indexOf(ch) ) {
				break;
			}
		}
		if( isNewLine ) {
			next = "\n";
			return;
		}
		if( curIdx >= src.length() ) {
			next = null;
			return;
		}
		if( 0 <= "{}[]".indexOf(src.charAt(curIdx)) ) {
			next = ""+src.charAt(curIdx);
			curIdx++;
			return;
		}
		boolean inQuote = false;
		boolean inEscape = false;
		StringBuffer token = new StringBuffer();
		search:
		for( ; curIdx < src.length(); curIdx++ ) {
			char ch = src.charAt(curIdx);
			if( inEscape ) {
				token.append(ch);
				inEscape = false;
				continue;
			}
			
			if( inQuote ) {
				switch( ch ) {
				case '\\':
					inEscape = true;
					continue search;
				case '"':
					curIdx++;
					break search;
				default:
					token.append(ch);
				}
			} else {
				if( 0 <= " \t\r\n".indexOf(src.charAt(curIdx)) ) {
					break search;
				}
				switch( ch ) {
				case '{':
				case '}':
				case '[':
				case ']':
					break search;
				case '\\':
					inEscape = true;
					continue search;
				case '"':
					inQuote = true;
					continue search;
				default:
					token.append(ch);
				}
			}
		}
		if( 0 == token.length() && !inQuote) {
			next = null;
		} else {
			next = token.toString();
		}
	}
}
