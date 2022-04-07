package modoo.ext.module.payment.config;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegularPayJobParameters {
	
    
    private String createDate;
    
    @Value("#{jobParameters[payStatus]}")
    private String payStatus;
    
    @Value("#{jobParameters[createDate]}")
    public void setCreateDate(String todaymills) {
    	if(todaymills == null || todaymills.isEmpty())
    		return;
    	Date date = new Date(Long.parseLong(todaymills));
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");    	
        this.createDate = df2.format(date);
	}
}
