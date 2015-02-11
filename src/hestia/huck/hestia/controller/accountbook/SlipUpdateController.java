package huck.hestia.controller.accountbook;

import huck.hestia.HestiaController;
import huck.hestia.RequestPath;
import huck.hestia.VelocityRenderer;
import huck.hestia.db.Credit;
import huck.hestia.db.Debit;
import huck.hestia.db.HestiaDB;
import huck.hestia.db.Slip;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class SlipUpdateController implements HestiaController {
	private HestiaDB<?,?> db;
	private VelocityRenderer renderer;
	
	public SlipUpdateController(HestiaDB<?,?> db, VelocityRenderer renderer) throws IOException {
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
		
		JSONObject originData = null;
		JSONObject editingData = null;
		JSONObject errorData = null;
		
		switch( path.size() ) {
		case 0:
			break;
		case 1:
			int slipId;
			try {
				slipId = Integer.parseInt(path.get(0));
			} catch( NumberFormatException ex ) {
				notFound(req);
				return null;	
			}
			List<Slip> slipList = db.retrieveSlipList(a->a.id()==slipId);
			if( slipList.isEmpty() ) {
				notFound(req);
				return null;
			}
			List<Debit> debitList = db.retrieveDebitList(a->a.slip().id()==slipId);
			List<Credit> creditList = db.retrieveCreditList(a->a.slip().id()==slipId);
			originData = createFormDataFromSlip(slipList.get(0), debitList, creditList);
			break;
		default:
			notFound(req);
			return null;
		}

		if( "post".equals(req.getMethod().toLowerCase())) {
			editingData = new JSONObject();
			errorData = new JSONObject();
			Slip slip = processSave(req, originData, editingData, errorData);
			if( null != slip ) {
				return redirectTo("/account_book/slip/"+slip.id());
			}
			
			if( 0 == errorData.length() ) {
				errorData = null;
			}
			if( 0 == editingData.length() ) {
				editingData = null;
			}
		}
		String originDataString = "null";
		if( null != originData ) {
			originDataString = originData.toString();
		}
		HashMap<String, Object> valueMap = new HashMap<String, Object>();
		valueMap.put("shopList", db.retrieveShopList(null));
		valueMap.put("debitCodeList", db.retrieveDebitCodeList(null));
		valueMap.put("creditCodeList", db.retrieveCreditCodeList(null));
		valueMap.put("originData", originDataString);
		valueMap.put("editingData", editingData);
		valueMap.put("errorData", errorData);
		return renderer.render("/account_book/slip_form.html", req, valueMap);
	}
	
	private JSONObject createFormDataFromSlip(Slip slip, List<Debit> debitList, List<Credit> creditList) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<>();
		
		LinkedHashMap<String, Object> slipData = new LinkedHashMap<>();
		slipData.put("id", slip.id());
		slipData.put("shop_id", slip.shop().id());
		slipData.put("datetime", slip.slipDttm());
		result.put("slip", slipData);
		
		ArrayList<Object> debitDataList = new ArrayList<>();
		for( Debit debit : debitList ) {
			LinkedHashMap<String, Object> debitData = new LinkedHashMap<>();
			debitData.put("id", debit.id());
			debitData.put("code", debit.debitCode().id());
			debitData.put("description", debit.description());
			debitData.put("price", debit.unitPrice());
			debitData.put("quantity", debit.quantity());
			debitDataList.add(debitData);
		}
		result.put("debit", debitDataList);
		
		ArrayList<Object> creditDataList = new ArrayList<>();
		for( Credit credit : creditList ) {
			LinkedHashMap<String, Object> creditData = new LinkedHashMap<>();
			creditData.put("id", credit.id());
			creditData.put("code", credit.creditCode().id());
			creditData.put("description", credit.description());
			creditData.put("price", credit.price());
			creditDataList.add(creditData);
		}
		result.put("credit", creditDataList);
		return new JSONObject(result);
	}

	private Slip processSave(HttpRequest req, JSONObject originData, JSONObject editingData, JSONObject errorData) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, String> param = (Map<String, String>)req.getAttribute("bodyParam");
		if( null == param ) {
			throw new Exception("aa");
		}
		System.out.println(param.get("test"));
		return null;
	}
}

