package modoo.ext.module.payment.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TextUtil.class);
	
	public static boolean isEmpty(String str) {
		
		if(str == null)
			return true;
		
		if(str.isEmpty())
			return true;
		else
			return false;
	}	
}
