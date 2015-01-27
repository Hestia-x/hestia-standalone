package huck.hestia.db.memory;

import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;
import java.util.function.BiConsumer;


public class BinaryDataManager {
	public static HestiaMemoryDB.Loader getLoader(String name, ReadableByteChannel channel) throws Exception {
		return new Loader(name, channel);
	}
	public static HestiaMemoryDB.Dumper getDumper(WritableByteChannel channel) throws Exception {
		return new Dumper(channel);
	}
	
	@FunctionalInterface
	interface CreateFunction<Data> {
		Data apply(short type, int id, ByteBuffer buf) throws Exception;
	}
	@FunctionalInterface
	interface WriteFunction<Data> {
	    void apply(ByteBuffer buf, Data data) throws Exception;
	}
	
	private static class Dumper extends HestiaMemoryDB.Dumper {
		private WritableByteChannel output;
		private Dumper(WritableByteChannel output) {
			this.output = output;
		}
		private <Data> void write(ByteBuffer buf, short type, int id, Data data, WriteFunction<Data> writeFunc) throws Exception {
			buf.clear();
			buf.putShort(type);
			buf.putInt(id);
			writeFunc.apply(buf, data);
			buf.flip();
			while( buf.hasRemaining() ) {
				output.write(buf);
			}
		}
		@Override
		protected void dump(HestiaMemoryDB db) throws Exception {
			try {
				ByteBuffer buf = ByteBuffer.allocate(1024);
				for( MemoryAsset asset : assetMap(db).values() ) {
					write(buf, (short)101, asset.id(), asset, this::asset);
				}
				for( MemoryShop shop : shopMap(db).values() ) {
					write(buf, (short)102, shop.id(), shop, this::shop);
				}
				for( MemoryDebitCode debitCode : debitCodeMap(db).values() ) {
					write(buf, (short)103, debitCode.id(), debitCode, this::debitCode);
				}
				for( MemoryCreditCode creditCode : creditCodeMap(db).values() ) {
					write(buf, (short)104, creditCode.id(), creditCode, this::creditCode);
				}
				for( MemorySlip slip : slipMap(db).values() ) {
					write(buf, (short)105, slip.id(), slip, this::slip);
				}
				for( MemoryDebit debit : debitMap(db).values() ) {
					write(buf, (short)106, debit.id(), debit, this::debit);
				}
				for( MemoryCredit credit : creditMap(db).values() ) {
					write(buf, (short)107, credit.id(), credit, this::credit);
				}
			} finally {
				output.close();
			}
		}
		private static void putString(ByteBuffer buf, String str) throws UnsupportedEncodingException {
			if( null == str ) {
				buf.putShort((short)0);
			} else {
				byte[] tmp = str.getBytes("UTF-8");
				buf.putShort((short)tmp.length);
				buf.put(tmp);
			}
		}
		private void asset(ByteBuffer buf, MemoryAsset asset) throws UnsupportedEncodingException {
			putString(buf, asset.name());
			putString(buf, asset.description());
		}
		private void shop(ByteBuffer buf, MemoryShop shop) throws UnsupportedEncodingException {
			putString(buf, shop.name());
		}
		private void debitCode(ByteBuffer buf, MemoryDebitCode debitCode) throws UnsupportedEncodingException {
			putString(buf, debitCode.name());
			if( null == debitCode.asset() ) {
				buf.putInt(-1);
			} else {
				buf.putInt(debitCode.asset().id());
			}
			putString(buf, debitCode.defaultDescription());
		}
		private void creditCode(ByteBuffer buf, MemoryCreditCode creditCode) throws UnsupportedEncodingException {
			putString(buf, creditCode.name());
			if( null == creditCode.asset() ) {
				buf.putInt(-1);
			} else {
				buf.putInt(creditCode.asset().id());
			}
			putString(buf, creditCode.defaultDescription());
		}
		private void slip(ByteBuffer buf, MemorySlip slip) throws UnsupportedEncodingException {
			putString(buf, slip.slipDttm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			buf.putInt(slip.shop().id());
		}
		private void debit(ByteBuffer buf, MemoryDebit debit) throws UnsupportedEncodingException {
			buf.putInt(debit.slip().id());
			buf.putInt(debit.debitCode().id());
			putString(buf, debit.description());
			buf.putInt(debit.unitPrice());
			buf.putInt(debit.quantity());
		}
		private void credit(ByteBuffer buf, MemoryCredit credit) throws UnsupportedEncodingException {
			buf.putInt(credit.slip().id());
			buf.putInt(credit.creditCode().id());
			putString(buf, credit.description());
			buf.putInt(credit.price());
		}
	}
	
	private static class Loader extends HestiaMemoryDB.Loader {
		private String name;
		private ReadableByteChannel source;
		private TreeMap<Integer, MemoryAsset> assetMap = new TreeMap<>();
		private TreeMap<Integer, MemoryShop> shopMap = new TreeMap<>();
		private TreeMap<Integer, MemoryDebitCode> debitCodeMap = new TreeMap<>();
		private TreeMap<Integer, MemoryCreditCode> creditCodeMap = new TreeMap<>();
		private TreeMap<Integer, MemorySlip> slipMap = new TreeMap<>();
		private TreeMap<Integer, MemoryDebit> debitMap = new TreeMap<>();
		private TreeMap<Integer, MemoryCredit> creditMap = new TreeMap<>();
		
