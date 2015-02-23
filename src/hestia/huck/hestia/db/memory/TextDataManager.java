package huck.hestia.db.memory;


import huck.hestia.db.Credit;
import huck.hestia.db.Debit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;


public class TextDataManager {
	public static HestiaMemoryDB.Loader getLoader(String name, InputStream in) throws Exception {
		return new Loader(name, in);
	}
	public static HestiaMemoryDB.Dumper getDumper(OutputStream out) throws Exception {
		return new Dumper(out);
	}
	
	private static TextDataConverter CURRENT_CONVERTER;
	private static HashMap<String, TextDataConverter> DATA_CONVERTER_MAP = new HashMap<>();
	static {
		TextDataConverter[] converterList = new TextDataConverter[] {
			new TextDataConverterV1(),
		};
		
		for( TextDataConverter converter : converterList ) {
			DATA_CONVERTER_MAP.put(converter.getVersion(), converter);
		}
		
		CURRENT_CONVERTER = converterList[0];
	}

	static interface TextDataConverter {
		TextDataConverter newInst();
		String getVersion();
		
		DataLine asset2Line(MemoryAsset asset);
		DataLine shop2Line(MemoryShop asset);
		DataLine debitCode2Line(MemoryDebitCode asset);
		DataLine creditCode2Line(MemoryCreditCode asset);
		DataLine slip2Line(MemorySlip asset);
		DataLine debit2Line(MemoryDebit asset);
		DataLine credit2Line(MemoryCredit asset);
		
		MemoryAsset line2Asset(DataLine line) throws Exception;
		MemoryShop line2Shop(DataLine line) throws Exception;
		MemoryDebitCode line2DebitCode(DataLine line) throws Exception;
		MemoryCreditCode line2CreditCode(DataLine line) throws Exception;
		MemorySlip line2Slip(DataLine line) throws Exception;
		MemoryDebit line2Debit(DataLine line) throws Exception;
		MemoryCredit line2Credit(DataLine line) throws Exception;
	}
	
	static class DataLine {
		private String line;
		private String type;
		private int id;
		private ArrayList<String> columnList;
		public String getLine() {
			return line;
		}
		public int getId() {
			return id;
		}
		public List<String> columnList() {
			return columnList;
		}
		public DataLine(String line) throws Exception {
			this.line = line;
			String[] columns = line.split("\t");
			if( 2 >= columns.length ) {
				throw new Exception("unknown data format: " + line);
			}
			this.type = textDecode(columns[0]);
			try {
				this.id = Integer.parseInt(columns[1]);
			} catch(Exception ex){
				throw new Exception("unknown data format: " + line);
			}
			this.columnList = new ArrayList<>();
			for( int i=2; i<columns.length; i++ ) {
				this.columnList.add(textDecode(columns[i]));
			}
		}
		public DataLine(String type, int id, String... columns) {
			StringBuffer buf = new StringBuffer();
			buf.append(textEncode(type)).append("\t");
			buf.append(id).append("\t");
			for( String column : columns ) {
				buf.append(textEncode(column)).append("\t");
			}
			buf.deleteCharAt(buf.length()-1);
			this.line = buf.toString();
			this.type = type;
			this.id = id;
			this.columnList = new ArrayList<>();
			if( null != columns ) {
				for( int i=0; i<columns.length; i++ ) {
					columnList.add(columns[i]);
				}
			}
		}
	}
	private static String textEncode(String src) {
		if( null == src ) {
			return "#0";
		}
		if( "".equals(src) ) {
			return "#S";
		}
		StringBuffer buf = new StringBuffer();
		for( int i=0; i<src.length(); i++ ) {
			char ch = src.charAt(i);
			switch(ch) {
			case '\r': buf.append("#r"); break;
			case '\n': buf.append("#n"); break;
			case '\t': buf.append("#t"); break;
			case '#': buf.append("##"); break;
			default: buf.append(ch); break;
			}
		}
		return buf.toString();
	}
	private static String textDecode(String src) {
		if( null == src ) return null;
		if( "#0".equals(src) ) {
			return null;
		}
		if( "#S".equals(src) ) {
			return "";
		}
		StringBuffer buf = new StringBuffer(src.length());
		boolean isEscape = false;
		for( int i=0; i<src.length(); i++ ) {
			char ch = src.charAt(i);
			if( isEscape ) {
				switch(ch) {
				case 'r': buf.append('\r'); break;
				case 'n': buf.append('\n'); break;
				case 't': buf.append('\t'); break;
				default: buf.append(ch); break;
				}				
				isEscape = false;
			} else {
				if( '#' == ch ) {
					isEscape = true;
				} else {
					buf.append(ch);
				}
			}
		}
		return buf.toString();
	}
	
