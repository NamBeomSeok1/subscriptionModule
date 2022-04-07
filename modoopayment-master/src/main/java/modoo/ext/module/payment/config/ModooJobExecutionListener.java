package modoo.ext.module.payment.config;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public interface ModooJobExecutionListener extends JobExecutionListener{
	@Override
	void beforeJob(JobExecution jobExecution);
	@Override
	void afterJob(JobExecution jobExecution);
}
