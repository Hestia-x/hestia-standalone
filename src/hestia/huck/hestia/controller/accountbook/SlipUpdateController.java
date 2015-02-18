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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
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

		int slipId = -1;
		JSONObject originData = null;
		switch( path.size() ) {
		case 0:
			break;
		case 1:
			try {
				slipId = Integer.parseInt(path.get(0));
			} catch( NumberFormatException ex ) {
				notFound(req);
				return null;	
			}
			int tmpSlipId = slipId;
			List<Slip> slipList = db.retrieveSlipList(a->a.id()==tmpSlipId);
			if( slipList.isEmpty() ) {
				notFound(req);
				return null;
			}
			List<Debit> debitList = db.retrieveDebitList(a->a.slip().id()==tmpSlipId);
			List<Credit> creditList = db.retrieveCreditList(a->a.slip().id()==tmpSlipId);
			originData = createFormDataFromSlip(slipList.get(0), debitList, creditList);
			break;
		default:
			notFound(req);
			return null;
		}

		String originDataString = "null";
		String editingDataString = "null";
		String errorDataString = "null";
		if( null != originData ) {
			originDataString = originData.toString();
		}		
		if( "post".equals(req.getMethod().toLowerCase())) {
			SaveResult saveResult = processSave(req, originData);
			if( null == saveResult.errorData ) {
				return redirectTo("/account_book/slip/"+saveResult.slipId);
			}
			if( null != saveResult.editingData ) {
				editingDataString = saveResult.editingData.toString();
			}
			if( null != saveResult.errorData ) {
				errorDataString = saveResult.errorData.toString();
			}
		}

		HashMap<String, Object> valueMap = new HashMap<String, Object>();
		valueMap.put("shopList", db.retrieveShopList(null));
		valueMap.put("debitCodeList", db.retrieveDebitCodeList(null));
		valueMap.put("creditCodeList", db.retrieveCreditCodeList(null));
		valueMap.put("originData", originDataString);
		valueMap.put("editingData", editingDataString);
		valueMap.put("errorData", errorDataString);
		valueMap.put("slipId", slipId);
		return renderer.render("/account_book/slip_form.html", req, valueMap);
	}
	
	private JSONObject createFormDataFromSlip(Slip slip, List<Debit> debitList, List<Credit> creditList) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<>();
		
		LinkedHashMap<String, Object> slipData = new LinkedHashMap<>();
		slipData.put("id", slip.id());
		slipData.put("shop_id", slip.shop().id());
		slipData.put("datetime", slip.slipDttm().format(DateTimeFormatter.ofPattern("uuuu-MM-dd kk:mm:ss")));
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

	private static class SaveResult {
		int slipId;
		JSONObject editingData;
		JSONObject errorData;
	}
	private static class SaveException extends Exception {
		private static final long serialVersionUID = -1849038092383472407L;
		private String errorForm;
		private String msg;
		public SaveException(String errorForm, String msg) {
			super(msg);
			this.errorForm = errorForm;
			this.msg = msg;
		}
		public SaveException(String errorForm, String msg, Exception cause) {
			super(msg, cause);
			this.errorForm = errorForm;
			this.msg = msg;
		}
	}
	
	private SaveResult processSave(HttpRequest req, JSONObject originData) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, String> param = (Map<String, String>)req.getAttribute("bodyParam");
		if( null == param ) {
			throw new Exception("only POST allowed");
		}

		String editingDataString = param.get("editing_data");
		System.out.println(editingDataString);
		Slip slip = null;
		JSONObject editingData = new JSONObject(editingDataString);
		JSONObject errorData = null;
		try {
			checkEditingData(originData, editingData);
			JSONObject slipData = editingData.getJSONObject("slip");
			JSONArray debitList = editingData.getJSONArray("debit");
			JSONArray creditList = editingData.getJSONArray("credit");
			int slip_id = slipData.getInt("id");
			int slip_shopId = slipData.getInt("shop_id");
			LocalDateTime slip_datetime = (LocalDateTime)slipData.get("datetime_obj");
			
			if( 0 < slip_id ) {
				db.updateSlip(slip_id, slip_shopId, slip_datetime);
				slip = db.retrieveSlipList(a->a.id() == slip_id).get(0);
			} else {
				slip = db.insertSlip(slip_datetime, slip_shopId);
			}			
			for( int i=0; i<debitList.length(); i++ ) {
				JSONObject debitData = debitList.getJSONObject(i);
				int id = debitData.getInt("id");
				int code = debitData.getInt("code");
				String description = debitData.getString("description");
				int price = debitData.getInt("price");
				int quantity = debitData.getInt("quantity");
				if( 0 >= id ) {
					db.insertDebit(slip.id(), code, description, price, quantity);
				} else if( 0 < code ) {
					db.updateDebit(id, code, description, price, quantity);
				} else {
					db.deleteDebit(id);
				}
			}
			for( int i=0; i<creditList.length(); i++ ) {
				JSONObject creditData = creditList.getJSONObject(i);
				int id = creditData.getInt("id");
				int code = creditData.getInt("code");
				String description = creditData.getString("description");
				int price = creditData.getInt("price");
				if( 0 >= id ) {
					db.insertCredit(slip.id(), code, description, price);
				} else if( 0 < code ) {
					db.updateCredit(id, code, description, price);
				} else {
					db.deleteCredit(id);
				}
			}
		} catch( SaveException ex ) {
			errorData = new JSONObject();
			errorData.put("form", ex.errorForm);
			errorData.put("msg", ex.msg);
			logger().error(ex, ex);
		}
		SaveResult saveResult = new SaveResult();
		saveResult.slipId = null==slip?-1:slip.id();
		saveResult.editingData = editingData;
		saveResult.errorData = errorData;
		return saveResult;
	}
	
	private void checkEditingData(JSONObject originData, JSONObject editingData) throws SaveException, JSONException {
		// TODO compare originData and editingData
		JSONObject slipData = editingData.getJSONObject("slip");
		JSONArray debitList = editingData.getJSONArray("debit");
		JSONArray creditList = editingData.getJSONArray("credit");
		if( 0 == debitList.length() ) {
			throw new SaveException(null, "need one or more debit");
		}
		if( 0 == creditList.length() ) {
			throw new SaveException(null, "need one or more credit");
		}
		HashSet<Integer> orgDebitIdSet = new HashSet<>();
		HashSet<Integer> orgCreditIdSet = new HashSet<>();
		if( null != originData ) {
			JSONObject orgSlipData = originData.getJSONObject("slip");
			JSONArray orgDebitList = originData.getJSONArray("debit");
			JSONArray orgCreditList = originData.getJSONArray("credit");
			
			if( slipData.getInt("id") != orgSlipData.getInt("slip") ) {
				throw new SaveException(null, "different slip id");
			}
			for( int i=0; i<orgDebitList.length(); i++ ) {
				orgDebitIdSet.add(orgDebitList.getJSONObject(i).getInt("id"));
			}
			for( int i=0; i<orgCreditList.length(); i++ ) {
				orgCreditIdSet.add(orgCreditList.getJSONObject(i).getInt("id"));
			}
		}
		
		int debitSum = 0;
		int creditSum = 0;
		
		int slip_id = slipData.getInt("id");
		int slip_shopId = slipData.getInt("shop_id");
		String slip_datetimeStr = slipData.getString("datetime");
		
		LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		LocalDateTime slip_datetime = now;
		if( 0 <= slip_id && db.retrieveSlipList(a->a.id() == slip_id).isEmpty() ) {
			throw new SaveException(null, "unknown slip data");
		}
		if( null != slip_datetimeStr && !slip_datetimeStr.trim().isEmpty()) {
			try {
				slip_datetime = LocalDateTime.parse(slip_datetimeStr, DateTimeFormatter.ofPattern("uuuu-MM-dd kk:mm:ss"));
			} catch( DateTimeParseException ex ) {
				throw new SaveException("slip.datetime", "unknown datetime format", ex);
			}
			if( slip_datetime.isAfter(now) ) {
				throw new SaveException("slip.datetime", "can not put future datetime.");
			}
		}
		slipData.put("datetime", slip_datetime.format(DateTimeFormatter.ofPattern("uuuu-MM-dd kk:mm:ss")));
		slipData.put("datetime_obj", slip_datetime);
		if( db.retrieveShopList(a->a.id() == slip_shopId).isEmpty() ) {
			throw new SaveException("slip.shop_id", "unknown code");
		}
		
		HashSet<Integer> debitIdSet = new HashSet<>();
		HashSet<Integer> creditIdSet = new HashSet<>();
		for( int i=0; i<debitList.length(); i++ ) {
			JSONObject debitData = debitList.getJSONObject(i);
			int id = debitData.getInt("id");
			int code = debitData.getInt("code");
			String description = debitData.getString("description");
			int price = debitData.getInt("price");
			int quantity = debitData.getInt("quantity");
			if( 0 < id ) {
				if( 0 < slip_id ) {
					if( !orgDebitIdSet.contains(id) ) {
						throw new SaveException("debit"+i+".description", "need description");
					} else {
						debitIdSet.add(id);
					}
				} else {
					throw new SaveException(null, "unexpected debit_id: " + id);	
				}
			}
			if( 0 >= id || 0 < code ) {
				if( db.retrieveDebitCodeList(a->a.id() == code).isEmpty() ) {
					throw new SaveException("debit"+i+".code_name", "unknown code");
				}
				if( null == description || description.trim().isEmpty() ) {
					throw new SaveException("credit"+i+".description", "need description");
				}
				debitSum += (price * quantity);
			}
			if( 0 > price ) {
				throw new SaveException("debit"+i+".price_str", "need positive price");
			}
			if( 0 > quantity ) {
				throw new SaveException("debit"+i+".quantity", "need positive quantity");
			}
		}
		for( int i=0; i<creditList.length(); i++ ) {
			JSONObject creditData = creditList.getJSONObject(i);
			int id = creditData.getInt("id");
			int code = creditData.getInt("code");
			String description = creditData.getString("description");
			int price = creditData.getInt("price");
			if( 0 > slip_id && 0 <= id ) {
				throw new SaveException(null, "unexpected credit_id: " + id);
			}
			if( 0 < id ) {
				if( 0 < slip_id ) {
					if( !orgCreditIdSet.contains(id) ) {
						throw new SaveException("debit"+i+".description", "need description");
					} else {
						creditIdSet.add(id);
					}
				} else {
					throw new SaveException(null, "unexpected debit_id: " + id);	
				}
			}
			if( 0 >= id || 0 < code ) {
				if( db.retrieveDebitCodeList(a->a.id() == code).isEmpty() ) {
					throw new SaveException("credit"+i+".code_name", "unknown code");
				}
				if( null == description || description.trim().isEmpty() ) {
					throw new SaveException("credit"+i+".description", "need description");
				}
				creditSum += price;
			}
			if( 0 > price ) {
				throw new SaveException("credit"+i+".price_str", "need positive price");
			}
		}
		if( !debitIdSet.containsAll(orgDebitIdSet) ) {
			throw new SaveException(null, "invalid form data");
		}
		if( !creditIdSet.containsAll(orgCreditIdSet) ) {
			throw new SaveException(null, "invalid form data");
		}
		if( debitSum != creditSum ) {
			throw new SaveException(null, "unbalanced slip");
		}
	}
}