	private static void write(OutputStream output, DataLine line) throws Exception {
		output.write((line.getLine()+"\n").getBytes("UTF-8"));
	}
	private static class Dumper extends HestiaMemoryDB.Dumper {
		private OutputStream output;
		private Dumper(OutputStream output) {
			this.output = output;
		}

		@Override
		protected void dump(HestiaMemoryDB db) throws Exception {
			TextDataConverter converter = CURRENT_CONVERTER.newInst();
			try {
				StringBuffer buf = new StringBuffer();
				buf.append("HESTIA").append("\n");
				buf.append("VERSION\t").append(textEncode(converter.getVersion())).append("\n");
				buf.append("\n");
				output.write(buf.toString().getBytes("UTF-8"));
				
				for( MemoryAsset asset : assetMap(db).values() ) {
					write(output, converter.asset2Line(asset));
				}
				for( MemoryShop shop : shopMap(db).values() ) {
					write(output, converter.shop2Line(shop));
				}
				for( MemoryDebitCode debitCode : debitCodeMap(db).values() ) {
					write(output, converter.debitCode2Line(debitCode));
				}
				for( MemoryCreditCode creditCode : creditCodeMap(db).values() ) {
					write(output, converter.creditCode2Line(creditCode));
				}
				for( MemorySlip slip : slipMap(db).values() ) {
					output.write("\n".getBytes("UTF-8"));
					write(output, converter.slip2Line(slip));
					for( Debit debit : db.retrieveDebitList(a->a.slip().id()==slip.id()) ) {
						write(output, converter.debit2Line((MemoryDebit)debit));
					}
					for( Credit credit : db.retrieveCreditList(a->a.slip().id()==slip.id()) ) {
						write(output, converter.credit2Line((MemoryCredit)credit));
					}
				}
			} finally {
				output.close();
			}
		}
	}
	@FunctionalInterface
	private static interface ConvertFunc<FROM, TO> {
		TO apply(FROM from) throws Exception;
	}
	private static class Loader extends HestiaMemoryDB.Loader {
		private String name;
		private InputStream source;

		private Loader(String name, InputStream source) {
			this.name = name;
			this.source = source;
		}

		private static <Data> void add(HestiaMemoryDB db, DataLine dataLine, ConvertFunc<DataLine, Data> dataConverter, BiConsumer<HestiaMemoryDB, Data> addFunc) throws Exception {
			Data data = dataConverter.apply(dataLine);
			addFunc.accept(db, data);
		}
		
		@Override
		protected String load(HestiaMemoryDB db) throws Exception {
			try {
				TextDataConverter converter = null;
				BufferedReader reader = new BufferedReader(new InputStreamReader(source, "UTF-8"));
				HashMap<String, String> header = new HashMap<String, String>();
				String line;
				int stat = 0;
				while( null != (line=reader.readLine()) ) {
					if( 0 == stat ) {
						if( !"HESTIA".equals(line) ) {
							throw new Exception("unknown format");
						}
						stat = 1;
					} else if( 1 == stat ) {
						line = line.trim();
						if( !line.isEmpty() ) {
							String name;
							String value;
							int idx = line.indexOf('\t');
							if( -1 == idx ) {
								name = textDecode(line);
								value = null;
							} else {
								name = textDecode(line.substring(0, idx));
								value = textDecode(line.substring(idx+1, line.length()));
							}
							header.put(name, value);
						} else {
							stat = 2;
							String version = header.get("VERSION");
							converter = DATA_CONVERTER_MAP.get(version);
							if( null == converter ) {
								throw new Exception("unsupported version");
							}
							converter = converter.newInst();
						}
					} else if( 2 == stat ) {
						if( line.isEmpty() ) continue;
						
						DataLine dataLine = new DataLine(line);
						switch( dataLine.type ) {
						case "asset": add(db, dataLine, converter::line2Asset, this::addAsset); break;
						case "shop": add(db, dataLine, converter::line2Shop, this::addShop); break;
						case "debitCode": add(db, dataLine, converter::line2DebitCode, this::addDebitCode); break;
						case "creditCode": add(db, dataLine, converter::line2CreditCode, this::addCreditCode); break;
						case "slip": add(db, dataLine, converter::line2Slip, this::addSlip); break;
						case "debit": add(db, dataLine, converter::line2Debit, this::addDebit); break;
						case "credit": add(db, dataLine, converter::line2Credit, this::addCredit); break;
						}
					}
				}
			} finally {
				source.close();
			}
			return name;
		}
	}
}

