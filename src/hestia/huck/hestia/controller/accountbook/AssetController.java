package huck.hestia.controller.accountbook;

import huck.hestia.HestiaController;
import huck.hestia.HttpUtil;
import huck.hestia.RequestPath;
import huck.hestia.VelocityRenderer;
import huck.hestia.VelocityRenderer.ActionFunction;
import huck.hestia.db.Asset;
import huck.hestia.db.HestiaDB;
import huck.hestia.history.AccountChanger;
import huck.hestia.history.AccountHistory;
import huck.hestia.history.AccountHistory.GroupType;
import huck.hestia.history.BalanceChangeGroup;
import huck.hestia.history.HistoryGenerator;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class AssetController implements HestiaController {
	private HestiaDB db;
	private VelocityRenderer renderer;
	
	public AssetController(HestiaDB db, VelocityRenderer renderer) throws IOException {
		this.db = db;
		this.renderer = renderer;
	}
	
	@Override
	public HttpResponse controll(HttpRequest req, String matchPath) throws Exception {
		RequestPath path = new RequestPath(req.getRequestPath().substring(matchPath.length()));
		req.setAttribute("path", path);

		ActionFunction actionFunction;
		switch( path.get(0) ) {
		case "": actionFunction = this::assetMain; break;
		default: actionFunction =  this::assetDetail; break;
		}
		return renderer.render(req, actionFunction);
	}
	
	private String assetMain(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		return assetHistory(req, valueMap, a->true);
	}
	private String assetDetail(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		RequestPath path = (RequestPath)req.getAttribute("path");
		int assetId = path.getInt(1, Integer.MIN_VALUE);
		if( !db.retrieveAssetList(a->a.id()==assetId).isEmpty() ) {
			return assetHistory(req, valueMap, a->a.id()==assetId);	
		}
		notFound(req);
		return null;
	}
	
	private String assetHistory(HttpRequest req, HashMap<String, Object> valueMap, Predicate<Asset> assetPredicate) throws Exception {
		LocalDate fromDate = HttpUtil.getLocalDate(req, "from", false);
		LocalDate toDate = HttpUtil.getLocalDate(req, "to", false);
		LocalDate today = LocalDate.now();
		String groupTypeStr = HttpUtil.getString(req, "group", false);
		GroupType groupType = GroupType.OCCURRENCE;
		try {
			groupType = GroupType.valueOf(groupTypeStr);
		} catch( Exception ignore ) {
		}
		
		//  기준 되는 LocalDateTime 생성.
		if( null == toDate ) {
			toDate = today;
		}
		if( null == fromDate ) {
			if( groupType == GroupType.MONTH ) {
				LocalDate a = toDate.minusMonths(3);
				LocalDate b = today.withDayOfYear(1);
				fromDate = a.isBefore(b) ? a : b;
			} else {
				LocalDate a = toDate.minusDays(5);
				LocalDate b = today.withDayOfMonth(1);
				fromDate = a.isBefore(b) ? a : b;
			}
		}
		if( fromDate.isAfter(toDate) ) {
			LocalDate tmp = fromDate;
			fromDate = toDate;
			toDate = tmp;
		}
		
		ArrayList<AccountChanger> allAccountChanger = new ArrayList<>();
		allAccountChanger.addAll(db.retrieveCreditList(null));
		allAccountChanger.addAll(db.retrieveDebitList(null));			
		AccountHistory history = HistoryGenerator.createAccountHistory(fromDate, toDate, groupType, allAccountChanger, Asset.class, assetPredicate);
		HashMap<LocalDate, AtomicInteger> dateGroupCountMap = new HashMap<>();
		for( BalanceChangeGroup groupData : history.getBalanceChangeGroupList() ) {
			dateGroupCountMap.putIfAbsent(groupData.getDate(), new AtomicInteger(0));
			AtomicInteger cnt = dateGroupCountMap.get(groupData.getDate());
			cnt.addAndGet(groupData.getBalanceChangeList().size());
		}
		
		valueMap.put("history", history);
		valueMap.put("dateGroupCountMap", dateGroupCountMap);
		return "/account_book/asset_history.html";
	}
}

