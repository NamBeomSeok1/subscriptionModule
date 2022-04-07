package modoo.ext.module.payment.vo;

import java.math.BigDecimal;
import java.math.BigInteger;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 모두의구독 정기결제 기본정보 VO
 * @author Hanwon
 *
 */
@Getter
@Setter
public class ModooPayVO{
	
	/**
	 * 결제이력테이블에서 가져오는 값
	 */
	// 주문결제고유번호
	private BigInteger ORDER_SETLE_NO = null;
	
	// 결제금액
	private BigInteger SETLE_TOT_AMOUNT = null;
	
	// 결제포인트
	private BigInteger SETLE_POINT = null;
	
	// 카드결제금액
	private BigInteger SETLE_CARD_AMOUNT = null;
		
	// 결제종류코드       EZP : 이지웰포인트  CARD : 일반카드결제
	private String SETLE_TY_CODE = null;
	
	// 결제상태코드      R : 결제대기   S : 결제완료   F : 결제실패  C : 결제취소  T : 구독해지
	private String SETLE_STTUS_CODE = null;
	
	// 결제결과코드
	private String SETLE_RESULT_CODE = null;
	
	// 결제결과메세지
	private String SETLE_RESULT_MSSAGE = null;
		
	// 이지웰결제승인번호
	private String EZW_SETLE_CONFM_NO = null;
	
	// 이니시스결제승인번호
	private String INI_SETLE_CONFM_NO = null;	
	
	// 다음 결제일
	private String SETLE_PRARNDE = null;
	
	// 결제결과종류코드 (EZP,CARD,EZCD,FREE)
	private String SETLE_RESULT_TY_CODE = null;
	
	// 결제실패횟수
	private Integer SETLE_FAIL_CNT = 0;
	
	// 주문배송정보 입력을 위한 임시키값
	private String TEMP_KEY = null;	
	
		
	/**
	 * 주문배송테이블에서 가져오는 값
	 */
	// 배송일
	private String HDRY_DLVY_DE = null;	
	
	// 차수
	private Integer ORDER_ODR = null;
	
	// 배송지 우편번호
	private String DLVY_ZIP = null;
	
	// 배송지주소
	private String DLVY_ADRES = null;
	
	// 배송지연락처
//	private String TELNO = null;
	
	// 배송지상세주소
	private String DLVY_ADRES_DETAIL = null;
	
	// 배송요청메세지
	private String DLVY_MSSAGE = null;	
	
	// 주문요청상태 코드  W:주문대기  O:주문  C:취소  E:교환  R:반품
	private String ORDER_REQ_STTUS_CODE = null;
	
	// 판매금액
	private BigInteger SLE_AMOUNT = null;
	
	// 할인금액
	private BigInteger DSCNT_AMOUNT = null;
	
	// 결제금액 (판매금액 - 할인금액)
	private BigInteger SETLE_AMOUNT = null;
	
	// 구독옵션정보
	private String ORDER_INFO = null;
	
	// 입력전용
	// 요청유형코드
	private String REQ_TY_CODE = null;
			
	
	/**
	 * 주문테이블에서 가져오는 값
	 */
	// 주문고유번호
	private String ORDER_NO = null;
	
	// 주문그룹고유번호
	private BigDecimal ORDER_GROUP_NO = BigDecimal.ZERO;
		
	// 주문자 ID
	private String ORDRR_ID = null;
	
	// 주문자명
	private String ORDRR_NM = null;
	
	// 상품고유ID
	private String GOODS_ID = null;
		
	// 판매단가 (상품금액-배송비제외)
	private BigInteger GOODS_AMOUNT = null;
	
	// 결제총금액
	private BigInteger TOT_AMOUNT = null;

	// 결제배송금액
	private BigInteger DLVY_AMOUNT = null;
			
	// 주문구분코드    SBS : 구독   GNRL : 일반
	private String ORDER_KND_CODE = null;
	
	// 구독주기구분코드   WEEK : 주   MONTH : 월
	private String SBSCRPT_CYCLE_SE_CODE;
	
	// 구독주기_주
	private Integer SBSCRPT_WEEK_CYCLE = 0;
	
