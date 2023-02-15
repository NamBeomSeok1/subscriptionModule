package modoo.ext.module.payment.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * 모두의구독 정기결제 기본정보 VO
 * @author Hanwon
 *
 */
@Getter
@Setter
public class LogVO {


    //주문에러로그번호
    private Integer orderErrorLogNo;
    //주문번호
    private String orderNo;
    //주문그룹번호
    private String orderGroupNo;
    //주문자ID
    private String ordrrId;
    //주문자이름
    private String ordrrNm;
    //주문회차
    private Integer orderTurn;
    //주문에러코드
    private String errorCode;
    //에러메시지
    private String errorMsg;
    //로그생성날짜
    private Date creatDt;

    @Override
    public String toString() {
        return "LogVO{" +
                "orderErrorLogNo=" + orderErrorLogNo +
                ", orderNo='" + orderNo + '\'' +
                ", orderGroupNo='" + orderGroupNo + '\'' +
                ", ordrrId='" + ordrrId + '\'' +
                ", ordrrNm='" + ordrrNm + '\'' +
                ", orderTurn='" + orderTurn + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", creatDt=" + creatDt +
                '}';
    }
}
