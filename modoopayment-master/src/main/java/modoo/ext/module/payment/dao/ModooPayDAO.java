package modoo.ext.module.payment.dao;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import modoo.ext.module.payment.vo.ModooPayVO;

@Repository
public class ModooPayDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ModooPayDAO.class);
		
	protected static final String NAMESPACE = "modoo.ext.module.payment.dao.";
	
	@Autowired
	private SqlSession sqlSession;
	
	public void updateSTN_ORDER_SETLE(ModooPayVO mvo){	
		LOGGER.info("updateSTN_ORDER_SETLE mvo : {}", mvo.getORDER_NO());
		sqlSession.update(NAMESPACE + "updateSTN_ORDER_SETLE", mvo);
	}
	
	public void updateEXCCLC_PRARNDE(ModooPayVO mvo){	
		LOGGER.info("updateEXCCLC_PRARNDE mvo : {}", mvo.getORDER_NO());
		sqlSession.update(NAMESPACE + "updateEXCCLC_PRARNDE", mvo);
	}
	
	public void updateSTN_ORDER(ModooPayVO mvo){	
		LOGGER.info("updateSTN_ORDER mvo : {}", mvo.getORDER_NO());
		sqlSession.update(NAMESPACE + "updateSTN_ORDER", mvo);
	}

	public void updateSTN_COUPON_FAIL(ModooPayVO mvo) {
		LOGGER.info("updateSTN_COUPON_FAIL: {}", mvo.getORDER_NO());
		sqlSession.update(NAMESPACE + "updateSTN_COUPON_FAIL", mvo);
	}
	
	public void insertNextSTN_ORDER_SETLE(ModooPayVO mvo){	
		LOGGER.info("insertNextSTN_ORDER_SETLE mvo : {}", mvo.getORDER_NO());
		sqlSession.insert(NAMESPACE + "insertNextSTN_ORDER_SETLE", mvo);
	}
	
	public void insertNextSTN_ORDER_DLVY(ModooPayVO mvo){	
		LOGGER.info("insertNextSTN_ORDER_DLVY mvo : {}", mvo.getORDER_NO());
		sqlSession.insert(NAMESPACE + "insertNextSTN_ORDER_DLVY", mvo);
	}
	
	public BigInteger selectNextId() {
		return sqlSession.selectOne(NAMESPACE + "selectNextId");
	}
	
	public BigInteger selectNextSTN_ORDER_PK(String tempKey) {
		return sqlSession.selectOne(NAMESPACE + "selectNextSTN_ORDER_PK", tempKey);
	}
	
	public void updateDteSeq(BigInteger nextId) {
		sqlSession.update(NAMESPACE + "updateDteSeq", nextId);
	}
	
	public void updateSTN_ORDER_DLVY(ModooPayVO mvo) {
		sqlSession.update(NAMESPACE + "updateSTN_ORDER_DLVY", mvo);
	}
	
	public void updateSTN_ORDER_FAIL(ModooPayVO mvo) {
		sqlSession.update(NAMESPACE + "updateSTN_ORDER_FAIL", mvo);
	}
	
	public void updateSTN_ORDER_DLVY_FAIL(ModooPayVO mvo) {
		sqlSession.update(NAMESPACE + "updateSTN_ORDER_DLVY_FAIL", mvo);
	}
	
	public void updateSTN_ORDER_SETLE_FAIL(ModooPayVO mvo) {
		sqlSession.update(NAMESPACE + "updateSTN_ORDER_SETLE_FAIL", mvo);
	}
}