	// 구독주기_월
	private Integer SBSCRPT_MT_CYCLE = 0;
	
	// 구독일
	private Integer SBSCRPT_DLVY_DAY = 0;
	
	// 주문자 전화번호
	private String ORDRR_TELNO = null;
	
	// 주문자 핸드폰번호
//	private String ORDRR_MOBINO = null;
	
	// 주문자 이메일
	private String ORDRR_EMAIL = null;
		
	// 수령자 이름
	private String DLVY_USER_NM = null;
	
	// 수령자 전화번호
	private String DLVY_TELNO = null;
	
	// 수령자 휴대전화
	private String DLVY_CELNO = null;
	
	// 주문시 요청사항
	private String ORDER_REQUEST = null;
	
	// 포인트 사용여부 (이지웰)
	private String POINT_YN = "Y";
	
	// 적립금 사용여부 (이지웰)
	private String EZMIL_YN = "N";
	
	// 특별포인트 사용여부 (이지웰)
	private String SPECIAL_USE_YN = "N";
	
	// 결제수단별 현금영수증발행 (이지웰)
	private String RECEIPT_YN = "YYYY";
	
	// 주문자 유저키 (이지웰)
	private String USER_KEY = null;
				
	// 고객사코드 (이지웰)
	private String CLIENT_CD = null;
	
	// 주문자 빌키 (이니시스)
	private String BILL_KEY = null;
	
	// 주문수량
	private Integer ORDER_CO = 0;
	
	// 주문상태코드
	private String ORDER_STTUS_CODE = null;
			
	
	/**
	 * 상품테이블에서 가져오는 값
	 */
	// 상품명
	private String GOODS_NM = null;

	// 상품공급가
	private java.math.BigDecimal GOODS_SPLPC = null;

	// 상품 과세 구분
	private String TAXT_SE_CODE = null;
	
	// 수수료 % default 15%
	private Integer GOODS_CHARGE_PT = 15;

	// 상품 쿠폰 코드
	private String VCH_CODE = null;


	/**
	 * 쿠폰테이블에서 가져오는 값
	 */
	// 쿠폰번호
	private String COUPON_NO = null;



	/**
	 * 제휴사 테이블에서 가져오는 값
	 */
	// 결제제휴사고유ID
	private String PRTNR_ID = null;
	
	/**
	 * 업체 테이블에서 가져오는 값
	 */
	private String CHARGER_TELNO = null;
	
	/**
	 * 기타
	 */	
	// 결제전문생성일
	private BigDecimal TIMESTAMP;		
	
	// 다음배송일
	private String NEXT_DLVY_DE = null;	
	
	// 주문결제 insert 결과
	private Integer DLVY_RESULT = 0;	
	
	// 이니시스결제 결과
	private Integer INICIS_PAY_RESULT = 0;
	
	// 차수증가
	public void addOdrNum() {
		this.ORDER_ODR++;
	}

