package huck.hestia.db.memory;

import huck.common.jdbc.DBConnectionManager;
import huck.hestia.db.Credit;
import huck.hestia.db.Debit;

import java.io.File;
import java.util.List;

public class Transfer {
	public static void main(String... args) throws Exception {
		Class.forName(org.gjt.mm.mysql.Driver.class.getName());
		String dbUrl = "jdbc:mysql://127.0.0.1:3306/account_book?characterEncoding=UTF-8";
		String dbUser = "root";
		String dbPassword = null;
		
		HestiaMemoryDB db = new HestiaMemoryDB();
		db.load(new LoaderMysql(new DBConnectionManager(dbUrl, dbUser, dbPassword)));
		
		List<Debit> debitList = db.retrieveDebitList(null);
		for( Debit debit : debitList ) {
			switch(debit.slip().id()) {
			case 1:
			case 78:
				((MemoryDebit)debit).unitPrice(500000);
			}
		}
		List<Credit> creditList = db.retrieveCreditList(null);
		for( Credit credit : creditList ) {
			switch(credit.slip().id()) {
			case 1:
			case 78:
				((MemoryCredit)credit).price(500000);
			}
		}		
		db.save(FileDataManager.getDumper(new File("test.data")));
	}
}
