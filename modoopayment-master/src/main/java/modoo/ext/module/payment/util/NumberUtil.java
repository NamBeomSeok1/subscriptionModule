package modoo.ext.module.payment.util;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NumberUtil.class);
	
	public static int parseInt(String number) {
		
		int result = 0;
		try {
			result = Integer.parseInt(number);			
		}catch(Exception e) {
			LOGGER.error("NumberUtil - parseInt ERROR : {}", e);
			result = 0;
		}
		return result;
	}
	
	public static long parseLong(String number) {
		
		long result = 0;
		try {
			result = Long.parseLong(number);			
		}catch(Exception e) {
			LOGGER.error("NumberUtil - parseLong ERROR : {}", e);
			result = 0;
		}
		return result;
	}
	
	public static String numberGen(int len) {
        
        Random rand = new Random();
        String numStr = ""; //난수가 저장될 변수
        
        for(int i=0;i<len;i++) {            
            //0~9 까지 난수 생성
            String ran = Integer.toString(rand.nextInt(10));            
            numStr += ran;
        }
        return numStr;
    }
}
