package huck.hestia.db;


import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

public interface HestiaDB<LOAD_OPTION, SAVE_OPTION> {
	public String loadedDataName();
	public void load(LOAD_OPTION option) throws Exception;	
	public void save(SAVE_OPTION option) throws Exception;
	public boolean isModified();
	
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
	
	public void updateSlip(int slipId, int shopId, LocalDateTime slipDttm);
	public void updateDebit(int debitId, int debitCodeId, String description, int unitPrice, int quantity);
	public void updateCredit(int creditId, int creditCodeId, String description, int price);
	
	public boolean deleteSlip(int slipId, boolean includeDetails);
	public void deleteDebit(int debitId);
	public void deleteCredit(int creditId);
}
