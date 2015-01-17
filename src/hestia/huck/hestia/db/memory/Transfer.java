package huck.hestia.db.memory;

import huck.common.jdbc.DBConnectionManager;
import huck.hestia.db.Credit;
import huck.hestia.db.Debit;
import huck.hestia.db.Slip;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Transfer {
	public static void main(String... args) throws Exception {
		Class.forName(org.gjt.mm.mysql.Driver.class.getName());
		String dbUrl = "jdbc:mysql://127.0.0.1:3306/account_book?characterEncoding=UTF-8";
		String dbUser = "root";
		String dbPassword = null;
		HestiaMemoryDB db = new HestiaMemoryDB(new LoaderMysql(new DBConnectionManager(dbUrl, dbUser, dbPassword)));
		List<Slip> slipList = db.retrieveSlipList(a->a.slipDttm().getYear()==2014);
		Set<Integer> slipIdSet = slipList.stream().collect(Collectors.mapping(a->a.id(), Collectors.toSet()));
		List<Debit> debitList = db.retrieveDebitList(a->slipIdSet.contains(a.slip().id()));
		List<Credit> creditList = db.retrieveCreditList(a->slipIdSet.contains(a.slip().id()));
		for( Debit debit : debitList ) {
			((MemoryDebit)debit).unitPrice(300000);
		}
		for( Credit credit : creditList ) {
			((MemoryCredit)credit).price(100000);
		}
		FileDataManager dataMgr = new FileDataManager(new File("test.data"));
		db.dump(dataMgr.getDumper());
	}
}
