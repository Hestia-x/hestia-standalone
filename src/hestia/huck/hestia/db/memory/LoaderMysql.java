package huck.hestia.db.memory;

import huck.common.jdbc.DAOHelper;
import huck.common.jdbc.DAOResultSet;
import huck.common.jdbc.DBConnectionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class LoaderMysql extends HestiaMemoryDB.Loader {
	private DBConnectionManager dbConnManager;
	public LoaderMysql(DBConnectionManager dbConnManager) {
		this.dbConnManager = dbConnManager;
	}

	@Override
	protected String load(HestiaMemoryDB db) throws SQLException {
		try( DAOHelper dao = new DAOHelper(dbConnManager, false) ) {
			DAOResultSet rs;
	
			// assets
			Logger.getLogger("hestia").info("[DB] Start loading from Mysql");
			TreeMap<Integer, MemoryAsset> assetMap = new TreeMap<>();
			rs = dao.executeQuery("SELECT `id`, `name`, `description` FROM assets");
			while( rs.next() ) {
				int id = rs.getInt("id");
				String name =rs.getString("name");
				String description = rs.getString("description");
				MemoryAsset asset = new MemoryAsset(id, name, description);
				this.addAsset(db, asset);
				assetMap.put(id, asset);
			}
			Logger.getLogger("hestia").info("[DB] assets loaded");
	
			// shops
			TreeMap<Integer, MemoryShop> shopMap = new TreeMap<>();
			rs = dao.executeQuery("SELECT `id`, `name` FROM shops");
			while( rs.next() ) {
				int id = rs.getInt("id");
				String name =rs.getString("name");
				MemoryShop shop = new MemoryShop(id, name);
				this.addShop(db, shop);
				shopMap.put(id, shop);
			}
			Logger.getLogger("hestia").info("[DB] shops loaded");
			
			// debit_codes
			TreeMap<Integer, MemoryDebitCode> debitCodeMap = new TreeMap<>();
			rs = dao.executeQuery("SELECT `id`, `name`, `asset_id`, `default_description` FROM debit_codes");
			while( rs.next() ) {
				int id = rs.getInt("id");
				String name =rs.getString("name");
				Integer assetId = rs.getInt("asset_id");
				String defaultDescription =rs.getString("default_description");
				MemoryAsset asset = null;
				if( null != assetId ) {
					asset = assetMap.get(assetId);
				}
				MemoryDebitCode debitCode = new MemoryDebitCode(id, name, asset, defaultDescription);
				this.addDebitCode(db, debitCode);
				debitCodeMap.put(id, debitCode);
			}
			Logger.getLogger("hestia").info("[DB] debit_codes loaded");
	
			// credit_codes
			TreeMap<Integer, MemoryCreditCode> creditCodeMap = new TreeMap<>();
			rs = dao.executeQuery("SELECT `id`, `name`, `asset_id`, `default_description` FROM credit_codes");
			while( rs.next() ) {
				int id = rs.getInt("id");
				String name =rs.getString("name");
				Integer assetId = rs.getInt("asset_id");
				String defaultDescription =rs.getString("default_description");
				MemoryAsset asset = null;
				if( null != assetId ) {
					asset = assetMap.get(assetId);
				}
				MemoryCreditCode creditCode = new MemoryCreditCode(id, name, asset, defaultDescription);
				this.addCreditCode(db, creditCode);
				creditCodeMap.put(id, creditCode);
			}
			Logger.getLogger("hestia").info("[DB] credit_codes loaded");
	
			// slips
			TreeMap<Integer, MemorySlip> slipMap = new TreeMap<>();
			rs = dao.executeQuery("SELECT `id`, DATE_FORMAT(slip_dttm, '%Y-%m-%dT%H:%i:%s') AS slip_dttm, shop_id FROM slips");
			while( rs.next() ) {
				int id = rs.getInt("id");
				String slipDttmStr = rs.getString("slip_dttm");
				int shopId = rs.getInt("shop_id");
				MemoryShop shop = shopMap.get(shopId);
				
				MemorySlip slip = new MemorySlip(id, LocalDateTime.parse(slipDttmStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME), shop);
				this.addSlip(db, slip);
				slipMap.put(id, slip);
			}
			Logger.getLogger("hestia").info("[DB] slips loaded");
	
			// debits
			rs = dao.executeQuery("SELECT `id`, `slip_id`, `debit_code_id`, `description`, `unit_price`, `quantity` FROM debits");
			while( rs.next() ) {
				int id = rs.getInt("id");
				int slipId = rs.getInt("slip_id");
				int debitCodeId = rs.getInt("debit_code_id");
				String description =rs.getString("description");
				int unitPrice = rs.getInt("unit_price");
				int quantity = rs.getInt("quantity");
				
				MemorySlip slip = slipMap.get(slipId);
				MemoryDebitCode debitCode = debitCodeMap.get(debitCodeId);
				MemoryDebit debit = new MemoryDebit(id, slip, debitCode, description, unitPrice, quantity);
				this.addDebit(db, debit);
			}
			Logger.getLogger("hestia").info("[DB] debits loaded");

			// credits
			rs = dao.executeQuery("SELECT `id`, `slip_id`, `credit_code_id`, `description`, `price` FROM credits");
			while( rs.next() ) {
				int id = rs.getInt("id");
				int slipId = rs.getInt("slip_id");
				int creditCodeId = rs.getInt("credit_code_id");
				String description =rs.getString("description");
				int price = rs.getInt("price");
				
				MemorySlip slip = slipMap.get(slipId);
				MemoryCreditCode creditCode = creditCodeMap.get(creditCodeId);
				MemoryCredit credit = new MemoryCredit(id, slip, creditCode, description, price);
				this.addCredit(db, credit);
			}
			Logger.getLogger("hestia").info("[DB] credits loaded");
			
			return "mysql";
		}
	}
}
