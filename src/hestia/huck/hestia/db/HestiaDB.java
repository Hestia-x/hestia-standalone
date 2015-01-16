package huck.hestia.db;

import huck.hestia.db.data.Asset;
import huck.hestia.db.data.Credit;
import huck.hestia.db.data.CreditCode;
import huck.hestia.db.data.Debit;
import huck.hestia.db.data.DebitCode;
import huck.hestia.db.data.Shop;
import huck.hestia.db.data.Slip;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

public interface HestiaDB {
	public List<Asset> retrieveAssetList(Predicate<Asset> predicate);
	public List<Shop> retrieveShopList(Predicate<Shop> predicate);
	public List<DebitCode> retrieveDebitCodeList(Predicate<DebitCode> predicate);
	public List<CreditCode> retrieveCreditCodeList(Predicate<CreditCode> predicate);
	public List<Slip> retrieveSlipList(Predicate<Slip> predicate);
	public List<Debit> retrieveDebitList(Predicate<Debit> predicate);
	public List<Credit> retrieveCreditList(Predicate<Credit> predicate);
	
	public Slip insertSlip(LocalDateTime dttm, int shopId);
	public Debit insertDebit(int slipId, int debitCodeId, String description, int unitPrice, int quantity);
	public Credit insertCredit(int slipId, int creditCodeId, String description, int price);
}
