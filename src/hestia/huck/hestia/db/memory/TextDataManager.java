package huck.hestia.db.memory;


import huck.hestia.db.Credit;
import huck.hestia.db.Debit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;


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
		
		MemoryAsset line2Asset(DataLine line);
		MemoryShop line2Shop(DataLine line);
		MemoryDebitCode line2DebitCode(DataLine line);
		MemoryCreditCode line2CreditCode(DataLine line);
		MemorySlip line2Slip(DataLine line);
		MemoryDebit line2Debit(DataLine line);
		MemoryCredit line2Credit(DataLine line);
	}
	
	static class DataLine {
		private String type;
		private int id;
		private ArrayList<String> columns;
		public DataLine(String type, int id, ArrayList<String> columns) {
			this.type = type;
			this.id = id;
			this.columns = columns;
		}
	}
	private static String textEncode(String src) {
		return src;
	}
	private static String textDecode(String src) {
		return src;
	}
	
	private static void write(OutputStream output, StringBuffer buf, DataLine line) throws Exception {
		buf.setLength(0);
		output.write(buf.toString().getBytes("UTF-8"));
	}
	private static DataLine read(String line) {
		return null;
	}
	

	private static class Dumper extends HestiaMemoryDB.Dumper {
		private OutputStream output;
		private Dumper(OutputStream output) {
			this.output = output;
		}

		@Override
		protected void dump(HestiaMemoryDB db) throws Exception {
			TextDataConverter converter = CURRENT_CONVERTER.newInst();
			StringBuffer buf = new StringBuffer();
			try {
				buf.setLength(0);
				buf.append("HESTIA").append("\n");
				buf.append("VERSION\t").append(textEncode(converter.getVersion())).append("\n");
				buf.append("\n");
				output.write(buf.toString().getBytes("UTF-8"));
				
				for( MemoryAsset asset : assetMap(db).values() ) {
					write( output, buf, converter.asset2Line(asset));
				}
				for( MemoryShop shop : shopMap(db).values() ) {
					write( output, buf, converter.shop2Line(shop));
				}
				for( MemoryDebitCode debitCode : debitCodeMap(db).values() ) {
					write( output, buf, converter.debitCode2Line(debitCode));
				}
				for( MemoryCreditCode creditCode : creditCodeMap(db).values() ) {
					write( output, buf, converter.creditCode2Line(creditCode));
				}
				for( MemorySlip slip : slipMap(db).values() ) {
					write( output, buf, converter.slip2Line(slip));
					for( Debit debit : db.retrieveDebitList(a->a.slip().id()==slip.id()) ) {
						write( output, buf, converter.debit2Line((MemoryDebit)debit));
					}
					for( Credit credit : db.retrieveCreditList(a->a.slip().id()==slip.id()) ) {
						write( output, buf, converter.credit2Line((MemoryCredit)credit));
					}
				}
			} finally {
				output.close();
			}
		}
	}
	
	private static class Loader extends HestiaMemoryDB.Loader {
		private String name;
		private InputStream source;

		private Loader(String name, InputStream source) {
			this.name = name;
			this.source = source;
		}

		private static <Data> void add(HestiaMemoryDB db, DataLine dataLine, Function<DataLine, Data> dataConverter, BiConsumer<HestiaMemoryDB, Data> addFunc) throws Exception {
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
						DataLine dataLine = read(line);
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

