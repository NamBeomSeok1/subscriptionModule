package modoo.ext.module.payment.exception;

public class PayException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2768935966515673330L;
	
	// 구독 중지로 인해 결제하지 않음
	public static final String ERR_CODE_R999 = "R999";
	
	// 성공
	public static final String ERR_CODE_R000 = "R000";
	
	// 이니시스 빌링 승인 API 요청 결과 에러
	public static final String ERR_CODE_R001 = "R001";
	
	// 이니시스 빌링 승인 API HTTP 통신 에러
	public static final String ERR_CODE_R002 = "R002";
	
	// 이니시스 빌링 승인 API 기타 에러
	public static final String ERR_CODE_R003 = "R003";
	
	
	public static final String ERR_CODE_R004 = "R004";
	public static final String ERR_CODE_R005 = "R005";
	public static final String ERR_CODE_R006 = "R006";
	public static final String ERR_CODE_R007 = "R007";
	
	// 빌키가 존재하지 않음
	public static final String ERR_CODE_R008 = "R008";
	
	// 구독취소(or 해지) 로 인해 더이상 결제하지 않음
	public static final String ERR_CODE_R009 = "R009";
	
	// 이지웰 UserKey 가 비어있음
	public static final String ERR_CODE_R010 = "R010";
	
	public static final String ERR_CODE_R011 = "R011";
	public static final String ERR_CODE_R012 = "R012";
	public static final String ERR_CODE_R013 = "R013";
	
	// 이지웰 선차감 API 통신 에러
	public static final String ERR_CODE_R014 = "R014";
	
	// 이지웰 선차감 API 기타 에러
	public static final String ERR_CODE_R015 = "R015";
	
	// 이지웰 선차감 API 이지웰 주문번호 생성되지 않음
	public static final String ERR_CODE_R016 = "R016";
	
	// 이지웰 선차감 API 결과 파싱에러
	public static final String ERR_CODE_R017 = "R017";
	
	// 이지웰 주문 확정 API 통신 에러
	public static final String ERR_CODE_R018 = "R018";
	
	// 이지웰 주문 확정 API 결과 에러 (주문확정이 되지 않음. CS로 해결)
	public static final String ERR_CODE_R019 = "R019";
	
	// 이지웰 포인트 조회 API 결과 오류	
	public static final String ERR_CODE_R020 = "R020";

	// 이제너두 포인트 조회 API 결과 오류
	public static final String ERR_CODE_R021 = "R021";

	// 이제너두 결제 API 통신 오류
	public static final String ERR_CODE_R022 = "R022";

	// 이제너두 결제 API 결제 실패
	public static final String ERR_CODE_R023 = "R023";

	public static final String ERR_CODE_R024 = "R024";
	public static final String ERR_CODE_R025 = "R025";
	public static final String ERR_CODE_R026 = "R026";
	public static final String ERR_CODE_R027 = "R027";
	public static final String ERR_CODE_R028 = "R028";
	public static final String ERR_CODE_R029 = "R029";
	public static final String ERR_CODE_R030 = "R030";
}
