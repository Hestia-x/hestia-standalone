package huck.hestia.db;

import huck.hestia.db.data.Asset;
import huck.hestia.db.data.Credit;
import huck.hestia.db.data.CreditCode;
import huck.hestia.db.data.Debit;
import huck.hestia.db.data.DebitCode;
import huck.hestia.db.data.Shop;
import huck.hestia.db.data.Slip;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class HestiaMemoryDB implements HestiaDB {
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
		Shop shop = shopMap.get(shopId);
		if( null == shop ) {
			throw new NoSuchElementException("no shopId: " + shopId);
		}
		Slip slip = new Slip(slipMap.lastKey()+1, dttm, shop);
		slipMap.put(slip.id(), slip);		
		return slip;
	}
	@Override
	public Debit insertDebit(int slipId, int debitCodeId, String description, int unitPrice, int quantity) {
		Slip slip = slipMap.get(slipId);
		DebitCode debitCode = debitCodeMap.get(debitCodeId);
		if( null == slip ) {
			throw new NoSuchElementException("no slipId: " + slipId);
		}
		if( null == debitCode ) {
			throw new NoSuchElementException("no debitCodeId: " + debitCodeId);
		}
		Debit debit = new Debit(debitMap.lastKey()+1, slip, debitCode, description, unitPrice, quantity);
		debitMap.put(debit.id(), debit);
		return debit;
	}
	@Override
	public Credit insertCredit(int slipId, int creditCodeId, String description, int price) {
		Slip slip = slipMap.get(slipId);
		CreditCode creditCode = creditCodeMap.get(creditCodeId);
		if( null == slip ) {
			throw new NoSuchElementException("no slipId: " + slipId);
		}
		if( null == creditCode ) {
			throw new NoSuchElementException("no creditCodeId: " + creditCodeId);
		}
		Credit credit = new Credit(creditMap.lastKey()+1, slip, creditCode, description, price);
		creditMap.put(credit.id(), credit);
		return credit;
	}
	
	
	public void load(Loader loader) {
		assetMap = new TreeMap<>();
		shopMap = new TreeMap<>();
		debitCodeMap = new TreeMap<>();
		creditCodeMap = new TreeMap<>();
		slipMap = new TreeMap<>();
		debitMap = new TreeMap<>();
		creditMap = new TreeMap<>();
		loader.load();
	}
	
	// definitions
	private TreeMap<Integer, Asset> assetMap;
	private TreeMap<Integer, Shop> shopMap;
	private TreeMap<Integer, DebitCode> debitCodeMap;
	private TreeMap<Integer, CreditCode> creditCodeMap;
	
	// data
	private TreeMap<Integer, Slip> slipMap;
	private TreeMap<Integer, Debit> debitMap;
	private TreeMap<Integer, Credit> creditMap;
	
	private <T> List<T> retrieveListFromMap(TreeMap<Integer, T> map, Predicate<T> predicate, ToIntFunction<T> keyExtractor) {
		Supplier<List<T>> supplier = ArrayList::new;
		List<T> result;
		if( null == predicate ) {
			result = supplier.get();
			result.addAll(map.values());
		} else {
			result = map.values().stream().filter(predicate).collect(Collectors.toList());
			result.sort(Comparator.comparingInt(keyExtractor));
		}
		return Collections.unmodifiableList(result);
	}
	
	public abstract class Loader {
		abstract protected void load();
		
		protected final void addAsset(Asset asset) {
			if( assetMap.containsKey(asset.id()) ) {
				throw new IllegalArgumentException("duplicated asset:" + asset.id());
			}
			assetMap.put(asset.id(), asset);
		}
		protected final void addShop(Shop shop) {
			if( shopMap.containsKey(shop.id()) ) {
				throw new IllegalArgumentException("duplicated shop:" + shop.id());
			}
			shopMap.put(shop.id(), shop);
		}
		protected final void addDebitCode(DebitCode debitCode) {
			if( debitCodeMap.containsKey(debitCode.id()) ) {
				throw new IllegalArgumentException("duplicated debitCode:" + debitCode.id());
			}
			if( null != debitCode.asset() && !assetMap.containsKey(debitCode.asset().id()) ) {
				throw new NoSuchElementException(String.format("no asset: debitCodeId=%d; assetId=%d", debitCode.id(), debitCode.asset().id()));
			}
			debitCodeMap.put(debitCode.id(), debitCode);
			
		}
		protected final void addCreditCode(CreditCode creditCode) {
			if( creditCodeMap.containsKey(creditCode.id()) ) {
				throw new IllegalArgumentException("duplicated creditCode:" + creditCode.id());
			}
			if( null != creditCode.asset() && !assetMap.containsKey(creditCode.asset().id()) ) {
				throw new NoSuchElementException(String.format("no asset: creditCodeId=%d; assetId=%d", creditCode.id(), creditCode.asset().id()));
			}
			creditCodeMap.put(creditCode.id(), creditCode);
		}
		protected final void addSlip(Slip slip) {
			if( slipMap.containsKey(slip.id()) ) {
				throw new IllegalArgumentException("duplicated slip:" + slip.id());
			}
			if( !shopMap.containsKey(slip.shop().id()) ) {
				throw new NoSuchElementException(String.format("no shop: slipId=%d; shopId=%d", slip.id(), slip.shop().id()));
			}
			slipMap.put(slip.id(), slip);
		}
		protected final void addDebit(Debit debit) {
			if( debitMap.containsKey(debit.id()) ) {
				throw new IllegalArgumentException("duplicated debit:" + debit.id());
			}
			if( !slipMap.containsKey(debit.slip().id()) ) {
				throw new NoSuchElementException(String.format("no slip: debitId=%d; slipId=%d", debit.id(), debit.slip().id()));
			}
			if( !debitCodeMap.containsKey(debit.debitCode().id()) ) {
				throw new NoSuchElementException(String.format("no debitCode: debitId=%d; debitCodeId=%d", debit.id(), debit.debitCode().id()));
			}

			debitMap.put(debit.id(), debit);
		}
		protected final void addCredit(Credit credit) {
			if( creditMap.containsKey(credit.id()) ) {
				throw new IllegalArgumentException("duplicated asset:" + credit.id());
			}
			if( !slipMap.containsKey(credit.slip().id()) ) {
				throw new NoSuchElementException(String.format("no slip: creditId=%d; slipId=%d", credit.id(), credit.slip().id()));
			}
			if( !creditCodeMap.containsKey(credit.creditCode().id()) ) {
				throw new NoSuchElementException(String.format("no creditCode: creditId=%d; creditCodeId=%d", credit.id(), credit.creditCode().id()));
			}
			creditMap.put(credit.id(), credit);
		}
	}
}
