package modoo.ext.module.payment.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import modoo.ext.module.payment.dao.ModooPayDAO;
import modoo.ext.module.payment.exception.PayException;
import modoo.ext.module.payment.service.RegularPayService;
import modoo.ext.module.payment.util.EncryptUtil;
import modoo.ext.module.payment.util.JsonUtil;
import modoo.ext.module.payment.util.NumberUtil;
import modoo.ext.module.payment.util.TextUtil;
import modoo.ext.module.payment.vo.ModooPayVO;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;

@Service
public class RegularPayServiceImpl implements RegularPayService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RegularPayServiceImpl.class);
	
	@Autowired
	private EncryptUtil encryptUtil;
	
	@Autowired
	private ModooPayDAO payDao;
		
	@Value("${payment.fail.cnt.max}")
	private Integer paymentFailCntMax;

	@Value("${payment.ezwel.url}")
	private String ezwelUrl;	

	@Value("${payment.ezwel.cspCd}")
	private String ezwelCspCd;

	@Value("${payment.exanadu.url}")
	private String exanaduUrl;

	@Value("${payment.exanadu.vendor}")
	private String exanaduVendor;

	@Value("${payment.exanadu.svid}")
	private String exanaduSvid;

	@Value("${payment.inicis.url}")
	private String inicisUrl;
	
	@Value("${payment.inicis.cancel.url}")
	private String inicisCancelUrl;	
	
	@Value("${payment.inicis.clientIp}")
	private String inicisClientIp;
	
	@Value("${payment.inicis.mid}")
	private String inicisMid;
	
	@Value("${payment.inicis.cp.url}")
	private String inicisCpUrl;
				
	@Value("${payment.inicis.encrypt.iv}")
	private String inicisCipherIv;
	
	@Value("${payment.inicis.encrypt.key}")
	private String inicisCipherKey;
	
	@Value("${payment.biztalk.url}")
	private String biztalkUrl;
	
	@Value("${payment.biztalk.enable}")
	private boolean biztalkEnable;
	
		
	
	// BTC 파트너코드
	private static final String BTC = "PRTNR_0000";

	// benepia 파트너코드
	private static final String BTB_BENEPIA = "PRTNR_0002";
	
	// 이지웰 파트너코드
	private static final String BTB_EZWEL = "PRTNR_0001";

	// 이제너두 파트너코드
	private static final String BTB_EXANADU = "PRTNR_0003";
	
	// biz톡 템플릿코드
	// 주문완료 (고객)
	private static final String BIZTALK_TAMPLATE_002 = "template_002";
	
	// 주문완료 (업체)
	private static final String BIZTALK_TAMPLATE_017 = "template_017";
	
	// 구독해지완료
	private static final String BIZTALK_TAMPLATE_005 = "template_005";
	
	// 결제실패
	private static final String BIZTALK_TAMPLATE_012 = "template_012";
	
	// 비즈톡
	
	/**
	 * 이니시스 빌키 결제
	 */
	@Override
	public ModooPayVO doInicisRegularPay(ModooPayVO mvo) {
		LOGGER.info("doInicisRegularPay : [ {} ], [ {} ]", mvo.getORDER_NO(), mvo.getORDRR_NM());

		// 기본 데이터
		String type = "Billing";
		String paymethod = "Card";
					
		//이니시스용 전문 전송 시간
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date(mvo.getTIMESTAMP().longValue());
		String inicisTimeStamp = sdf.format(date);	
				
		try {	
			// 해시 생성
			// KEY+type+paymethod+timestamp+clientIp+mid+moid+price+billKey			
			String hashSrc = inicisCipherKey + 
					type + 
					paymethod + 
					inicisTimeStamp + 
					inicisClientIp + 
					inicisMid + 
					mvo.getORDER_GROUP_NO().longValue() + 
					mvo.getSETLE_CARD_AMOUNT().longValue() +
					mvo.getBILL_KEY();
			
			String hashData = encryptUtil.getHash("SHA-512", hashSrc);
								
			HttpPost httpPost = new HttpPost(inicisUrl);
		    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		    		    	
			List<NameValuePair> postParams = new ArrayList<NameValuePair>();			
			postParams.add(new BasicNameValuePair("type", type));
			postParams.add(new BasicNameValuePair("paymethod", paymethod));
			postParams.add(new BasicNameValuePair("timestamp", inicisTimeStamp));
			postParams.add(new BasicNameValuePair("clientIp", inicisClientIp));
			postParams.add(new BasicNameValuePair("mid", inicisMid));
			postParams.add(new BasicNameValuePair("url", inicisCpUrl));
			postParams.add(new BasicNameValuePair("moid", "" + mvo.getORDER_GROUP_NO().longValue()));
			postParams.add(new BasicNameValuePair("goodName", "" + mvo.getORDER_NO()));
			postParams.add(new BasicNameValuePair("buyerName", mvo.getORDRR_NM()));
			postParams.add(new BasicNameValuePair("buyerEmail", mvo.getORDRR_EMAIL()));
			postParams.add(new BasicNameValuePair("buyerTel", mvo.getORDRR_TELNO()));
			postParams.add(new BasicNameValuePair("price", "" + mvo.getSETLE_CARD_AMOUNT().longValue()));
//			postParams.add(new BasicNameValuePair("regNo", "생년월일(YYMMDD)"));
//			postParams.add(new BasicNameValuePair("cardPw", "카드비밀번호 2자리"));
//			postParams.add(new BasicNameValuePair("currency", "통화코드 (WON / USD)"));
			postParams.add(new BasicNameValuePair("billKey", mvo.getBILL_KEY()));
			postParams.add(new BasicNameValuePair("authentification", "00"));
//			postParams.add(new BasicNameValuePair("cardQuota", "할부기간"));
//			postParams.add(new BasicNameValuePair("quotaInterest", "무이자구분"));
			postParams.add(new BasicNameValuePair("hashData", hashData));			
			
			org.apache.http.HttpEntity postEntity = new UrlEncodedFormEntity(postParams, "UTF-8");			 
			httpPost.setEntity(postEntity);
			 
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			CloseableHttpResponse response = httpClient.execute(httpPost);
			 
		    ResponseHandler<String> handler = new BasicResponseHandler();
	        String body = handler.handleResponse(response);
	        	        		
	        int statusCode = response.getStatusLine().getStatusCode();

	        if(statusCode == 200){
	        	Map<String, String> resultMap = new Gson().fromJson(body, new TypeToken<HashMap<String, String>>(){}.getType());	
		    	
		    	String resultCode = resultMap.get("resultCode");
		    	String resultMsg = resultMap.get("resultMsg");
		    	String tid = resultMap.get("tid");
		    	
		    	//결제 성공 여부 확인
		    	if(!resultCode.equalsIgnoreCase("00")) {
		    		LOGGER.error("[ " + mvo.getORDER_NO() + " ] Inicis billing ERROR : {}, {} ", resultCode, resultMsg);
		    		mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R001);
			    	mvo.setSETLE_RESULT_MSSAGE("Inicis billing ERROR : " + resultMsg);
		    	} else {	
		    		LOGGER.info("[ " + mvo.getORDER_NO() + " ] doInicisRegularPay success : {}", tid);		
		    		mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R000);
			    	mvo.setSETLE_RESULT_MSSAGE("Inicis billing is succeed.");
		    	}
	        			    	
		    	mvo.setINI_SETLE_CONFM_NO(tid);		  
		    	    	
		    }else{
		    	mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R002);
		    	mvo.setSETLE_RESULT_MSSAGE("Inicis billing http error : " + statusCode);
		    	return mvo;
		    }		    
		} catch(Exception e) {
			LOGGER.error("[ " + mvo.getORDER_NO() + " ] doInicisRegularPay ERROR : {}", e);
			
			mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R003);
	    	mvo.setSETLE_RESULT_MSSAGE("Inicis billing exception");
	    	return mvo;
		}		
			
		return mvo;
	}

	/**
	 * 이제너두 포인트 조회 및 계산
	 * @param mvo
	 * @return
	 */
	@Override
	public ModooPayVO doExanaduPoint(ModooPayVO mvo) {

		LOGGER.info("doExanaduPoint : [ {} ], [ {} ]", mvo.getORDER_NO(), mvo.getORDRR_NM());

		URL url = null;
		BufferedReader in = null;
		URLConnection con = null;
		PrintWriter out = null;

		Map<String, Object> resultObj = new HashMap<String, Object>();

		//이제너두 포인트 사용여부 확인 및 조회 (결제종류를 카드로 선택해놨다면 이 과정을 건너뛴다)
		String exanaduPoint = "0";

		if(mvo.getSETLE_TY_CODE() != null && (mvo.getSETLE_TY_CODE().equalsIgnoreCase("EXA")||mvo.getSETLE_TY_CODE().equalsIgnoreCase("EXCD"))) {

			try {

				Map<String, Object> json = new HashMap<String, Object>();

				// TODO 운영 URL로 추후 변경 필요
				/*url = new URL("http://dev-fo.etbs.co.kr/wl/servlets/bene.api.AvailAmt?vendor="+exanaduVendor+"&svid="+exanaduSvid+"&userId=" + mvo.getUSER_KEY());*/
				url = new URL(exanaduUrl+ "/wl/servlets/bene.api.AvailAmt?vendor="+exanaduVendor+"&svid=" +exanaduSvid+ "&userId=" +mvo.getUSER_KEY());
				con = url.openConnection();
				con.setDoOutput(true);
				String jsonString = json.toString();


				/*송신전문 OutputStream*/
				out = new PrintWriter(con.getOutputStream());
				out.println(jsonString);
				out.close();

				/*수신전문 InputStream*/
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String line = null;
				while ((line = in.readLine()) != null) {
					System.out.println("======================이제너두 수신전문 ==========================");
					resultObj = convertJSONstringToMap(line);
					System.out.println("resultObj" + resultObj.toString());
					System.out.println("availAmt" + resultObj.get("availAmt"));

					exanaduPoint=String.valueOf(resultObj.get("availAmt"));
				}

				// 이과정에서 오류나면 회원의 상태가 정상적이지 않다는 의미.
			} catch(Exception e) {
				LOGGER.error("[ " + mvo.getORDER_NO() + " ] pointPayWithExanadu ERROR 110 : {}", e);
				mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R021);
				return mvo;
			}
		}

		// 결제금액
		BigInteger totAmount = mvo.getSETLE_TOT_AMOUNT();

		// 가용가능한 이제너두 포인트
		BigInteger useablePoint = new BigInteger(exanaduPoint);

		// 사용할 이제너두 포인트
		BigInteger useEzwelPoint = BigInteger.ZERO;

		// 카드결제할 금액 (결제금액 - 이제너두 포인트)
		BigInteger restAmount = BigInteger.ZERO;
		// 무료상품일경우 0원으로 결제
		if(!mvo.getSETLE_RESULT_TY_CODE().equalsIgnoreCase("FREE")) {
			//이제너두 포인트가 결제금액 이상이면 이니시스 결제를 진행할 필요 없다.
			if(totAmount.compareTo(useablePoint) <= 0) {
				// 실제 결제 상태 기록
				mvo.setSETLE_RESULT_TY_CODE("EXA");
				useEzwelPoint = totAmount;
			} else {
				// 이제너두 포인트가 0 이라면 이니시스(카드)로만 결제한다.
				if(useablePoint.equals(BigInteger.ZERO)) {
					mvo.setSETLE_RESULT_TY_CODE("CARD");
				} else {
					// 복합결제가 되었을경우
					mvo.setSETLE_RESULT_TY_CODE("EXCD");
				}
				useEzwelPoint = useablePoint;
				restAmount = totAmount.subtract(useEzwelPoint);
			}
		}

		mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R000);
		mvo.setSETLE_POINT(useEzwelPoint);
		mvo.setSETLE_CARD_AMOUNT(restAmount);

		return mvo;
	}

	@Override
	public ModooPayVO doExanaduPay(ModooPayVO mvo) {

		java.math.BigDecimal totalSplPc = new BigDecimal("0");

		Map<String, Object> resultObj = new HashMap<String, Object>();

		URL url = null;
		BufferedReader in = null;
		URLConnection con = null;
		PrintWriter out = null;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date(mvo.getTIMESTAMP().longValue());
		String orderDate = sdf.format(date);

		Map<String, Object> payDetail = new HashMap<String, Object>();
		Map<String, Object> requestParam = new HashMap<String, Object>();

		try {
			List<Map<String, Object>> payDetails = new ArrayList<Map<String, Object>>();

			String exanaduOrderNo = "";
			totalSplPc = totalSplPc.add(new BigDecimal(mvo.getORDER_CO()).multiply(mvo.getGOODS_SPLPC()));
			payDetail.put("ITEM_CNT", mvo.getORDER_CO()); // 상품별 주문 수량
			payDetail.put("TOT_PRICE", mvo.getSETLE_TOT_AMOUNT().toString()); // 상품별총판매가
			payDetail.put("TOT_SUPPLY", new BigDecimal(mvo.getORDER_CO()).multiply(mvo.getGOODS_SPLPC())); // 상품별총공급가
			payDetail.put("ITEM_CODE", mvo.getGOODS_ID()); // 상품코드
			payDetail.put("ITEM_NAME", mvo.getGOODS_NM()); // 상품명
			payDetail.put("USE_DATE", orderDate); // 실사용일
			payDetail.put("USER_NAME", mvo.getORDRR_NM()); // 실사용자

			// 과세/면세기준
			if ("TA01".equals(mvo.getTAXT_SE_CODE())) {
				payDetail.put("ITEM_TAX", "01");
			} else if ("TA02".equals(mvo.getTAXT_SE_CODE())) {
				payDetail.put("ITEM_TAX", "00");
			}

			payDetail.put("VNDR_PROD_TP_CD", "10"); // 제휴상품유형
			payDetails.add(payDetail);

			HttpPost httpPost = new HttpPost(ezwelUrl);
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			System.out.println(mvo.getORDER_NO()+(mvo.getORDER_ODR())+"@@@");

			List<NameValuePair> postParams = new ArrayList<NameValuePair>();

			requestParam.put("CUST_ID", mvo.getUSER_KEY()); //연동 시 전달 받은 고객 식별키
			requestParam.put("VENDOR_ORDERNO", mvo.getORDER_NO()+(mvo.getORDER_ODR())); //제휴사 주문번호(이제너두는 주문번호+주문회차)
			requestParam.put("ORDER_DATE", orderDate); //주문일
			requestParam.put("SUM_TOT_PRICE", mvo.getSETLE_TOT_AMOUNT().toString()); //상품별총판매가합계(배송비제외)
			requestParam.put("SUM_TOT_SUPPLY", totalSplPc.toString()); //상품별총공급가합계(배송비제외)
			requestParam.put("DELIVERY_FAIR", mvo.getDLVY_AMOUNT().toString()); //배송비, 없을 경우 0
			requestParam.put("DELIVERY_NAME", mvo.getDLVY_USER_NM()); //받는사람이름
			requestParam.put("DELIVERY_DATE", orderDate); //배송희망일
			requestParam.put("DELIVERY_TEL", mvo.getDLVY_TELNO()); //받는사람연락처
			requestParam.put("DELIVERY_HP", mvo.getDLVY_TELNO()); //받는사람핸드폰

			System.out.println("=================================우편번호:" + mvo.getDLVY_ZIP());
			System.out.println("앞자리" + mvo.getDLVY_ZIP().substring(0, 3));
			System.out.println("뒷자리" + mvo.getDLVY_ZIP().substring(3, 5));

			requestParam.put("DELIVERY_ZIP1", mvo.getDLVY_ZIP().substring(0, 3)); //배송지우편번호
			requestParam.put("DELIVERY_ZIP2", mvo.getDLVY_ZIP().substring(3, 5)); //배송지우편번호
			requestParam.put("DELIVERY_ADDR1", mvo.getDLVY_ADRES()); //배송지주소
			requestParam.put("DELIVERY_ADDR2", mvo.getDLVY_ADRES_DETAIL()); //배송지주소상세

			requestParam.put("CARD_AMOUNT", mvo.getSETLE_CARD_AMOUNT().toString()); //제휴사카드결제금액
			requestParam.put("POINT_AMOUNT", mvo.getSETLE_POINT().toString()); //제너두복지포인트결제금액
			requestParam.put("ACCT_AMOUNT", 0); //제휴사가상계좌 및 계좌이체금액
			requestParam.put("ETC_AMOUNT", 0); //제휴사자체기타결제금액
			requestParam.put("XAN_KEY", ""); //제너두복지포인트기준키
			requestParam.put("payDetail", payDetails); //주문상세정보
			requestParam.put("DETAIL_CNT", 1); //payDetail 건수

			System.out.println(requestParam.toString()+"@@@@");
			// TODO 운영 URL로 추후 변경 필요
			url = new URL(exanaduUrl+"/wl/servlets/tbs.pmt.vendor.gate.URLConnLinkGateJson?vendor="+exanaduVendor+"&svid="+exanaduSvid+ "&cust_id=" +mvo.getUSER_KEY()+ "&step=PayInfo");
			// 로컬 테스트용 URL
			/*url = new URL("http://dev-fo.etbs.co.kr" +"/wl/servlets/tbs.pmt.vendor.gate.URLConnLinkGateJson?vendor=VR003309&svid=1706&cust_id=" + mvo.getUSER_KEY() + "&step=PayInfo");*/

			con = url.openConnection();
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=euc-kr");

			ObjectMapper mapper = new ObjectMapper();
			String jsonString = mapper.writeValueAsString(requestParam);

			/*송신전문 OutputStream*/
			out = new PrintWriter(con.getOutputStream());
			out.println(jsonString);
			out.close();

			/*수신전문 InputStream*/
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				System.out.println("======================exanadu response params ==========================");
				resultObj = convertJSONstringToMap(line);
				System.out.println(resultObj.toString());

				resultObj.put("resultCode", resultObj.get("RESULT"));
				if ("OK".equals(resultObj.get("RESULT"))) {
					mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R000);
					mvo.setSETLE_RESULT_MSSAGE("Exanadu point confirm-order is succeed.");
					exanaduOrderNo = String.valueOf(resultObj.get("EXAN_ORDER"));
				} else {
					mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R023);
					mvo.setSETLE_RESULT_MSSAGE("EXANADU PAY failed");
					return mvo;
				}
			}
			LOGGER.error("[ " + mvo.getORDER_NO() + " ] pointPayWithExanadu orderNo : {}",exanaduOrderNo );
			mvo.setEZW_SETLE_CONFM_NO(exanaduOrderNo);

		}catch (Exception e){
			LOGGER.error("[ " + mvo.getORDER_NO() + " ] pointPayWithExanadu ERROR 111 command : {}", e);
			mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R022);
			mvo.setSETLE_RESULT_MSSAGE("EXANADU PAY command Exception");
			return mvo;

		} finally {
			if ( in != null ) {
				try {
					in.close();
				} catch (Exception e) {
					mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R021);
					LOGGER.error("[ " + mvo.getORDER_NO() + " ] pointPayWithExanadu ERROR  command : {}", e);
					e.printStackTrace();
				}
			}
			if ( out != null ) {
				try {
					out.close();
				} catch (Exception e) {
					mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R021);
					LOGGER.error("[ " + mvo.getORDER_NO() + " ] pointPayWithExanadu ERROR 111 command : {}", e);
					e.printStackTrace();
				}
			}
		}
		return  mvo;
	}

	/**
	 * JSON String을 Map으로 변환
	 * @param json
	 * @return
	 * @throws Exception
	 * @author
	 */
	public static Map<String,Object> convertJSONstringToMap(String json) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = new HashMap<String, Object>();

		map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});

		return map;
	}

	/**
	 * 이지웰 포인트 조회 및 결제금액산정
	 */
	@Override
	public ModooPayVO doEzwelPoint(ModooPayVO mvo) {
		LOGGER.info("doEzwelPoint : [ {} ], [ {} ]", mvo.getORDER_NO(), mvo.getORDRR_NM());
		
		String enc_cspCd = "";
		String enc_clientCd = "";
		String enc_userKey = "";
		
		try {
			//공통 파라미터
			enc_cspCd = encryptUtil.encodeParamBase64(ezwelCspCd);
			enc_clientCd = encryptUtil.encodeParamForEzwel(mvo.getCLIENT_CD());
		    enc_userKey = encryptUtil.encodeParamForEzwel(mvo.getUSER_KEY());
		} catch(Exception e) {
			LOGGER.error("[ " + mvo.getORDER_NO() + " ] doEzwelPoint ERROR : userinfo is invalid");
			mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R010);
			mvo.setSETLE_RESULT_MSSAGE("Ezwel ERROR : userinfo is invalid");
			return mvo;
		}	 
		
		//이지웰 포인트 사용여부 확인 및 조회 (결제종류를 카드로 선택해놨다면 이 과정을 건너뛴다)
		String ezwelPoint = "0";
		
		if(mvo.getPOINT_YN() != null && mvo.getPOINT_YN().equalsIgnoreCase("Y")
				&& mvo.getSETLE_TY_CODE() != null && (mvo.getSETLE_TY_CODE().equalsIgnoreCase("EZP")||mvo.getSETLE_TY_CODE().equalsIgnoreCase("EZCD"))) {
			
		    String enc_110_command = encryptUtil.encodeParamForEzwel("110");
		    
			try {				
				HttpPost httpPost = new HttpPost(ezwelUrl);
				httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
						    
			    List<NameValuePair> postParams = new ArrayList<NameValuePair>();			
				postParams.add(new BasicNameValuePair("cspCd", enc_cspCd));
				postParams.add(new BasicNameValuePair("command", enc_110_command));
				postParams.add(new BasicNameValuePair("clientCd", enc_clientCd));			
				postParams.add(new BasicNameValuePair("userKey", enc_userKey));

				LOGGER.info("[ " + mvo.getORDER_NO() + " ] userKey : {}", enc_userKey);
				
				org.apache.http.HttpEntity postEntity = new UrlEncodedFormEntity(postParams, "UTF-8");			 
				httpPost.setEntity(postEntity);
				 
				CloseableHttpClient httpClient = HttpClientBuilder.create().build();
				CloseableHttpResponse response = httpClient.execute(httpPost);			 
				
		        int statusCode = response.getStatusLine().getStatusCode();
		        
		        // 이과정에서 오류나면 회원의 상태가 정상적이지 않다는 의미.
			    if(statusCode == 200){			    	
			    	ResponseHandler<String> handler = new BasicResponseHandler();
			    	ezwelPoint = handler.handleResponse(response);
			    	
			    	LOGGER.info("pointPayWithEzwel result ezwelPoint response : {}" , ezwelPoint);
			    	
			    	// 복호화
			    	ezwelPoint = encryptUtil.decodeResultForEzwel(ezwelPoint.trim()).trim();
			    	
			    	if(TextUtil.isEmpty(ezwelPoint)) {
			    		mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R020);
			    		return mvo;
			    	}
			    	
			    	// 숫자만 추출한다.
			    	ezwelPoint = ezwelPoint.replaceAll("[^0-9]","");
			    	
			    	if(TextUtil.isEmpty(ezwelPoint)) {
			    		mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R020);
			    		return mvo;
			    	}
			    				    	
			    	LOGGER.info("[ " + mvo.getORDER_NO() + " ] pointPayWithEzwel result ezwelPoint : {}", ezwelPoint);
			    	
			    }else{
			    	mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R020);
			    	return mvo;
			    }
			    
			} catch(Exception e) {
				LOGGER.error("[ " + mvo.getORDER_NO() + " ] pointPayWithEzwel ERROR 110 : {}", e);		
				mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R020);
		    	return mvo;
			}
		} 
				
		// 실제로 이지웰 포인트를 쓸 수 있는지 여부 확인 (추후 이니시스 카드 결제할지 여부에 사용)
		if(ezwelPoint == null || ezwelPoint.isEmpty()) {
			ezwelPoint = "0";
		}		
		
		// 결제금액
		BigInteger totAmount = mvo.getSETLE_TOT_AMOUNT();
		
		// 가용가능한 이지웰 포인트
		BigInteger useablePoint = new BigInteger(ezwelPoint);
		
		// 사용할 이지웰 포인트
		BigInteger useEzwelPoint = BigInteger.ZERO;
		
		// 카드결제할 금액 (결제금액 - 이지웰 포인트)		
		BigInteger restAmount = BigInteger.ZERO;
			
		// 무료상품일경우 0원으로 결제
		if(!mvo.getSETLE_RESULT_TY_CODE().equalsIgnoreCase("FREE")) {
			//이지웰 포인트가 결제금액 이상이면 이니시스 결제를 진행할 필요 없다.
			if(totAmount.compareTo(useablePoint) <= 0) {			
				// 실제 결제 상태 기록
				mvo.setSETLE_RESULT_TY_CODE("EZP"); 
				useEzwelPoint = totAmount;
			} else {
				// 이지웰 포인트가 0 이라면 이니시스(카드)로만 결제한다.
				if(useablePoint.equals(BigInteger.ZERO)) {
					mvo.setSETLE_RESULT_TY_CODE("CARD"); 
				} else {
					// 복합결제가 되었을경우
					mvo.setSETLE_RESULT_TY_CODE("EZCD"); 
				}
				useEzwelPoint = useablePoint;
				restAmount = totAmount.subtract(useEzwelPoint);
			}
		}
				
		mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R000);
		mvo.setSETLE_POINT(useEzwelPoint);
		mvo.setSETLE_CARD_AMOUNT(restAmount);
		
		return mvo;
	}


	/**
	 * 이지웰 결제
	 */
	@Override
	public ModooPayVO doEzwelPay(ModooPayVO mvo) {
		LOGGER.info("doEzwelPay : [ {} ], [ {} ]", mvo.getORDER_NO(), mvo.getORDRR_NM());
		
		String enc_cspCd = "";
		String enc_clientCd = "";
		String enc_userKey = "";
		
		try {
			//공통 파라미터
			enc_cspCd = encryptUtil.encodeParamBase64(ezwelCspCd);
			enc_clientCd = encryptUtil.encodeParamForEzwel(mvo.getCLIENT_CD());
		    enc_userKey = encryptUtil.encodeParamForEzwel(mvo.getUSER_KEY());
		} catch(Exception e) {
			LOGGER.error("[ " + mvo.getORDER_NO() + " ] pointPayWithEzwel ERROR : userinfo is invalid");
			mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R010);
			mvo.setSETLE_RESULT_MSSAGE("Ezwel ERROR : userinfo is invalid");
			return mvo;
		}	
		
		LOGGER.info("[ " + mvo.getORDER_NO() + " ] pointPayWithEzwel useEzwelPoint : [ {} ]", mvo.getSETLE_POINT());
		
		// 이지웰 포인트 선차감
		// 이지웰 포인트를 사용할수 없는 경우에도 선차감 API는 요청한다.		
		String ezwelOrderNo = "";
		try {
			String enc_111_command = encryptUtil.encodeParamForEzwel("111");
			
			String enc_usePoint = encryptUtil.encodeParamForEzwel(mvo.getSETLE_POINT().toString());
			String enc_useMileage = encryptUtil.encodeParamForEzwel("0");
			String enc_useSpecialPoint = encryptUtil.encodeParamForEzwel("0");
			
			String enc_goodsNm = encryptUtil.encodeParamForEzwel(mvo.getGOODS_NM());
			String enc_goodsCd = encryptUtil.encodeParamForEzwel(mvo.getGOODS_ID());
			String enc_unitCost = encryptUtil.encodeParamForEzwel("" + mvo.getGOODS_AMOUNT());
			
			String enc_buyPrice = encryptUtil.encodeParamForEzwel("" + mvo.getGOODS_AMOUNT());
			String enc_orderCount = encryptUtil.encodeParamForEzwel("" + mvo.getORDER_CO());
			String enc_orderTotal = encryptUtil.encodeParamForEzwel(mvo.getSETLE_TOT_AMOUNT().toString());
			
			String enc_payMoney = encryptUtil.encodeParamForEzwel(mvo.getSETLE_TOT_AMOUNT().toString());
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
			Date date = new Date(mvo.getTIMESTAMP().longValue());
			String[] orderDate = sdf.format(date).split("_");		
			
			String enc_orderDd = encryptUtil.encodeParamForEzwel(orderDate[0]);
			String enc_orderTm = encryptUtil.encodeParamForEzwel(orderDate[1]);
			
			String enc_orderNm = encryptUtil.encodeParamForEzwel(mvo.getORDRR_NM());
			String enc_orderEmail = encryptUtil.encodeParamForEzwel(mvo.getORDRR_EMAIL());
			String enc_orderTelNum = encryptUtil.encodeParamForEzwel(mvo.getORDRR_TELNO());
			
			String enc_orderMobile = encryptUtil.encodeParamForEzwel(mvo.getORDRR_TELNO());
			String enc_rcverNm = encryptUtil.encodeParamForEzwel(mvo.getDLVY_USER_NM());
			String enc_rcverTelNum = encryptUtil.encodeParamForEzwel(mvo.getDLVY_TELNO());
			
			String enc_rcverMobile = encryptUtil.encodeParamForEzwel(mvo.getDLVY_TELNO());
			String enc_dlvrPost = encryptUtil.encodeParamForEzwel(mvo.getDLVY_ZIP());
			String enc_dlvrAddr1 = encryptUtil.encodeParamForEzwel(mvo.getDLVY_ADRES());
			
			String enc_dlvrAddr2 = encryptUtil.encodeParamForEzwel(mvo.getDLVY_ADRES_DETAIL());
			String enc_orderRequest = encryptUtil.encodeParamForEzwel(mvo.getDLVY_MSSAGE());
			String enc_aspOrderNum = encryptUtil.encodeParamForEzwel("" + mvo.getORDER_NO());
						
			HttpPost httpPost = new HttpPost(ezwelUrl);
		    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    		    
			List<NameValuePair> postParams = new ArrayList<NameValuePair>();			
			postParams.add(new BasicNameValuePair("cspCd", enc_cspCd));
			postParams.add(new BasicNameValuePair("command", enc_111_command));
			postParams.add(new BasicNameValuePair("clientCd", enc_clientCd));
			postParams.add(new BasicNameValuePair("userKey", enc_userKey));
			
			postParams.add(new BasicNameValuePair("usePoint", enc_usePoint));
			postParams.add(new BasicNameValuePair("useMileage", enc_useMileage));
			postParams.add(new BasicNameValuePair("useSpecialPoint", enc_useSpecialPoint));
			
			postParams.add(new BasicNameValuePair("goodsNm", enc_goodsNm));
			postParams.add(new BasicNameValuePair("goodsCd", enc_goodsCd));
			postParams.add(new BasicNameValuePair("unitCost", enc_unitCost));
			
			postParams.add(new BasicNameValuePair("buyPrice", enc_buyPrice));
			postParams.add(new BasicNameValuePair("orderCount", enc_orderCount));
			postParams.add(new BasicNameValuePair("orderTotal", enc_orderTotal));
						
			postParams.add(new BasicNameValuePair("payMoney", enc_payMoney));
			postParams.add(new BasicNameValuePair("orderDd", enc_orderDd));
			postParams.add(new BasicNameValuePair("orderTm", enc_orderTm));
			
			postParams.add(new BasicNameValuePair("orderNm", enc_orderNm));
			postParams.add(new BasicNameValuePair("orderEmail", enc_orderEmail));
			postParams.add(new BasicNameValuePair("orderTelNum", enc_orderTelNum));
			
			postParams.add(new BasicNameValuePair("orderMobile", enc_orderMobile));
			postParams.add(new BasicNameValuePair("rcverNm", enc_rcverNm));
			postParams.add(new BasicNameValuePair("rcverTelNum", enc_rcverTelNum));
			
			postParams.add(new BasicNameValuePair("rcverMobile", enc_rcverMobile));
			postParams.add(new BasicNameValuePair("dlvrPost", enc_dlvrPost));
			postParams.add(new BasicNameValuePair("dlvrAddr1", enc_dlvrAddr1));
			
			postParams.add(new BasicNameValuePair("dlvrAddr2", enc_dlvrAddr2));
			postParams.add(new BasicNameValuePair("orderRequest", enc_orderRequest));
			postParams.add(new BasicNameValuePair("aspOrderNum", enc_aspOrderNum));				
					
			org.apache.http.HttpEntity postEntity = new UrlEncodedFormEntity(postParams, "UTF-8");			 
			httpPost.setEntity(postEntity);
			 
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			CloseableHttpResponse response = httpClient.execute(httpPost);			 
			
	        int statusCode = response.getStatusLine().getStatusCode();
	        
		    if(statusCode == 200){		    	
		    	ResponseHandler<String> handler = new BasicResponseHandler();
		    	ezwelOrderNo = handler.handleResponse(response);
		    	
		    	// 복호화
		    	ezwelOrderNo = encryptUtil.decodeResultForEzwel(ezwelOrderNo.trim()).trim();
		    	
		    	// 응답형식에 맞게 추출
		    	ezwelOrderNo = ezwelOrderNo.replaceAll("[^0-9YN|]","");
		        
		        LOGGER.info("[ " + mvo.getORDER_NO() + " ] pointPayWithEzwel result ezwelOrderNo : {}", ezwelOrderNo);
		    	
		    }else{		    	
		    	LOGGER.error("[ " + mvo.getORDER_NO() + " ] pointPayWithEzwel ERROR 111 command : http result : {}", statusCode);
    	
		    	mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R014);
		    	mvo.setSETLE_RESULT_MSSAGE("Ezwel 111 command ERROR - http result : " + statusCode);
		    	
		    	return mvo;
		    }
	    
		} catch (Exception e) {
			LOGGER.error("[ " + mvo.getORDER_NO() + " ] pointPayWithEzwel ERROR 111 command : {}", e);
			
			mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R015);
	    	mvo.setSETLE_RESULT_MSSAGE("Ezwel 111 command Exception");
	    	return mvo;
		}	
		
		// 이지웰 선차감 API가 실패하면 더이상 결제를 진행하지 않는다.
		if(ezwelOrderNo == null || ezwelOrderNo.isEmpty() || ezwelOrderNo.equalsIgnoreCase("N")) {
			LOGGER.error("[ " + mvo.getORDER_NO() + " ] pointPayWithEzwel ERROR 111 command : ezwelOrderNo is empty");
			
			mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R016);
	    	mvo.setSETLE_RESULT_MSSAGE("Ezwel 111 command ERROR : ezwelOrderNo is empty");
	    	return mvo;
		}
				
		//  Y|123462394  이런 형태로 응답이 온다
		try {
			if(!ezwelOrderNo.contains("|") || !ezwelOrderNo.startsWith("Y")) {
				LOGGER.error("[ " + mvo.getORDER_NO() + " ] pointPayWithEzwel ERROR 111 command : ezwelOrderNo is invalid : {}", ezwelOrderNo);
				mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R017);
		    	mvo.setSETLE_RESULT_MSSAGE("[ " + mvo.getORDER_NO() + " ] Ezwel 111 command ERROR : ezwelOrderNo is invalid");
		    	return mvo;
			}
			String[] ezwelOrderNoResults = ezwelOrderNo.split("\\|");
			
			if(ezwelOrderNoResults == null || ezwelOrderNoResults.length < 2) {
				LOGGER.error("[ " + mvo.getORDER_NO() + " ] Ezwel ERROR 111 command : ezwelOrderNo is invalid: {}", ezwelOrderNo);
				mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R017);
		    	mvo.setSETLE_RESULT_MSSAGE("[ " + mvo.getORDER_NO() + " ] Ezwel 111 command ERROR : ezwelOrderNo is invalid");
		    	return mvo;
			}
			
			ezwelOrderNo = ezwelOrderNoResults[1];
		}catch(Exception e) {
			// 이 에러는 일반적인 상황에서 일어날 수가 없다...
			LOGGER.error("[ " + mvo.getORDER_NO() + " ] Ezwel ERROR 111 command : {}", e);
			mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R017);
	    	mvo.setSETLE_RESULT_MSSAGE("[ " + mvo.getORDER_NO() + " ] Ezwel 111 command ERROR : ezwelOrderNo is invalid");
	    	return mvo;
		}
		
		// 이지웰  주문번호 저장
		mvo.setEZW_SETLE_CONFM_NO(ezwelOrderNo);
		
		//이지웰 주문 확정 요청
		String ezwelPayResult = "";				
		try {
			String enc_112_command = encryptUtil.encodeParamForEzwel("112");
			String enc_ezwelOrderNo = encryptUtil.encodeParamForEzwel(ezwelOrderNo);
			String enc_aspOrderNum = encryptUtil.encodeParamForEzwel("" + mvo.getORDER_NO());
						
			HttpPost httpPost = new HttpPost(ezwelUrl);
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
					    
		    List<NameValuePair> postParams = new ArrayList<NameValuePair>();			
			postParams.add(new BasicNameValuePair("cspCd", enc_cspCd));
			postParams.add(new BasicNameValuePair("command", enc_112_command));
			postParams.add(new BasicNameValuePair("aspOrderNum", enc_aspOrderNum));			
			postParams.add(new BasicNameValuePair("orderNum", enc_ezwelOrderNo));
			
			org.apache.http.HttpEntity postEntity = new UrlEncodedFormEntity(postParams, "UTF-8");			 
			httpPost.setEntity(postEntity);
			 
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			CloseableHttpResponse response = httpClient.execute(httpPost);			 
			
	        int statusCode = response.getStatusLine().getStatusCode();
	        
		    if(statusCode == 200){
		    	mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R000);
		    	mvo.setSETLE_RESULT_MSSAGE("Ezwel point confirm-order is succeed.");
		    	
		    	ResponseHandler<String> handler = new BasicResponseHandler();
		    	ezwelPayResult = handler.handleResponse(response).trim();
		    	
		    	// 응답형태에 맞게 추출
		    	ezwelPayResult = ezwelPayResult.replaceAll("[^YN]","");
		        
		    	LOGGER.info("[ " + mvo.getORDER_NO() + " ] Ezwel result ezwelPayResult : {}", ezwelPayResult);		    	
		    }else{
		    	LOGGER.error("[ " + mvo.getORDER_NO() + " ] Ezwel ERROR 112 command: http result : {}", statusCode);

		    	mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R018);
		    	mvo.setSETLE_RESULT_MSSAGE("Ezwel 112 command ERROR - http result : " + statusCode);
		    	return mvo;
		    }		    
		} catch(Exception e) {
			LOGGER.error("[ " + mvo.getORDER_NO() + " ] Ezwel 112 command ERROR : {}", e);
			
			mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R018);
	    	mvo.setSETLE_RESULT_MSSAGE("Ezwel 112 command http exception");
	    	return mvo;
		}
		
		// 선차감 요청 성공하고 나서 주문확정이 성공적으로 안되면 결제 실패로 간주한다.
		if(!ezwelPayResult.equalsIgnoreCase("Y")) {
			LOGGER.error("[ " + mvo.getORDER_NO() + " ] Ezwel 112 command ERROR : ezwelPayResult failed : {}", PayException.ERR_CODE_R019);

			mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R019);
	    	mvo.setSETLE_RESULT_MSSAGE("Ezwel 112 command error : ezwelPayResult is invalid");
	    	return mvo;
		}		
				
		return mvo;
	}




	/**
	 * 정기결제 프로세스
	 * 1. 주문상태 확인 및 최종 결제 금액 계산
	 * 2. 이니시스 빌키 결제
	 * 3. B2B 결제
	 * 4. 결제결과 후처리
	 * @param mvo
	 * @return
	 */
	@Override
	public ModooPayVO doRegularPay(ModooPayVO mvo) {
		LOGGER.info("[ " + mvo.getORDER_NO() + " ] doRegularPay getORDRR_NM : {}", mvo.getORDRR_NM());
		try {
			// 전문생성시간 세팅
			mvo.setTIMESTAMP(new BigDecimal(System.currentTimeMillis()));
			
			if(mvo.getSETLE_STTUS_CODE() == null) {
				mvo.setSETLE_STTUS_CODE("Z"); //null 에러 방지를 위한 초기값세팅
			}
			
			if(mvo.getSETLE_RESULT_TY_CODE() == null || mvo.getPRTNR_ID().equalsIgnoreCase(BTC) || mvo.getPRTNR_ID().equalsIgnoreCase(BTB_BENEPIA)) {
				mvo.setSETLE_RESULT_TY_CODE("CARD"); //null 에러 방지를 위한 초기값세팅
			}

			// 빌키가 없을경우 결제를 진행하지 않는다.
			// 이지웰 포인트결제인경우 빌키 체크 하지 않음
			if(TextUtil.isEmpty(mvo.getBILL_KEY()) && (!mvo.getSETLE_TY_CODE().equals("EZP") && !mvo.getSETLE_TY_CODE().equals("EXA"))) {
				mvo.setSETLE_STTUS_CODE("F");
				mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R008);
				mvo.setSETLE_RESULT_MSSAGE("빌키가 없음 ");
				LOGGER.info("[ "+mvo.getORDER_NO() + " : mvo.setSETLE_RESULT_CODE()]  : {} , {}", mvo.getSETLE_RESULT_CODE(),mvo.getSETLE_RESULT_MSSAGE());
				return mvo;
			}
			
			// 주문이 유효하지 않으면 결제 진행하지 않는다.
			if(mvo.getORDER_STTUS_CODE() == null					
					|| mvo.getORDER_STTUS_CODE().equalsIgnoreCase("ST04")
					|| mvo.getORDER_STTUS_CODE().equalsIgnoreCase("ST99")) {				
				mvo.setSETLE_STTUS_CODE("T");
				mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R009);
				mvo.setSETLE_RESULT_MSSAGE("주문이 유효하지 않음 : " + mvo.getORDER_STTUS_CODE());
				LOGGER.info("[ "+mvo.getORDER_NO() + " : mvo.setSETLE_RESULT_CODE()]  : {} , {}", mvo.getSETLE_RESULT_CODE(),mvo.getSETLE_RESULT_MSSAGE());
				return mvo;
			}

			Integer successCnt = payDao.selectSuccessOrderCnt(mvo.getORDER_NO());
			//수강권 사용일이 종료일자를 넘었으면 구독해지
			if("Y".equals(mvo.getCOUPON_DT_END_AT()) ){
				mvo.setSETLE_STTUS_CODE("C");
				mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R999);
				mvo.setSETLE_RESULT_MSSAGE("수강권 종료일자 초과");
				LOGGER.info("[ "+mvo.getORDER_NO() + " : mvo.setSETLE_RESULT_CODE()]  : {} , {}", mvo.getORDER_NO(),mvo.getSETLE_RESULT_MSSAGE());
				return mvo;
			}
			//수강권 사용유효횟수를 넘었으면 구독해지
			LOGGER.info("SuccessCnt : {}, COUPON_VALID_PD :{}" ,successCnt,mvo.getCOUPON_VALID_PD()==null?0:mvo.getCOUPON_VALID_PD());
			if(mvo.getCOUPON_VALID_PD()!=null
					&& !StringUtils.isEmpty(mvo.getCOUPON_VALID_PD())
					&& successCnt >= Integer.valueOf(mvo.getCOUPON_VALID_PD())){
				mvo.setSETLE_STTUS_CODE("C");
				mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R999);
				mvo.setSETLE_RESULT_MSSAGE("수강권 종료횟수 초과");
				LOGGER.info("[ "+mvo.getORDER_NO() + " : mvo.setSETLE_RESULT_CODE()]  : {} , {} , VALID_PD : {}", mvo.getORDER_NO(),mvo.getSETLE_RESULT_MSSAGE(),mvo.getCOUPON_VALID_PD());
				return mvo;
			}

			
			// 주문이 취소접수중이면 다시 철회할 수도 있기때문에 다음 결제날 세팅후 넘어간다.
			if(mvo.getORDER_STTUS_CODE().equalsIgnoreCase("ST01")) {
				mvo.setSETLE_STTUS_CODE("C");
				mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R999);
				mvo.setSETLE_RESULT_MSSAGE("구독 취소 요청중");
				LOGGER.info("[ "+mvo.getORDER_NO() + " : mvo.setSETLE_RESULT_CODE()]  : {} , {}", mvo.getSETLE_RESULT_CODE(),mvo.getSETLE_RESULT_MSSAGE());
				return getNextPayDate(mvo);
			}
			
			// 주문상태가 중지이면 날짜계산만 하고 넘어간다.
			if(mvo.getSETLE_STTUS_CODE().equalsIgnoreCase("P") || mvo.getORDER_STTUS_CODE().equalsIgnoreCase("ST03")) {
				mvo.setSETLE_STTUS_CODE("P");
				mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R999);
				mvo.setSETLE_RESULT_MSSAGE("구독 건너뛰기");
				LOGGER.info("[ "+mvo.getORDER_NO() + " : mvo.setSETLE_RESULT_CODE()]  : {} , {}", mvo.getSETLE_RESULT_CODE(),mvo.getSETLE_RESULT_MSSAGE());
				return getNextPayDate(mvo);
			}
			
			// 실제 결제되어야할 결제금액을 계산하고 세팅한다.
			// 판매금액
			BigInteger sleAmount = mvo.getSLE_AMOUNT();
			
			// 할인금액
			BigInteger dscntAmount = mvo.getDSCNT_AMOUNT();
			
			if(sleAmount == null) {
				sleAmount = BigInteger.ZERO;
			}
			
			if(dscntAmount == null) {
				dscntAmount = BigInteger.ZERO;
			}
			
			// 할인금액이 더 클경우 판매금액에 맞춰준다.
			if(sleAmount.compareTo(dscntAmount) < 0) {
				dscntAmount = sleAmount;
			}
			
			// 판매금액 - 할인금액 = 실 결제금액
			BigInteger setleAmount = sleAmount.subtract(dscntAmount);		
			mvo.setSETLE_TOT_AMOUNT(setleAmount); // 주문결제테이블용
					
			// 결제할 금액이 0원이면 무료상품임을 의미한다. 
			// 이 경우 이지웰 상품이라면 0원으로 이지웰 결제를 태우지만 B2C라면 아무결제도 태우지않고 DB기록만 한다.
			if(setleAmount.equals(BigInteger.ZERO)) {
				mvo.setSETLE_RESULT_TY_CODE("FREE");
			}
		
			// 제휴사구분
			LOGGER.info("[ " + mvo.getORDER_NO() + " ] doRegularPay - mvo.getPRTNR_ID : {}", mvo.getPRTNR_ID());
		
		} catch(Exception e) {
			LOGGER.error("[ " + mvo.getORDER_NO() + " ] doRegularPay ERROR : {}", e);
			
			mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R999);
			mvo.setSETLE_RESULT_MSSAGE("정기결제 처리 오류");
			return getNextPayDate(mvo);
		}
				
		// 이지웰일경우
		if(mvo.getPRTNR_ID().equalsIgnoreCase(BTB_EZWEL)) {
			// 이지웰 포인트 조회 및 결제금액세팅
			mvo = doEzwelPoint(mvo);
			
			if(!mvo.getSETLE_RESULT_CODE().equalsIgnoreCase(PayException.ERR_CODE_R000)) {
				LOGGER.error("[ " + mvo.getORDER_NO() + " ] doRegularPay - doEzwelPoint ERROR : {} ", mvo.getSETLE_RESULT_CODE());
				mvo.setSETLE_STTUS_CODE("F");
				mvo.setSETLE_RESULT_MSSAGE("Ezwel point error");
				return mvo;
			}
			
			// 결제할 금액이 0원 초과일 경우에만 이니시스 정기 결제
			if(mvo.getSETLE_CARD_AMOUNT() != null && mvo.getSETLE_CARD_AMOUNT().compareTo(BigInteger.ZERO) > 0) {
				mvo = doInicisRegularPay(mvo);
			}
			
			// 이니시스 결제 정상 처리 여부 확인 - 이니시스 결제가 실패했다면 결제는 여기서 중지
			if(!mvo.getSETLE_RESULT_CODE().equalsIgnoreCase(PayException.ERR_CODE_R000)) {
				LOGGER.error("[ " + mvo.getORDER_NO() + " ] doRegularPay - doInicisRegularPay ERROR : {} , {}", mvo.getSETLE_RESULT_CODE(), mvo.getSETLE_RESULT_MSSAGE());
				mvo.setSETLE_STTUS_CODE("F");
				return mvo;
			} else {
				// 이니시스 결제성공 (이지웰 포인트 결제로 인해서 의도적으로 이니시스 결제를 태우지 않았으면 성공으로 간주한다) 
				mvo.setINICIS_PAY_RESULT(1);
			}
			
			// 이지웰 결제
			mvo = doEzwelPay(mvo);	

		}else if(BTB_EXANADU.equals(mvo.getPRTNR_ID())){
			// 이지웰 포인트 조회 및 결제금액세팅
			mvo = doExanaduPoint(mvo);

			if(!mvo.getSETLE_RESULT_CODE().equalsIgnoreCase(PayException.ERR_CODE_R000)) {
				LOGGER.error("[ " + mvo.getORDER_NO() + " ] doRegularPay - doEzwelPoint ERROR : {} ", mvo.getSETLE_RESULT_CODE());
				mvo.setSETLE_STTUS_CODE("F");
				mvo.setSETLE_RESULT_MSSAGE("Exanadu point error");
				return mvo;
			}

			// 결제할 금액이 0원 초과일 경우에만 이니시스 정기 결제
			if(mvo.getSETLE_CARD_AMOUNT() != null && mvo.getSETLE_CARD_AMOUNT().compareTo(BigInteger.ZERO) > 0) {
				mvo = doInicisRegularPay(mvo);
			}

			// 이니시스 결제 정상 처리 여부 확인 - 이니시스 결제가 실패했다면 결제는 여기서 중지
			if(!mvo.getSETLE_RESULT_CODE().equalsIgnoreCase(PayException.ERR_CODE_R000)) {
				LOGGER.error("[ " + mvo.getORDER_NO() + " ] doRegularPay - doInicisRegularPay ERROR : {} , {}", mvo.getSETLE_RESULT_CODE(), mvo.getSETLE_RESULT_MSSAGE());
				mvo.setSETLE_STTUS_CODE("F");
				return mvo;
			} else {
				// 이니시스 결제성공 (이지웰 포인트 결제로 인해서 의도적으로 이니시스 결제를 태우지 않았으면 성공으로 간주한다)
				mvo.setINICIS_PAY_RESULT(1);
			}

			// 이지웰 결제
			mvo = doExanaduPay(mvo);
		} else if(mvo.getPRTNR_ID().equalsIgnoreCase(BTC) || mvo.getPRTNR_ID().equalsIgnoreCase(BTB_BENEPIA)){
			// 이지웰상품이 아니거나 베네피아 상품 인경우
			mvo.setSETLE_CARD_AMOUNT(mvo.getSETLE_TOT_AMOUNT());
			
			// B2C 무료상품일경우 카드결제또한 진행하지 않는다.
			if(mvo.getSETLE_RESULT_TY_CODE().equalsIgnoreCase("FREE")) {
				mvo.setSETLE_STTUS_CODE("S");
				return getNextPayDate(mvo);
			}
			
			// 이니시스 정기 결제
			mvo = doInicisRegularPay(mvo);			
		}


		LOGGER.info(mvo.getORDER_NO() +" @@@@@@@@@@@@@@@@@@@@ "+ mvo.getSETLE_RESULT_CODE());

		// 최종 결제 결과 처리
		if(mvo.getSETLE_RESULT_CODE().equalsIgnoreCase(PayException.ERR_CODE_R000)) {
			mvo.setSETLE_STTUS_CODE("S");
		} else {
			mvo.setSETLE_STTUS_CODE("F");
			// 이지웰 결제가 실패했지만 이니시스가 성공한경우 이니시스결제를 취소해준다.
			if(mvo.getINICIS_PAY_RESULT() == 1 && mvo.getINI_SETLE_CONFM_NO() != null && mvo.getINI_SETLE_CONFM_NO().contains(inicisMid)) {
				mvo = doInicisCancel(mvo);
			}
			return mvo;
		}
					
		return getNextPayDate(mvo);
	}
	
	/**
	 * 이니시스 결제 취소
	 * @param mvo
	 * @return
	 */
	private ModooPayVO doInicisCancel(ModooPayVO mvo) {
		LOGGER.info("[ " + mvo.getORDER_NO() + " ] doInicisCancel : {}", mvo.getORDRR_NM());
		
		// 기본 데이터
		String type = "Refund";
		String paymethod = "Card";
					
		//이니시스용 전문 전송 시간
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date(mvo.getTIMESTAMP().longValue());
		String inicisTimeStamp = sdf.format(date);	
				
		try {	
			// 해시 생성
			// KEY+type+paymethod+timestamp+clientIp+mid+tid			
			String hashSrc = inicisCipherKey + 
					type + 
					paymethod + 
					inicisTimeStamp + 
					inicisClientIp + 
					inicisMid + 
					mvo.getINI_SETLE_CONFM_NO();
			
			String hashData = encryptUtil.getHash("SHA-512", hashSrc);
								
			HttpPost httpPost = new HttpPost(inicisCancelUrl);
		    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		    		    	
			List<NameValuePair> postParams = new ArrayList<NameValuePair>();			
			postParams.add(new BasicNameValuePair("type", type));
			postParams.add(new BasicNameValuePair("paymethod", paymethod));
			postParams.add(new BasicNameValuePair("timestamp", inicisTimeStamp));
			postParams.add(new BasicNameValuePair("clientIp", inicisClientIp));
			postParams.add(new BasicNameValuePair("mid", inicisMid));
			postParams.add(new BasicNameValuePair("tid", mvo.getINI_SETLE_CONFM_NO()));
			postParams.add(new BasicNameValuePair("msg", "auto canceled cuz partner payment has failed"));
			postParams.add(new BasicNameValuePair("hashData", hashData));			
			
			org.apache.http.HttpEntity postEntity = new UrlEncodedFormEntity(postParams, "UTF-8");			 
			httpPost.setEntity(postEntity);
			 
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			CloseableHttpResponse response = httpClient.execute(httpPost);
			 
		    ResponseHandler<String> handler = new BasicResponseHandler();
	        String body = handler.handleResponse(response);
	        	        		
	        int statusCode = response.getStatusLine().getStatusCode();

	        if(statusCode == 200){
	        	Map<String, String> resultMap = new Gson().fromJson(body, new TypeToken<HashMap<String, String>>(){}.getType());	
		    	
		    	String resultCode = resultMap.get("resultCode");
		    	String resultMsg = resultMap.get("resultMsg");
		    	String cancelDate = resultMap.get("cancelDate");
		    	String cancelTime = resultMap.get("cancelTime");
		    	
		    	//결제 성공 여부 확인
		    	if(!resultCode.equalsIgnoreCase("00")) {
		    		LOGGER.error("[ " + mvo.getORDER_NO() + " ] Inicis cancel ERROR : {}, {} ", resultCode, resultMsg);
			    	mvo.setSETLE_RESULT_MSSAGE("Inicis cancel ERROR : " + resultMsg);
		    	} else {	
		    		LOGGER.info("[ " + mvo.getORDER_NO() + " ] doInicisRegularPay cancel success : {}", mvo.getINI_SETLE_CONFM_NO());		
			    	mvo.setSETLE_RESULT_MSSAGE("Inicis auto cancel is succeed : " + cancelDate + cancelTime);
		    	}	  
		    	    	
		    }else{
		    	mvo.setSETLE_RESULT_MSSAGE("Inicis cancel http error : " + statusCode);
		    	return mvo;
		    }		    
		} catch(Exception e) {
			LOGGER.error("[ " + mvo.getORDER_NO() + " ] doInicisRegularPay cancel ERROR : {}", e);			
	    	mvo.setSETLE_RESULT_MSSAGE("Inicis cancel exception");
	    	return mvo;
		}		
			
		return mvo;
	}


	@Override
	public ModooPayVO getNextPayDate(ModooPayVO mvo) {
		LOGGER.info("[ " + mvo.getORDER_NO() + " ] getNextPayDate");
		
		String nextDeliDate = "";
    	String nextPayDate = "";
    	
    	try {
    		// 결제일 기준
	    	// 현재 결제일 파싱
			String payDate = mvo.getSETLE_PRARNDE();		
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(payDate));
			
	    	// 다음 결제주기 계산
	    	String cycleCode = mvo.getSBSCRPT_CYCLE_SE_CODE(); 	
	    	
	    	if(cycleCode.equalsIgnoreCase("MONTH")) {
	    		Integer monthCycle = mvo.getSBSCRPT_MT_CYCLE();	   
	    		Integer sbsMtdy = mvo.getSBSCRPT_DLVY_DAY();
	    		
	    		cal.add(Calendar.MONTH, monthCycle);
	    		
	    		int actualMaximum = cal.getActualMaximum(Calendar.DAY_OF_MONTH);	            
	    		
	            if(sbsMtdy > actualMaximum) {
	            	sbsMtdy = actualMaximum;
	            } 
	    		
	    		cal.set(Calendar.DAY_OF_MONTH, sbsMtdy);
	    	}
	    	
	    	if(cycleCode.equalsIgnoreCase("WEEK")) {
	    		Integer weekCycle = mvo.getSBSCRPT_WEEK_CYCLE();	    		
	    		cal.add(Calendar.WEEK_OF_YEAR, weekCycle);	
	    	}
	    	
	    	//다음 결제일
	    	nextPayDate = sdf.format(cal.getTime());

    		LOGGER.info("[ " + mvo.getORDER_NO() + " ] getNextPayDate : {}, {}", nextDeliDate, nextPayDate);
    		
    	}catch(Exception e) {
    		LOGGER.error("[ " + mvo.getORDER_NO() + " ] getNextPayDate ERROR : {}", e);
    	}
    	
    	mvo.setSETLE_PRARNDE(nextPayDate);
    	mvo.setNEXT_DLVY_DE(nextDeliDate);
		
		return mvo;
	}


	@Override
	@Transactional
	public void doWritePayInfo(List<? extends ModooPayVO> voList) {
		LOGGER.info("doWritePayInfo : {}", voList.size());
		
		// 비즈톡 결제성공맵 (고객용)
		Map<String, Object> bizTalkMapOrderOkCustom = new HashMap<String, Object>();
		bizTalkMapOrderOkCustom.put("tmplatCode", BIZTALK_TAMPLATE_002);
		List<Map<String, String>> bizTalkListOrderOkCustom = new ArrayList<Map<String, String>>();
		// 비즈톡 결제성공맵 (업체용)
		Map<String, Object> bizTalkMapOrderOkCp = new HashMap<String, Object>();
		bizTalkMapOrderOkCp.put("tmplatCode", BIZTALK_TAMPLATE_017);
		List<Map<String, String>> bizTalkListOrderOkCp = new ArrayList<Map<String, String>>();
		// 비즈톡 결제실패맵
		Map<String, Object> bizTalkMapOrderFail = new HashMap<String, Object>();
		bizTalkMapOrderFail.put("tmplatCode", BIZTALK_TAMPLATE_012);
		List<Map<String, String>> bizTalkListOrderFail = new ArrayList<Map<String, String>>();
		// 비즈톡 결제해지맵
		Map<String, Object> bizTalkMapOrderTn = new HashMap<String, Object>();
		bizTalkMapOrderTn.put("tmplatCode", BIZTALK_TAMPLATE_005);
		List<Map<String, String>> bizTalkListOrderTn = new ArrayList<Map<String, String>>();
		
		try {
			// 기존 결제결과 DB update
			for(ModooPayVO mvo : voList) {
				
				// 주문상태가 구독중이 아니고 취소/해지 접수중 이거나 구독해지, 또는 구독취소일 경우 현재 결제상태만 업데이트 후 더이상진행하지 않는다.
				if(mvo.getORDER_STTUS_CODE() == null
						|| mvo.getORDER_STTUS_CODE().equalsIgnoreCase("ST04")
						|| mvo.getORDER_STTUS_CODE().equalsIgnoreCase("ST99")) {
					payDao.updateSTN_ORDER_SETLE(mvo);
					
					LOGGER.info("[ " + mvo.getORDER_NO() + " ] doWritePayInfo canceled : {}", mvo.getORDER_STTUS_CODE());
					
					continue;
				}


				Integer successCnt = payDao.selectSuccessOrderCnt(mvo.getORDER_NO());
				//수강권 상품 종료 날짜가 지나거나 결제 횟수가 쿠폰유효날짜수 보다 같거나 많을때 자동으로 쿠폰 구독해지
				if((mvo.getCOUPON_DT_END_AT() != null
						&& "Y".equals(mvo.getCOUPON_DT_END_AT()))
					|| (!StringUtils.isEmpty(mvo.getCOUPON_VALID_PD())
						&& successCnt >= Integer.valueOf(mvo.getCOUPON_VALID_PD()))){
					mvo.setORDER_STTUS_CODE("ST04");
					mvo.setORDER_REQ_STTUS_CODE("T");
					mvo.setSETLE_STTUS_CODE("T");
					mvo.setREQ_TY_CODE("T02");

					payDao.updateSTN_ORDER_FAIL(mvo);
					payDao.updateSTN_ORDER_DLVY_FAIL(mvo);
					payDao.updateSTN_ORDER_SETLE_FAIL(mvo);
					//수강권 쿠폰 해지
					if(mvo.getVCH_CODE()!=null||!StringUtils.isEmpty(mvo.getVCH_CODE())){
						payDao.updateSTN_COUPON_FAIL(mvo);
					}
					LOGGER.info("[ " + mvo.getORDER_NO() + " ] couponEndDtUpdate couponNo: {}", mvo.getCOUPON_NO());

					continue;
				}
				
				// 기존결제상태가  실패일 경우, 다음 예약결제는 대기상태로 바꿔논다. 			
	//			if(mvo.getSETLE_STTUS_CODE().equalsIgnoreCase("F")) {
	//				mvo.setSETLE_STTUS_CODE("R");
	//				mvo.setSETLE_RESULT_CODE("");
	//				mvo.setSETLE_RESULT_MSSAGE("");
	//				mvo.addOdrNum();
	//			}
				
				// 2020.11.12 수정 
				// 기존결제상태가 실패일 경우
				// 결제실패 알림톡을 발송한다.
				// 3일에 걸쳐 (하루한번) 3번 재시도 한뒤, 3번 재시도 후에도 결제가 실패되면 자동 해지시킨다.
				if(mvo.getSETLE_STTUS_CODE().equalsIgnoreCase("F")) {
					
					//실패횟수가 최대치를 초과하면 자동 구독해지
					Integer failCnt = mvo.getSETLE_FAIL_CNT();
					if(failCnt == null) {
						failCnt = 0;
					}
					failCnt++;
					mvo.setSETLE_FAIL_CNT(failCnt);
					
					//자동구독해지
					if(failCnt > paymentFailCntMax) {					
						mvo.setORDER_STTUS_CODE("ST04");
						mvo.setORDER_REQ_STTUS_CODE("T");
						mvo.setSETLE_STTUS_CODE("T");
						mvo.setREQ_TY_CODE("T02");
						
						// 주문정보 테이블 구독해지 업데이트
						payDao.updateSTN_ORDER_FAIL(mvo);

						//수강권 쿠폰 해지
						if(mvo.getVCH_CODE()!=null||!StringUtils.isEmpty(mvo.getVCH_CODE())){
							payDao.updateSTN_COUPON_FAIL(mvo);
							LOGGER.info("[ " + mvo.getORDER_NO() + " ] couponFailUpdate couponNo: {}", mvo.getCOUPON_NO());
						}
						
						// 주문배송 테이블 구독해지 업데이트
						payDao.updateSTN_ORDER_DLVY_FAIL(mvo);	
											
						// 주문해지 알림톡정보 입력
						String orderTnMsg = mvo.getORDRR_TELNO() + 
								"@@@" + 
								"[모두의구독] 구독이 해지되었습니다." + 
								"\n* 상품명 : " + 
								mvo.getGOODS_NM();
						
						HashMap<String, String> tNparam = new HashMap<String, String>();
						tNparam.put("param", orderTnMsg);
						
						bizTalkListOrderTn.add(tNparam);
					} else {
						// 주문실패 알림톡정보 입력
						String orderFailMsg = mvo.getORDRR_TELNO() + 
								"@@@" + 
								"[모두의구독] 주문하신 결제가 정상적으로 이루어지지 않았습니다.\n" +
								"'마이페이지>구독변경'에서 결제 수단을 변경해주세요.\n" +
								"3일 경과 시, 해당 상품 구독이 해지됩니다." + 
								"\n* 상품명 : " + 
								mvo.getGOODS_NM();
						
						HashMap<String, String> failParam = new HashMap<String, String>();
						failParam.put("param", orderFailMsg);
						
						bizTalkListOrderFail.add(failParam);
					}	
					
					// 결제이력 테이블 결제실패 업데이트
					payDao.updateSTN_ORDER_SETLE_FAIL(mvo);					
					
					continue;
				}			
				
				// 현재까지 결제상태 결제이력 테이블 업데이트		
				payDao.updateSTN_ORDER_SETLE(mvo);
						
				// 다음 결제정보 입력
				// 예약결제상태가 건너뛰기이면 특수 결과코드를 입력한다.
				if(mvo.getSETLE_STTUS_CODE().equalsIgnoreCase("P") || mvo.getORDER_STTUS_CODE().equalsIgnoreCase("ST03")) {
					mvo.setSETLE_STTUS_CODE("R");
					mvo.setSETLE_RESULT_CODE(PayException.ERR_CODE_R999);
					mvo.setSETLE_RESULT_MSSAGE("구독 건너뛰기");
					mvo.addOdrNum();
				}
				
				// 기존결제상태가 성공일경우, 다음 예약결제를 대기상태로 두고 차수를 올린다.
				// 자동결제 성공 알림톡 발송한다.
				if(mvo.getSETLE_STTUS_CODE().equalsIgnoreCase("S")) {
					
					//다음 정산날짜 업데이트
					payDao.updateEXCCLC_PRARNDE(mvo);
					
					//주문성공 알림톡발송 (고객용)
					String orderOkMsgCustom = mvo.getORDRR_TELNO() + 
							"@@@" + 
							"[모두의구독] 주문이 완료되었습니다." + 
							"\n* 주문금액 : " + mvo.getSLE_AMOUNT() + " 원" +
							"\n* 상품명 : " + mvo.getGOODS_NM() + " ( " + mvo.getORDER_ODR() + " 회차)";											
					HashMap<String, String> okParamCustom = new HashMap<String, String>();
					okParamCustom.put("param", orderOkMsgCustom);					
					bizTalkListOrderOkCustom.add(okParamCustom);
					
					//주문성공시간
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					Date date = new Date(mvo.getTIMESTAMP().longValue());
					String biztalkTimeStamp = sdf.format(date);	
					
					//주문성공 알림톡발송 (업체용)
					String orderOkMsgCp = mvo.getCHARGER_TELNO() + 
							"@@@" + 
							"[모두의구독]주문이 접수되었습니다." 
							+ "\n주문 확인 후 상품 배송 부탁드립니다. "
							+ "\n주문일시 : " + biztalkTimeStamp
							+ "\n주문번호 : " + mvo.getORDER_NO()
							+ "\n상품명 : " + mvo.getGOODS_NM() + " ( " + mvo.getORDER_ODR() + " 회차)";											
					HashMap<String, String> okParamCp = new HashMap<String, String>();
					okParamCp.put("param", orderOkMsgCp);					
					bizTalkListOrderOkCp.add(okParamCp);					
					
					mvo.setSETLE_STTUS_CODE("R");
					mvo.setSETLE_RESULT_CODE("");
					mvo.setSETLE_RESULT_MSSAGE("");	
					mvo.addOdrNum();
				}	
				
				// 주문정보 테이블 업데이트 (증가된 차수)
				payDao.updateSTN_ORDER(mvo);
				
				// 주문배송 테이블 상태코드 업데이트
				payDao.updateSTN_ORDER_DLVY(mvo);
				
				// 새로 입력한 주문결제 테이블의 PK를 알아내기 위한 임시키 발행
				String tempKey = NumberUtil.numberGen(4) + System.currentTimeMillis();			
				mvo.setTEMP_KEY(tempKey);
				
				// 다음 예약결제정보 입력
				payDao.insertNextSTN_ORDER_SETLE(mvo);
				
				// 다음 주문배송 PK 조회 및 세팅
				BigInteger nextOrderSetleNo = payDao.selectNextSTN_ORDER_PK(tempKey);
				mvo.setORDER_SETLE_NO(nextOrderSetleNo);
				
				LOGGER.info("nextOrderSetleNo : {} , {}", tempKey, nextOrderSetleNo);
				
				// 다음 주문배송정보 입력				
				mvo.setORDER_REQ_STTUS_CODE("W");
				payDao.insertNextSTN_ORDER_DLVY(mvo);	
				
				LOGGER.info("DLVY_RESULT : {} ", mvo.getDLVY_RESULT());
			}	
		}catch(Exception e) {
			LOGGER.error("doWritePayInfo ERROR : {}",e);
		}
		
		List<Map<String, Object>> bizTalkList = null;
		
		try {
			bizTalkMapOrderOkCustom.put("paramList", bizTalkListOrderOkCustom);
			bizTalkMapOrderOkCp.put("paramList", bizTalkListOrderOkCp);
			bizTalkMapOrderFail.put("paramList", bizTalkListOrderFail);
			bizTalkMapOrderTn.put("paramList", bizTalkListOrderTn);
			
			bizTalkList = new ArrayList<Map<String, Object>>();
			bizTalkList.add(bizTalkMapOrderOkCustom);
			bizTalkList.add(bizTalkMapOrderOkCp);
			bizTalkList.add(bizTalkMapOrderFail);
			bizTalkList.add(bizTalkMapOrderTn);
		} catch(Exception e) {
			LOGGER.error("doWritePayInfo ERROR : {}", e);
		}
		
		if(biztalkEnable && bizTalkList != null) {
			sendBizTalk(bizTalkList);
		}
	}	
	
	private void sendBizTalk(List<Map<String, Object>> bizTalkList) {
		LOGGER.info("sendBizTalk START : {}", bizTalkList.size());
		
		try {
			for(Map<String, Object> bizTalk : bizTalkList) {
				
				List<String> paramList = ((List<String>)bizTalk.get("paramList"));
				
				if(paramList == null || paramList.size() == 0) {
					continue;
				}
				
				String bizTalkJsonStr = JsonUtil.getJsonStringFromMap(bizTalk);
				
				HttpPost httpPost = new HttpPost(biztalkUrl);
				
				StringEntity requestEntity = new StringEntity(
						bizTalkJsonStr,
					    ContentType.APPLICATION_JSON);
						 
				httpPost.setEntity(requestEntity);
				 
				CloseableHttpClient httpClient = HttpClientBuilder.create().build();
				CloseableHttpResponse response = httpClient.execute(httpPost);
				 
			    ResponseHandler<String> handler = new BasicResponseHandler();
		        String body = handler.handleResponse(response);
		        		        	        		
		        int statusCode = response.getStatusLine().getStatusCode();

		        if(statusCode == 200){
		        	Map<String, String> resultMap = new Gson().fromJson(body, new TypeToken<HashMap<String, String>>(){}.getType());	
			    	
			    	String successYn = resultMap.get("successYn");
			    	String resultMsg = resultMap.get("msg");
			    			    	  
			    	LOGGER.info("statusCode : " + successYn + " , resultMsg : " + resultMsg);
			    }
			}
		} catch(Exception e) {
			LOGGER.error("sendBizTalk ERROR : {}", e);
		}
	}
	
}
