package modoo.ext.module.payment.util;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
	public static String getJsonStringFromMap( Map<String, Object> map ) throws JsonProcessingException{    	
    	String jsonString = null;
    	ObjectMapper mapper = new ObjectMapper();  
    	jsonString = mapper.writeValueAsString(map);
        return jsonString;
    }
	
	public static String getJsonStringFromObject( Object obj ) throws JsonProcessingException{    	
    	String jsonString = null;
    	ObjectMapper mapper = new ObjectMapper();  
    	jsonString = mapper.writeValueAsString(obj);
        return jsonString;
    }
}
