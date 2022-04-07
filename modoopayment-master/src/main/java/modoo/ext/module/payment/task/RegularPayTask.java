package modoo.ext.module.payment.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import modoo.ext.module.payment.config.RegularPayJobConfig;

@RequiredArgsConstructor
@Component
public class RegularPayTask {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RegularPayTask.class);
	
	private final JobLauncher jobLauncher;
	private final Job regularPay_job;	
		
	@Value("${regularpay.scheduler.enable.second}")
	private boolean secondEnable;
	
	
	private JobParameters createRegularJobParameterMap() {
		return new JobParametersBuilder()
                .addString("createDate", "" + System.currentTimeMillis())
                .addString("payStatus", "0")
                .toJobParameters();
    }
	
	private JobParameters createRegularJobParameterMapSecond() {
		return new JobParametersBuilder()
                .addString("createDate", "" + System.currentTimeMillis())
                .addString("payStatus", "1")
                .toJobParameters();
    }
	
	
	@Scheduled(cron = "${regularpay.scheduler.cron}")
	public void regularPay() {
		LOGGER.info("selectRegularPay START 1");
		try {			
			if(!RegularPayJobConfig.jobRunning) {
				JobParameters jobParameters = createRegularJobParameterMap();
				jobLauncher.run(regularPay_job, jobParameters);
			} else {
				LOGGER.info("*** The job already running...this task will be skipped. ***");
			}
		} catch(Exception e) {
			LOGGER.error("selectRegularPay - ERROR : {}", e);
		}
	}
	
	
	@Scheduled(cron = "${regularpay.scheduler.cron.second}")
	public void regularPaySecond() {
		if(!secondEnable) {
//			LOGGER.info("regularPaySecond DISABLED");
			return;
		}		
		
		LOGGER.info("regularPaySecond START");
		
		try {			
			if(!RegularPayJobConfig.jobRunningSecond) {
				JobParameters jobParameters = createRegularJobParameterMapSecond();
				jobLauncher.run(regularPay_job, jobParameters);
			} else {
				LOGGER.info("*** The job already running...this task will be skipped. ***");
			}
		} catch(Exception e) {
			LOGGER.error("regularPaySecond - ERROR : {}", e);
		}
	}
}
