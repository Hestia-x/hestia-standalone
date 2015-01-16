package huck.hestia.controller.accountbook;

import huck.hestia.HestiaController;
import huck.hestia.HttpUtil;
import huck.hestia.VelocityRenderer;
import huck.hestia.db.Asset;
import huck.hestia.db.HestiaDB;
import huck.hestia.history.AccountHistory;
import huck.hestia.history.AccountHistory.GroupType;
import huck.hestia.history.BalanceChangeGroup;
import huck.hestia.history.BalanceChanger;
import huck.hestia.history.HistoryGenerator;
import huck.simplehttp.HttpException;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HistoryController implements HestiaController {
	private VelocityRenderer renderer;
	private HestiaDB db;
	public HistoryController(HestiaDB db, VelocityRenderer renderer) throws IOException {
		this.db = db;
		this.renderer = renderer;
	}
	
	@Override
	public HttpResponse controll(HttpRequest req) throws HttpException, Exception {
		String path = req.getRequestPath();
		String[] paths = path.substring(1).split("\\/");
		for( int i=0; i<paths.length-1; i++ ) {
			paths[i] += "/"; 
		}
		if( path.endsWith("/") ) {
			paths[paths.length-1] += "/";
		}
		
		if( 3 > paths.length || paths[2] == "assets" ) {
			return renderer.render("/account_book/AssetBalanceHistory.html", req, assets(req));	
		}
		
		return null;
		
	}
	
	private HashMap<String, Object> assets(HttpRequest req) throws Exception {
		LocalDate fromDate = HttpUtil.getLocalDate(req, "from", false);
		LocalDate toDate = HttpUtil.getLocalDate(req, "to", false);
		LocalDate today = LocalDate.now();
		
		//  기준 되는 LocalDateTime 생성.
		if( null == toDate ) {
			toDate = today;
		}
		if( null == fromDate ) {
			LocalDate a = toDate.minusDays(5);
			LocalDate b = today.withDayOfMonth(1);
			if( a.isBefore(b) ) {
				fromDate = a;
			} else {
				fromDate = b;
			}
		}
		if( fromDate.isAfter(toDate) ) {
			LocalDate tmp = fromDate;
			fromDate = toDate;
			toDate = tmp;
		}
		
		ArrayList<BalanceChanger> allBalanceChangerList = new ArrayList<>();
		allBalanceChangerList.addAll(db.retrieveCreditList(null));
		allBalanceChangerList.addAll(db.retrieveDebitList(null));			
		AccountHistory history = HistoryGenerator.createAccountHistory(fromDate, toDate, GroupType.OCCURRENCE, allBalanceChangerList, Asset.class, a->true);
		HashMap<LocalDate, AtomicInteger> dateGroupCountMap = new HashMap<>();
		for( BalanceChangeGroup groupData : history.getBalanceChangeGroupList() ) {
			dateGroupCountMap.putIfAbsent(groupData.getDate(), new AtomicInteger(0));
			AtomicInteger cnt = dateGroupCountMap.get(groupData.getDate());
			cnt.addAndGet(groupData.getBalanceChangeList().size());
		}
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("history", history);
		resultMap.put("dateGroupCountMap", dateGroupCountMap);
		return resultMap;
	}
}

