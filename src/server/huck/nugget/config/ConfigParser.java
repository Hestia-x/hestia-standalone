package huck.nugget.config;


public class ConfigParser {
	public static Config parseConfig(CharSequence src) {
		Tokenizer tokenizer = new Tokenizer(src);
		return parseConfig(tokenizer);
	}
	
	private static Config parseConfig(Tokenizer tokenizer) {
		Config config = new Config();
		int status = 0; // 0 <- name, 1 <- value
		String token;
		
		String curName = "";
		while( null != (token=tokenizer.pop()) ) {
			if( "}".equals(token) ) {
				break;
			}
			if( "\n".equals(token) ) {
				if( 0 != status ) {
					status = 0;
				}
				continue;
			}
			if( 0 == status ) {
				curName = token;
				status = 1;
			} else if( 1 == status ) {
				if( "{".equals(token) ) {
					config.addChild(curName, parseConfig(tokenizer));
				} else if("[".equals(token) ) {
					parseList(config, curName, tokenizer);
				} else {
					config.addValue(curName, token);
				}
			}
		}
		return config;
	}
	
	private static void parseList(Config config, String name, Tokenizer tokenizer) {
		String token;
		while( null != (token=tokenizer.pop()) ) {
			if( "]".equals(token) ) {
				break;
			}
			if( "\n".equals(token) ) {
				continue;
			}
			if( "{".equals(token) ) {
				config.addChild(name, parseConfig(tokenizer));
			} else {
				config.addValue(name, token);
			}
		}
	}
}
