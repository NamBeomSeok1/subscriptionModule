package modoo.ext.module.payment.service;

import java.util.List;

import modoo.ext.module.payment.vo.ModooPayVO;

public interface RegularPayService {
		
	public ModooPayVO doRegularPay(ModooPayVO mvo);
	
	public ModooPayVO doInicisRegularPay(ModooPayVO mvo);
	
	public ModooPayVO doExanaduPoint(ModooPayVO mvo);

	public ModooPayVO doExanaduPay(ModooPayVO mvo);

	public ModooPayVO doEzwelPoint(ModooPayVO mvo);

	public ModooPayVO doEzwelPay(ModooPayVO mvo);

	public ModooPayVO getNextPayDate(ModooPayVO mvo);
	
	public void doWritePayInfo(List<? extends ModooPayVO> voList);
}