		private Loader(String name, ReadableByteChannel source) {
			this.name = name;
			this.source = source;
		}

		private static <Data> void add(HestiaMemoryDB db, short type, int id, ByteBuffer buf, CreateFunction<Data> createFunc, BiConsumer<HestiaMemoryDB, Data> addFunc, TreeMap<Integer,Data> map) throws Exception {
			Data data = createFunc.apply(type, id, buf);
			addFunc.accept(db, data);
			map.put(id, data);
		}
		
		@Override
		protected String load(HestiaMemoryDB db) throws Exception {
			try {
				ByteBuffer buf = ByteBuffer.allocate(1024);
				buf.clear();
				
				boolean finished = false;
				read:
				while( !finished ) {				
					int readLen = source.read(buf);
					if( 0 > readLen ) {
						finished = true;
					}
					buf.flip();
					while( buf.hasRemaining() ) {
						int startPos = buf.position();
						try {
							short type = buf.getShort();
							int id = buf.getInt();
							switch( type ) {
							case 101: add(db, type, id, buf, this::asset, this::addAsset, assetMap); break;
							case 102: add(db, type, id, buf, this::shop, this::addShop, shopMap); break;
							case 103: add(db, type, id, buf, this::debitCode, this::addDebitCode, debitCodeMap); break;
							case 104: add(db, type, id, buf, this::creditCode, this::addCreditCode, creditCodeMap); break;
							case 105: add(db, type, id, buf, this::slip, this::addSlip, slipMap); break;
							case 106: add(db, type, id, buf, this::debit, this::addDebit, debitMap); break;
							case 107: add(db, type, id, buf, this::credit, this::addCredit, creditMap); break;
							}
						} catch( BufferUnderflowException ex) {
							if( !finished ) {
								buf.position(startPos);
								buf.compact();
								continue read;
							}
						}
					}
					buf.clear();
				}
			} finally {
				source.close();
			}
			return name;
		}
		private static String getString(ByteBuffer buf) throws UnsupportedEncodingException {
			short len = buf.getShort();
			if( 0 == len ) return null;
			byte[] data = new byte[len];
			buf.get(data);
			return new String(data, "UTF-8");
		}

		private MemoryAsset asset(short type, int id, ByteBuffer buf) throws UnsupportedEncodingException {
			String name = getString(buf);
			String description = getString(buf);
			return new MemoryAsset(id, name, description);
		}
		private MemoryShop shop(short type, int id, ByteBuffer buf) throws UnsupportedEncodingException {
			String name = getString(buf);
			return new MemoryShop(id, name);
		}
		private MemoryDebitCode debitCode(short type, int id, ByteBuffer buf) throws UnsupportedEncodingException {
			String name = getString(buf);
			int assetId = buf.getInt();
			String defaultDescription = getString(buf);
			MemoryAsset asset = assetMap.get(assetId);
			return new MemoryDebitCode(id, name, asset, defaultDescription);
		}
		private MemoryCreditCode creditCode(short type, int id, ByteBuffer buf) throws UnsupportedEncodingException {
			String name = getString(buf);
			int assetId = buf.getInt();
			String defaultDescription = getString(buf);
			MemoryAsset asset = assetMap.get(assetId);
			return new MemoryCreditCode(id, name, asset, defaultDescription);
		}
		private MemorySlip slip(short type, int id, ByteBuffer buf) throws UnsupportedEncodingException {
			String slipDttmStr = getString(buf);
			LocalDateTime slipDttm = LocalDateTime.parse(slipDttmStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			int shopId = buf.getInt();
			MemoryShop shop = shopMap.get(shopId);
			return new MemorySlip(id, slipDttm, shop);
		}
		private MemoryDebit debit(short type, int id, ByteBuffer buf) throws UnsupportedEncodingException {
			int slipId = buf.getInt();
			int debitCodeId = buf.getInt();
			String description = getString(buf);
			int unitPrice = buf.getInt();
			int quantity = buf.getInt();
			MemorySlip slip = slipMap.get(slipId);
			MemoryDebitCode debitCode = debitCodeMap.get(debitCodeId);
			return new MemoryDebit(id, slip, debitCode, description, unitPrice, quantity);
		}
		private MemoryCredit credit(short type, int id, ByteBuffer buf) throws UnsupportedEncodingException {
			int slipId = buf.getInt();
			int creditCodeId = buf.getInt();
			String description = getString(buf);
			int price = buf.getInt();
			MemorySlip slip = slipMap.get(slipId);
			MemoryCreditCode creditCode = creditCodeMap.get(creditCodeId);
			return new MemoryCredit(id, slip, creditCode, description, price);
		}
	}

}

