package huck.hestia.controller.accountbook;

import huck.hestia.HestiaController;
import huck.hestia.HttpUtil;
import huck.hestia.RequestPath;
import huck.hestia.VelocityRenderer;
import huck.hestia.VelocityRenderer.ActionFunction;
import huck.hestia.db.Credit;
import huck.hestia.db.Debit;
import huck.hestia.db.HestiaDB;
import huck.hestia.db.Slip;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SlipController implements HestiaController {
	private HestiaDB<?,?> db;
	private VelocityRenderer renderer;
	
	public SlipController(HestiaDB<?,?> db, VelocityRenderer renderer) throws IOException {
		this.db = db;
		this.renderer = renderer;
	}
	
	@Override
	public HttpResponse controll(HttpRequest req, String matchPath) throws Exception {
		String loadedDataName = db.loadedDataName();
		if( null == loadedDataName ) {
			return redirectTo("/system/load/");
		} else {
			req.setAttribute("loadedDataName", loadedDataName);
		}
		
		RequestPath path = new RequestPath(req.getRequestPath().substring(matchPath.length()));
		req.setAttribute("path", path);
		ActionFunction actionFunction;
		switch( path.size() ) {
		case 0: actionFunction = this::slipMain; break;
		case 1: actionFunction = this::slipDetail; break;
		default: actionFunction = null;
		}
		if( 2 == path.size() ) {
			return slipAction(req, path);
		}
		if( null == actionFunction ) {
			notFound(req);
		}
		return renderer.render(req, actionFunction);
	}
	
	private String slipMain(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
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
			fromDate = a.isBefore(b) ? a : b;
		}
		if( fromDate.isAfter(toDate) ) {
			LocalDate tmp = fromDate;
			fromDate = toDate;
			toDate = tmp;
		}
		LocalDateTime fromDttm = fromDate.atStartOfDay().minusNanos(1);
		LocalDateTime toDttm = toDate.plusDays(1).atStartOfDay();
		
		List<Slip> dbSlipList = db.retrieveSlipList(a -> a.slipDttm().isBefore(toDttm)&&a.slipDttm().isAfter(fromDttm));
		Set<Integer> slipIdSet = dbSlipList.stream().collect(Collectors.mapping(a->a.id(), Collectors.toSet()));
		List<Debit> debitList = db.retrieveDebitList(a->slipIdSet.contains(a.slip().id()));
		List<Credit> creditList = db.retrieveCreditList(a->slipIdSet.contains(a.slip().id()));
		
		ArrayList<Slip> slipList = new ArrayList<>(dbSlipList);
		slipList.sort(Comparator.comparingInt((Slip a)->a.id()).reversed());
		
		HashMap<Slip, HashMap<String, Integer>> result = new HashMap<>();
		for( Slip slip : slipList ) {
			HashMap<String, Integer> data = new HashMap<>();
			data.put("debit", 0);
			data.put("credit", 0);
			result.put(slip, data);
		}
		for( Debit debit : debitList ) {
			HashMap<String, Integer> data = result.get(debit.slip());
			data.put("debit", data.get("debit")+debit.quantity()*debit.unitPrice());			
		}
		for( Credit credit : creditList ) {
			HashMap<String, Integer> data = result.get(credit.slip());
			data.put("credit", data.get("credit")+credit.price());			
		}
		
		valueMap.put("from", fromDate);
		valueMap.put("to", toDate);
		valueMap.put("slipList", slipList);
		valueMap.put("result", result);
		return "/account_book/slip_list.html";
	}
	private String slipDetail(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		RequestPath path = (RequestPath)req.getAttribute("path");
		Integer slipId = path.getInt(0, Integer.MIN_VALUE);
		List<Slip> slipList = db.retrieveSlipList(a->a.id()==slipId);
		if( slipList.isEmpty() ) {
			notFound(req);
		}
		List<Debit> debitList = db.retrieveDebitList(a->a.slip().id()==slipId);
		List<Credit> creditList = db.retrieveCreditList(a->a.slip().id()==slipId);
		IntSummaryStatistics debitSummary = debitList.stream().collect(Collectors.summarizingInt(a->a.unitPrice()*a.quantity()));
		IntSummaryStatistics creditSummary = creditList.stream().collect(Collectors.summarizingInt(a->a.price()));
		valueMap.put("slip", slipList.get(0));
		valueMap.put("debitList", debitList);
		valueMap.put("creditList", creditList);
		valueMap.put("debitSummary", debitSummary);
		valueMap.put("creditSummary", creditSummary);
		return "/account_book/slip_detail.html";
	}
	
	private HttpResponse slipAction(HttpRequest req, RequestPath path) throws Exception {
		Integer slipId = path.getInt(0, Integer.MIN_VALUE);
		List<Slip> slipList = db.retrieveSlipList(a->a.id()==slipId);
		if( slipList.isEmpty() ) {
			notFound(req);
		}
		String action = path.get(1);
		switch( action ) {
		case "delete":
			db.deleteSlip(slipId, true);
			return this.redirectTo("/account_book/slip/");
		case "edit":
			return this.redirectTo("/account_book/slipform/"+slipId);
		default:
			notFound(req);
			return null;
		}
	}
}