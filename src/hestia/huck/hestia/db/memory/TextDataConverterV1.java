package huck.hestia.db.memory;

import huck.hestia.db.memory.TextDataManager.DataLine;
import huck.hestia.db.memory.TextDataManager.TextDataConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;


public class TextDataConverterV1 implements TextDataConverter {
	@Override
	public TextDataConverter newInst() {
		return new TextDataConverterV1();
	}

	@Override
	public String getVersion() {
		return "1";
	}

	@Override
	public DataLine asset2Line(MemoryAsset asset) {
		return new DataLine("asset", asset.id(), asset.name(), asset.description());
	}
	@Override
	public DataLine shop2Line(MemoryShop shop) {
		return new DataLine("shop", shop.id(), shop.name());
	}
	@Override
	public DataLine debitCode2Line(MemoryDebitCode debitCode) {
		if( null != debitCode.asset() ) {
			return new DataLine("debitCode", debitCode.id(), debitCode.name(), ""+debitCode.asset().id(), debitCode.defaultDescription());
		} else {
			return new DataLine("debitCode", debitCode.id(), debitCode.name(), null, debitCode.defaultDescription());
		}
	}
	@Override
	public DataLine creditCode2Line(MemoryCreditCode creditCode) {
		if( null != creditCode.asset() ) {
			return new DataLine("creditCode", creditCode.id(), creditCode.name(), ""+creditCode.asset().id(), creditCode.defaultDescription());
		} else {
			return new DataLine("creditCode", creditCode.id(), creditCode.name(), null, creditCode.defaultDescription());
		}
	}
	@Override
	public DataLine slip2Line(MemorySlip slip) {
		return new DataLine("slip", slip.id(), slip.slipDttm().format(DateTimeFormatter.ISO_DATE_TIME), ""+slip.shop().id());
	}
	@Override
	public DataLine debit2Line(MemoryDebit debit) {
		return new DataLine("debit", debit.id(), ""+debit.slip().id(), ""+debit.debitCode().id(), debit.description(), ""+debit.unitPrice(), ""+debit.quantity());
	}
	@Override
	public DataLine credit2Line(MemoryCredit credit) {
		return new DataLine("credit", credit.id(), ""+credit.slip().id(), ""+credit.creditCode().id(), credit.description(), ""+credit.price());
	}
	
	private HashMap<Integer, MemoryAsset> assetMap = new HashMap<>();
	private HashMap<Integer, MemoryShop> shopMap = new HashMap<>();
	private HashMap<Integer, MemoryDebitCode> debitCodeMap = new HashMap<>();
	private HashMap<Integer, MemoryCreditCode> creditCodeMap = new HashMap<>();
	private HashMap<Integer, MemorySlip> slipMap = new HashMap<>();
	
	private String getStr(DataLine line, int idx, boolean nullable) throws Exception {
		if( idx >= line.columnList().size() ) {
			throw new Exception("unknown format: " + line.getLine());
		}
		String str = line.columnList().get(idx);
		if( !nullable && null == str ) {
			throw new Exception("unknown format: " + line.getLine());
		}
		return str;
	}
	private Integer getInt(DataLine line, int idx, boolean nullable) throws Exception {
		String str = getStr(line, idx, nullable);
		if( null == str ) {
			return null;
		}
		try {
			return Integer.parseInt(str);
		} catch( Exception ex ) {
			throw new Exception("unknown format: " + line.getLine(), ex);
		}
	}
	private LocalDateTime getLocalDateTime(DataLine line, int idx, boolean nullable) throws Exception {
		String str = getStr(line, idx, nullable);
		if( null == str ) {
			return null;
		}
		try {
			return LocalDateTime.parse(str, DateTimeFormatter.ISO_DATE_TIME);
		} catch( Exception ex ) {
			throw new Exception("unknown format: " + line.getLine(), ex);
		}
	}
	@Override
	public MemoryAsset line2Asset(DataLine line) throws Exception {
		String name = getStr(line, 0, false);
		String description = getStr(line, 1, true);
		MemoryAsset asset = new MemoryAsset(line.getId(), name, description);
		assetMap.put(asset.id(), asset);
		return asset;
	}	
	@Override
	public MemoryShop line2Shop(DataLine line) throws Exception {
		String name = getStr(line, 0, false);
		MemoryShop shop = new MemoryShop(line.getId(), name);
		shopMap.put(shop.id(), shop);
		return shop;
	}
	@Override
	public MemoryDebitCode line2DebitCode(DataLine line) throws Exception {
		String name = getStr(line, 0, false);
		Integer assetId = getInt(line, 1, true);
		String defaultDescription = getStr(line, 2, true);
		MemoryDebitCode debitCode;
		if( null == assetId ) {
			debitCode = new MemoryDebitCode(line.getId(), name, null, defaultDescription);
		} else {
			MemoryAsset asset = assetMap.get(assetId);
			if( null == asset ) {
				throw new Exception("unknown assetId: " + line.getLine());
			}
			debitCode = new MemoryDebitCode(line.getId(), name, asset, defaultDescription);
		}
		debitCodeMap.put(debitCode.id(), debitCode);
		return debitCode;
	}
	@Override
	public MemoryCreditCode line2CreditCode(DataLine line) throws Exception {
		String name = getStr(line, 0, false);
		Integer assetId = getInt(line, 1, true);
		String defaultDescription = getStr(line, 2, true);
		MemoryCreditCode creditCode;
		if( null == assetId ) {
			creditCode = new MemoryCreditCode(line.getId(), name, null, defaultDescription);
		} else {
			MemoryAsset asset = assetMap.get(assetId);
			if( null == asset ) {
				throw new Exception("unknown assetId: " + line.getLine());
			}
			creditCode = new MemoryCreditCode(line.getId(), name, asset, defaultDescription);
		}
		creditCodeMap.put(creditCode.id(), creditCode);
		return creditCode;
	}
	@Override
	public MemorySlip line2Slip(DataLine line) throws Exception {
		LocalDateTime slipDttm = getLocalDateTime(line, 0, false);
		Integer shopId = getInt(line, 1, false);		
		MemoryShop shop = shopMap.get(shopId);
		MemorySlip slip = new MemorySlip(line.getId(), slipDttm, shop);
		slipMap.put(slip.id(), slip);
		return slip;
	}

	@Override
	public MemoryDebit line2Debit(DataLine line) throws Exception {
		Integer slipId = getInt(line, 0, false);
		Integer debitCodeId = getInt(line, 1, false);
		String description = getStr(line, 2, false);
		Integer unitPrice = getInt(line, 3, false);
		Integer quantity = getInt(line, 4, false);
		
		MemorySlip slip = slipMap.get(slipId);
		MemoryDebitCode debitCode = debitCodeMap.get(debitCodeId);
		return new MemoryDebit(line.getId(), slip, debitCode, description, unitPrice, quantity);
	}

	@Override
	public MemoryCredit line2Credit(DataLine line) throws Exception {
		Integer slipId = getInt(line, 0, false);
		Integer creditCodeId = getInt(line, 1, false);
		String description = getStr(line, 2, false);
		Integer price = getInt(line, 3, false);
		
		MemorySlip slip = slipMap.get(slipId);
		MemoryCreditCode creditCode = creditCodeMap.get(creditCodeId);
		return new MemoryCredit(line.getId(), slip, creditCode, description, price);
	}

}