	@Override
	public String toString() {
		return "ModooPayVO{" +
				"ORDER_SETLE_NO=" + ORDER_SETLE_NO +
				", SETLE_TOT_AMOUNT=" + SETLE_TOT_AMOUNT +
				", SETLE_POINT=" + SETLE_POINT +
				", SETLE_CARD_AMOUNT=" + SETLE_CARD_AMOUNT +
				", SETLE_TY_CODE='" + SETLE_TY_CODE + '\'' +
				", SETLE_STTUS_CODE='" + SETLE_STTUS_CODE + '\'' +
				", SETLE_RESULT_CODE='" + SETLE_RESULT_CODE + '\'' +
				", SETLE_RESULT_MSSAGE='" + SETLE_RESULT_MSSAGE + '\'' +
				", EZW_SETLE_CONFM_NO='" + EZW_SETLE_CONFM_NO + '\'' +
				", INI_SETLE_CONFM_NO='" + INI_SETLE_CONFM_NO + '\'' +
				", SETLE_PRARNDE='" + SETLE_PRARNDE + '\'' +
				", SETLE_RESULT_TY_CODE='" + SETLE_RESULT_TY_CODE + '\'' +
				", SETLE_FAIL_CNT=" + SETLE_FAIL_CNT +
				", TEMP_KEY='" + TEMP_KEY + '\'' +
				", HDRY_DLVY_DE='" + HDRY_DLVY_DE + '\'' +
				", ORDER_ODR=" + ORDER_ODR +
				", DLVY_ZIP='" + DLVY_ZIP + '\'' +
				", DLVY_ADRES='" + DLVY_ADRES + '\'' +
				", DLVY_ADRES_DETAIL='" + DLVY_ADRES_DETAIL + '\'' +
				", DLVY_MSSAGE='" + DLVY_MSSAGE + '\'' +
				", ORDER_REQ_STTUS_CODE='" + ORDER_REQ_STTUS_CODE + '\'' +
				", SLE_AMOUNT=" + SLE_AMOUNT +
				", DSCNT_AMOUNT=" + DSCNT_AMOUNT +
				", SETLE_AMOUNT=" + SETLE_AMOUNT +
				", ORDER_INFO='" + ORDER_INFO + '\'' +
				", REQ_TY_CODE='" + REQ_TY_CODE + '\'' +
				", ORDER_NO='" + ORDER_NO + '\'' +
				", ORDER_GROUP_NO=" + ORDER_GROUP_NO +
				", ORDRR_ID='" + ORDRR_ID + '\'' +
				", ORDRR_NM='" + ORDRR_NM + '\'' +
				", GOODS_ID='" + GOODS_ID + '\'' +
				", GOODS_AMOUNT=" + GOODS_AMOUNT +
				", TOT_AMOUNT=" + TOT_AMOUNT +
				", DLVY_AMOUNT=" + DLVY_AMOUNT +
				", ORDER_KND_CODE='" + ORDER_KND_CODE + '\'' +
				", SBSCRPT_CYCLE_SE_CODE='" + SBSCRPT_CYCLE_SE_CODE + '\'' +
				", SBSCRPT_WEEK_CYCLE=" + SBSCRPT_WEEK_CYCLE +
				", SBSCRPT_MT_CYCLE=" + SBSCRPT_MT_CYCLE +
				", SBSCRPT_DLVY_DAY=" + SBSCRPT_DLVY_DAY +
				", ORDRR_TELNO='" + ORDRR_TELNO + '\'' +
				", ORDRR_EMAIL='" + ORDRR_EMAIL + '\'' +
				", DLVY_USER_NM='" + DLVY_USER_NM + '\'' +
				", DLVY_TELNO='" + DLVY_TELNO + '\'' +
				", DLVY_CELNO='" + DLVY_CELNO + '\'' +
				", ORDER_REQUEST='" + ORDER_REQUEST + '\'' +
				", POINT_YN='" + POINT_YN + '\'' +
				", EZMIL_YN='" + EZMIL_YN + '\'' +
				", SPECIAL_USE_YN='" + SPECIAL_USE_YN + '\'' +
				", RECEIPT_YN='" + RECEIPT_YN + '\'' +
				", USER_KEY='" + USER_KEY + '\'' +
				", CLIENT_CD='" + CLIENT_CD + '\'' +
				", BILL_KEY='" + BILL_KEY + '\'' +
				", ORDER_CO=" + ORDER_CO +
				", ORDER_STTUS_CODE='" + ORDER_STTUS_CODE + '\'' +
				", GOODS_NM='" + GOODS_NM + '\'' +
				", GOODS_SPLPC=" + GOODS_SPLPC +
				", TAXT_SE_CODE='" + TAXT_SE_CODE + '\'' +
				", GOODS_CHARGE_PT=" + GOODS_CHARGE_PT +
				", PRTNR_ID='" + PRTNR_ID + '\'' +
				", CHARGER_TELNO='" + CHARGER_TELNO + '\'' +
				", TIMESTAMP=" + TIMESTAMP +
				", NEXT_DLVY_DE='" + NEXT_DLVY_DE + '\'' +
				", DLVY_RESULT=" + DLVY_RESULT +
				", INICIS_PAY_RESULT=" + INICIS_PAY_RESULT +
				'}';
	}
}
