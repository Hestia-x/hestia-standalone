package huck.hestia.db.memory;

import huck.hestia.db.Asset;
import huck.hestia.db.Credit;
import huck.hestia.db.CreditCode;
import huck.hestia.db.Debit;
import huck.hestia.db.DebitCode;
import huck.hestia.db.HestiaDB;
import huck.hestia.db.Shop;
import huck.hestia.db.Slip;
import huck.hestia.db.memory.HestiaMemoryDB.Dumper;
import huck.hestia.db.memory.HestiaMemoryDB.Loader;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class HestiaMemoryDB implements HestiaDB<Loader, Dumper> {
	private <Target, Implement extends Target> List<Target> retrieveListFromMap(TreeMap<Integer, Implement> map, Predicate<Target> predicate, ToIntFunction<Target> keyExtractor) {
		Supplier<List<Target>> supplier = ArrayList::new;
		List<Target> result;
		if( null == predicate ) {
			result = supplier.get();
			result.addAll(map.values());
		} else {
			result = map.values().stream().filter(predicate).collect(Collectors.toList());
			result.sort(Comparator.comparingInt(keyExtractor));
		}
		return Collections.unmodifiableList(result);
	}
	
	@Override
	public List<Asset> retrieveAssetList(Predicate<Asset> predicate) {
		return retrieveListFromMap(assetMap, predicate, a->a.id());
	}
	@Override
	public List<Shop> retrieveShopList(Predicate<Shop> predicate) {
		return retrieveListFromMap(shopMap, predicate, a->a.id());
	}
	@Override
	public List<DebitCode> retrieveDebitCodeList(Predicate<DebitCode> predicate) {
		return retrieveListFromMap(debitCodeMap, predicate, a->a.id());
	}
	@Override
	public List<CreditCode> retrieveCreditCodeList(Predicate<CreditCode> predicate) {
		return retrieveListFromMap(creditCodeMap, predicate, a->a.id());
	}
	@Override
	public List<Slip> retrieveSlipList(Predicate<Slip> predicate) {
		return retrieveListFromMap(slipMap, predicate, a->a.id());
	}
	@Override
	public List<Debit> retrieveDebitList(Predicate<Debit> predicate) {
		return retrieveListFromMap(debitMap, predicate, a->a.id());
	}
	@Override
	public List<Credit> retrieveCreditList(Predicate<Credit> predicate) {
		return retrieveListFromMap(creditMap, predicate, a->a.id());
	}
	
	@Override
	public Slip insertSlip(LocalDateTime dttm, int shopId) {
		MemoryShop shop = shopMap.get(shopId);
		if( null == shop ) {
			throw new NoSuchElementException("no shopId: " + shopId);
		}
		int lastKey = 0;
		if( !slipMap.isEmpty() ) {
			lastKey = slipMap.lastKey();
		}
		MemorySlip slip = new MemorySlip(lastKey+1, dttm, shop);
		slipMap.put(slip.id(), slip);
		modified = true;
		return slip;
	}
	@Override
	public Debit insertDebit(int slipId, int debitCodeId, String description, int unitPrice, int quantity) {
		MemorySlip slip = slipMap.get(slipId);
		MemoryDebitCode debitCode = debitCodeMap.get(debitCodeId);
		if( null == slip ) {
			throw new NoSuchElementException("no slipId: " + slipId);
		}
		if( null == debitCode ) {
			throw new NoSuchElementException("no debitCodeId: " + debitCodeId);
		}
		if( null == description || description.isEmpty() ) {
			description = debitCode.defaultDescription();
		}
		if( null == description ) {
			description = debitCode.name();
		}
		int lastKey = 0;
		if( !debitMap.isEmpty() ) {
			lastKey = debitMap.lastKey();
		}
		MemoryDebit debit = new MemoryDebit(lastKey+1, slip, debitCode, description, unitPrice, quantity);
		debitMap.put(debit.id(), debit);
		modified = true;
		return debit;
	}
	@Override
	public Credit insertCredit(int slipId, int creditCodeId, String description, int price) {
		MemorySlip slip = slipMap.get(slipId);
		MemoryCreditCode creditCode = creditCodeMap.get(creditCodeId);
		if( null == slip ) {
			throw new NoSuchElementException("no slipId: " + slipId);
		}
		if( null == creditCode ) {
			throw new NoSuchElementException("no creditCodeId: " + creditCodeId);
		}
		if( null == description || description.isEmpty() ) {
			description = creditCode.defaultDescription();
		}
		if( null == description ) {
			description = creditCode.name();
		}
		int lastKey = 0;
		if( !creditMap.isEmpty() ) {
			lastKey = creditMap.lastKey();
		}
		MemoryCredit credit = new MemoryCredit(lastKey+1, slip, creditCode, description, price);
		creditMap.put(credit.id(), credit);
		modified = true;
		return credit;
	}
	
	@Override
	public void updateSlip(int slipId, int shopId, LocalDateTime slipDttm) {
		MemorySlip slip =slipMap.get(slipId);
		MemoryShop shop = shopMap.get(shopId);
		if( null == slip ) {
			throw new NoSuchElementException("no slipId: " + slipId);
		}
		if( null == shop ) {
			throw new NoSuchElementException("no shopId: " + shopId);
		}		
		slip.shop(shop);
		slip.slipDttm(slipDttm);
		modified = true;
	}
	
	@Override
	public void updateDebit(int debitId, int debitCodeId, String description, int unitPrice, int quantity) {
		MemoryDebit debit =debitMap.get(debitId);
		MemoryDebitCode debitCode = debitCodeMap.get(debitCodeId);
		if( null == debit ) {
			throw new NoSuchElementException("no debitId: " + debitId);
		}
		if( null == debitCode ) {
			throw new NoSuchElementException("no debitCodeId: " + debitCodeId);
		}
		if( null == description || description.isEmpty() ) {
			description = debitCode.defaultDescription();
		}
		if( null == description ) {
			description = debitCode.name();
		}
		debit.debitCode(debitCode);
		debit.description(description);
		debit.unitPrice(unitPrice);
		debit.quantity(quantity);
		modified = true;
	}
	
	@Override
	public void updateCredit(int creditId, int creditCodeId, String description, int price) {
		MemoryCredit credit =creditMap.get(creditId);
		MemoryCreditCode creditCode = creditCodeMap.get(creditCodeId);
		if( null == credit ) {
			throw new NoSuchElementException("no creditId: " + creditId);
		}
		if( null == creditCode ) {
			throw new NoSuchElementException("no creditCodeId: " + creditCodeId);
		}
		if( null == description || description.isEmpty() ) {
			description = creditCode.defaultDescription();
		}
		if( null == description ) {
			description = creditCode.name();
		}
		credit.creditCode(creditCode);
		credit.description(description);
		credit.price(price);
		modified = true;
	}
	
	@Override
	public boolean deleteSlip(int slipId, boolean includeDetails) {
		MemorySlip slip =slipMap.get(slipId);
		if( null == slip ) {
			throw new NoSuchElementException("no slipId: " + slipId);
		}
		List<Debit> debitList = this.retrieveDebitList(a->a.slip().id()==slipId);
		List<Credit> creditList = this.retrieveCreditList(a->a.slip().id()==slipId);
		if( includeDetails || (debitList.isEmpty() && creditList.isEmpty()) ) {
			debitList.stream().forEach(a->debitMap.remove(a.id()));
			creditList.stream().forEach(a->creditMap.remove(a.id()));
			slipMap.remove(slip.id());
			modified = true;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void deleteDebit(int debitId) {
		MemoryDebit debit =debitMap.get(debitId);
		if( null == debit ) {
			throw new NoSuchElementException("no debitId: " + debitId);
		}
		debitMap.remove(debitId);
		modified = true;
	}
	
	@Override
	public void deleteCredit(int creditId) {
		MemoryCredit credit =creditMap.get(creditId);
		if( null == credit ) {
			throw new NoSuchElementException("no creditId: " + creditId);
		}
		creditMap.remove(creditId);
		modified = true;
	}
	
	@Override
	public String loadedDataName() {
		return dataName;
	}
	
	@Override
	public void load(Loader loader) throws Exception {
		assetMap = new TreeMap<>();
		shopMap = new TreeMap<>();
		debitCodeMap = new TreeMap<>();
		creditCodeMap = new TreeMap<>();
		slipMap = new TreeMap<>();
		debitMap = new TreeMap<>();
		creditMap = new TreeMap<>();
		dataName = null;
		modified = false;
		dataName = loader.load(this);		
	}
	@Override
	public void save(Dumper dumper) throws Exception {
		dumper.dump(this);
		modified = false;
	}
	@Override
	public boolean isModified() {
		return modified;
	}

	// definitions
	private boolean modified;
	private String dataName;
	private TreeMap<Integer, MemoryAsset> assetMap;
	private TreeMap<Integer, MemoryShop> shopMap;
	private TreeMap<Integer, MemoryDebitCode> debitCodeMap;
	private TreeMap<Integer, MemoryCreditCode> creditCodeMap;
	
	// data
	private TreeMap<Integer, MemorySlip> slipMap;
	private TreeMap<Integer, MemoryDebit> debitMap;
	private TreeMap<Integer, MemoryCredit> creditMap;
	
	public static abstract class Dumper {
		abstract protected void dump(HestiaMemoryDB db) throws Exception;		
		final protected Map<Integer, MemoryAsset> assetMap(HestiaMemoryDB db) {
			return Collections.unmodifiableMap(db.assetMap);
		}
		final protected Map<Integer, MemoryShop> shopMap(HestiaMemoryDB db) {
			return Collections.unmodifiableMap(db.shopMap);
		}
		final protected Map<Integer, MemoryDebitCode> debitCodeMap(HestiaMemoryDB db) {
			return Collections.unmodifiableMap(db.debitCodeMap);
		}
		final protected Map<Integer, MemoryCreditCode> creditCodeMap(HestiaMemoryDB db) {
			return Collections.unmodifiableMap(db.creditCodeMap);
		}
		final protected Map<Integer, MemorySlip> slipMap(HestiaMemoryDB db) {
			return Collections.unmodifiableMap(db.slipMap);
		}
		final protected Map<Integer, MemoryDebit> debitMap(HestiaMemoryDB db) {
			return Collections.unmodifiableMap(db.debitMap);
		}
		final protected Map<Integer, MemoryCredit> creditMap(HestiaMemoryDB db) {
			return Collections.unmodifiableMap(db.creditMap);
		}
	}
	public static abstract class Loader {
		abstract protected String load(HestiaMemoryDB db) throws Exception;
		
		final protected void addAsset(HestiaMemoryDB db, MemoryAsset asset) {
			if( db.assetMap.containsKey(asset.id()) ) {
				throw new IllegalArgumentException("duplicated asset:" + asset.id());
			}
			db.assetMap.put(asset.id(), asset);
		}
		final protected void addShop(HestiaMemoryDB db, MemoryShop shop) {
			if( db.shopMap.containsKey(shop.id()) ) {
				throw new IllegalArgumentException("duplicated shop:" + shop.id());
			}
			db.shopMap.put(shop.id(), shop);
		}
		final protected void addDebitCode(HestiaMemoryDB db, MemoryDebitCode debitCode) {
			if( db.debitCodeMap.containsKey(debitCode.id()) ) {
				throw new IllegalArgumentException("duplicated debitCode:" + debitCode.id());
			}
			if( null != debitCode.asset() && !db.assetMap.containsKey(debitCode.asset().id()) ) {
				throw new NoSuchElementException(String.format("no asset: debitCodeId=%d; assetId=%d", debitCode.id(), debitCode.asset().id()));
			}
			db.debitCodeMap.put(debitCode.id(), debitCode);
			
		}
		final protected void addCreditCode(HestiaMemoryDB db, MemoryCreditCode creditCode) {
			if( db.creditCodeMap.containsKey(creditCode.id()) ) {
				throw new IllegalArgumentException("duplicated creditCode:" + creditCode.id());
			}
			if( null != creditCode.asset() && !db.assetMap.containsKey(creditCode.asset().id()) ) {
				throw new NoSuchElementException(String.format("no asset: creditCodeId=%d; assetId=%d", creditCode.id(), creditCode.asset().id()));
			}
			db.creditCodeMap.put(creditCode.id(), creditCode);
		}
		final protected void addSlip(HestiaMemoryDB db, MemorySlip slip) {
			if( db.slipMap.containsKey(slip.id()) ) {
				throw new IllegalArgumentException("duplicated slip:" + slip.id());
			}
			if( !db.shopMap.containsKey(slip.shop().id()) ) {
				throw new NoSuchElementException(String.format("no shop: slipId=%d; shopId=%d", slip.id(), slip.shop().id()));
			}
			db.slipMap.put(slip.id(), slip);
		}
		final protected void addDebit(HestiaMemoryDB db, MemoryDebit debit) {
			if( db.debitMap.containsKey(debit.id()) ) {
				throw new IllegalArgumentException("duplicated debit:" + debit.id());
			}
			if( !db.slipMap.containsKey(debit.slip().id()) ) {
				throw new NoSuchElementException(String.format("no slip: debitId=%d; slipId=%d", debit.id(), debit.slip().id()));
			}
			if( !db.debitCodeMap.containsKey(debit.debitCode().id()) ) {
				throw new NoSuchElementException(String.format("no debitCode: debitId=%d; debitCodeId=%d", debit.id(), debit.debitCode().id()));
			}

			db.debitMap.put(debit.id(), debit);
		}
		final protected void addCredit(HestiaMemoryDB db, MemoryCredit credit) {
			if( db.creditMap.containsKey(credit.id()) ) {
				throw new IllegalArgumentException("duplicated asset:" + credit.id());
			}
			if( !db.slipMap.containsKey(credit.slip().id()) ) {
				throw new NoSuchElementException(String.format("no slip: creditId=%d; slipId=%d", credit.id(), credit.slip().id()));
			}
			if( !db.creditCodeMap.containsKey(credit.creditCode().id()) ) {
				throw new NoSuchElementException(String.format("no creditCode: creditId=%d; creditCodeId=%d", credit.id(), credit.creditCode().id()));
			}
			db.creditMap.put(credit.id(), credit);
		}
	}
}
