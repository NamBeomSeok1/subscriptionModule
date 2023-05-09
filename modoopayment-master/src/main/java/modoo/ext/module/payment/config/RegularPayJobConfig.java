package modoo.ext.module.payment.config;

import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.RequiredArgsConstructor;
import modoo.ext.module.payment.service.impl.RegularPayServiceImpl;
import modoo.ext.module.payment.vo.ModooPayVO;

@Configuration
@RequiredArgsConstructor
public class RegularPayJobConfig {	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RegularPayJobConfig.class);
	private final String JOB_NAME = "regularPay";
	
	private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobRepository jobRepository;
    private final RegularPayJobParameters jobParameters;    
    
    private final RegularPayServiceImpl regularPayService;      
    
    private final int CHUNK_SIZE = 1000;
    private final int CORE_POOL_SIZE = 2;
    private final int MAX_POOL_SIZE = 8;
    
    private final DataSource dataSource;
    
    public static boolean jobRunning = false;
    public static boolean jobRunningSecond = false;
    
    
    @Bean(JOB_NAME + "jobParameters")
    @JobScope
    public RegularPayJobParameters jobParameters() {
        return new RegularPayJobParameters();
    }
     
    
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
	    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
	    taskExecutor.setCorePoolSize(CORE_POOL_SIZE);
	    taskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
	    taskExecutor.setThreadNamePrefix("multi-thread-");
	    taskExecutor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
	    taskExecutor.initialize();
	    return taskExecutor;
    }
    
    
    @Bean
    public JobLauncher jobLauncher() throws Exception {
    	LOGGER.info("************ jobLauncher start **************");
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        LOGGER.info("************ jobLauncher end **************");
        return jobLauncher;
    }
    
    
    @Bean(name=JOB_NAME + "_job")
    public Job regularPayJob() {
        return jobBuilderFactory.get(JOB_NAME + "_job")
                .preventRestart()
        		.validator(new DefaultJobParametersValidator())
        		.incrementer(new RunIdIncrementer())
                .start(regularPayStep())
                .listener(new ModooJobExecutionListener() {

					@Override
					public void beforeJob(JobExecution jobExecution) {
						LOGGER.info("************ BEFORE JOB  1**************");
						jobRunning = true;
						jobRunningSecond = true;
					}

					@Override
					public void afterJob(JobExecution jobExecution) {
						LOGGER.info("************ AFTER JOB 1**************");
						jobRunning = false;
						jobRunningSecond = false;
					}})
                .build();
    }
    
    
    @Bean(name=JOB_NAME + "_step")
    public Step regularPayStep() {    	
        return stepBuilderFactory.get(JOB_NAME + "_step")
                .<ModooPayVO, ModooPayVO> chunk(CHUNK_SIZE)
                .reader(regularPayCursorReader())
                .processor(regularPayProcessor())
                .writer(regularPayWriter())
                .taskExecutor(taskExecutor())
                .throttleLimit(MAX_POOL_SIZE)
                .build();
    }
    
    
    @BeforeStep
	public void beforeStep(StepExecution stepExecution) {
//		JobExecution jobExecution = stepExecution.getJobExecution();
//		ExecutionContext jobContext = jobExecution.getExecutionContext();
//		LOGGER.info("*********************beforeStep");
	}
    
    
    @AfterStep
	public ExitStatus afterStep(StepExecution stepExecution) {
//		if( stepExecution.getExitStatus().getExitCode() != ExitStatus.COMPLETEd.getExitCode()) {
//			LOGGER.info("**************************AfterStep");		
//		}
		return stepExecution.getExitStatus();
	}
   
    
    @Bean(name=JOB_NAME + "_reader")
    @StepScope
    public SynchronizedItemStreamReader<ModooPayVO> regularPayCursorReader() {
   	
    	if(dataSource == null) {
    		LOGGER.error("********** DATASOURCE IS NULL !!!!!! **********");
    	}
    	    	
    	JdbcCursorItemReader<ModooPayVO> jdbcItemReader = null;
    	
    	try {
	    	String payStatus = jobParameters.getPayStatus();
	    	String payJobCreateDate = jobParameters.getCreateDate();
	    	// 최초 서비스 시작할때는 task를 통해 실행되는게 아니라 파라미터를 전달받지 못한다.
	    	if(payStatus == null || payStatus.isEmpty()) {
	    		payStatus = "0";
	    	}
	    	LOGGER.info("SynchronizedItemStreamReader START : [ {} , {} ]", payStatus , payJobCreateDate);
	    		    	
	    	jdbcItemReader = new JdbcCursorItemReaderBuilder<ModooPayVO>()
	                .fetchSize(CHUNK_SIZE)
	                .dataSource(dataSource)
	                .rowMapper(new BeanPropertyRowMapper<>(ModooPayVO.class))
	                .sql(getReaderQuery(payStatus))
	                .name(JOB_NAME + "_reader")
	                .saveState(false)
	                .build();	
    	}catch(Exception e) {
    		LOGGER.error("SynchronizedItemStreamReader ERROR : {}", e);
    	}
    	return new SynchronizedItemStreamReaderBuilder<ModooPayVO>()
    			.delegate(jdbcItemReader).build();
    }
    
    
//    @Bean(name=JOB_NAME + "_reader")
//    @StepScope
//    public JdbcPagingItemReader<ModooPayVO> regularPayPagingReader() {
//   	
//    	if(dataSource == null) {
//    		LOGGER.error("********** DATASOURCE IS NULL !!!!!! **********");
//    	}
//    	    	
//    	JdbcPagingItemReader<ModooPayVO> jdbcItemReader = null;
//    	
//    	try {
//	    	String payStatus = jobParameters.getPayStatus();
//	    	
//	    	// 최초 서비스 시작할때는 task를 통해 실행되는게 아니라 파라미터를 전달받지 못한다. 이 경우 건너뛴다.
//	    	if(payStatus == null || payStatus.isEmpty()) {
//	    		return null;
//	    	}
//	    	
//	    	LOGGER.info("JdbcPagingItemReader START : [ {} ]", payStatus);
//	    	
//	    	jdbcItemReader = new JdbcPagingItemReaderBuilder<ModooPayVO>()
//	                .fetchSize(CHUNK_SIZE)
//	                .dataSource(dataSource)
//	                .rowMapper(new BeanPropertyRowMapper<>(ModooPayVO.class))
//	                .queryProvider(createQueryProvider(payStatus))
//	                .name(JOB_NAME + "_reader")
//	                .build();
//	    	
//    	}catch(Exception e) {
//    		LOGGER.error("JdbcPagingItemReader ERROR : {}", e);
//    	}
//    	return jdbcItemReader;
//    }
        
    
//    public PagingQueryProvider createQueryProvider(String payStatus) throws Exception {
//    	
//    	String selectClause = 
//			    "a.ORDER_SETLE_NO, a.SETLE_TOT_AMOUNT, a.SETLE_TY_CODE, "
//				+ "a.SETLE_STTUS_CODE, a.SETLE_RESULT_CODE, a.SETLE_RESULT_MSSAGE, "
//				+ "a.EZW_SETLE_CONFM_NO, a.INI_SETLE_CONFM_NO, a.SETLE_PNTTM, "
//				+ "a.SETLE_POINT, a.SETLE_CARD_AMOUNT, a.SETLE_PRARNDE, a.SETLE_RESULT_TY_CODE, a.SETLE_FAIL_CNT, " 
//				 
//				+ "b.ORDER_NO, b.ORDER_GROUP_NO, b.ORDRR_ID, b.ORDRR_NM, b.NOW_ODR, b.GOODS_ID, b.ORDER_KND_CODE, b.SBSCRPT_DLVY_DAY, "
//				+ "b.GOODS_AMOUNT, b.SBSCRPT_CYCLE_SE_CODE, b.SBSCRPT_WEEK_CYCLE, "
//				+ "b.SBSCRPT_MT_CYCLE, b.ORDER_CO, b.DLVY_USER_NM, b.BILL_KEY, b.NEXT_SETLEDE, b.TELNO AS ORDRR_TELNO, b.ORDER_STTUS_CODE, "
//				 
//				+ "c.PCMAPNG_ID, c.GOODS_NM, "
//				 
//				+ "d.POINT_YN, d.EZMIL_YN, d.SPECIAL_USE_YN, d.RECEIPT_YN, d.USER_KEY, "
//				+ "d.EMAIL, d.CLIENT_CD, "
//				 
//			 	+ "e.PRTNR_ID, "
//			 	 
//			 	+ "f.ORDER_ODR, f.HDRY_DLVY_DE, f.ORDER_REQ_STTUS_CODE, f.DLVY_ZIP, f.DLVY_ADRES, "
//			 	+ "f.DLVY_ADRES_DETAIL, f.DLVY_MSSAGE, f.TELNO AS DLVY_TELNO, f.SLE_AMOUNT, f.DSCNT_AMOUNT, f.ORDER_INFO ";
//    	
//    	String fromClause = 
//		    	"STN_ORDER_SETLE AS a "
//		 		+ "JOIN STN_ORDER_DLVY AS f "
//		 		+ "ON a.ORDER_SETLE_NO = f.ORDER_SETLE_NO "	 
//		 		+ "JOIN STN_ORDER AS b "
//		 		+ "ON f.ORDER_NO = b.ORDER_NO "
//		 		+ "JOIN STN_GOODS AS c "
//		 		+ "ON b.GOODS_ID = c.GOODS_ID "
//		 		+ "JOIN DTN_MBER AS d "
//		 		+ "ON b.ORDRR_ID = d.MBER_ID "
//		 		+ "JOIN STN_PRTNR_CMPNY_MAPNG AS e "
//		 		+ "ON c.PCMAPNG_ID = e.PCMAPNG_ID ";
//    	
//    	String whereClause = "";
//    	switch(payStatus) {
//    	case "0" :
//    		whereClause = 
//			    "1 = 1 " 
//			 	+ "AND a.USE_AT = 'Y' " 
//			 	+ "AND b.USE_AT = 'Y' "
//			 	+ "AND c.USE_AT = 'Y' "
//			 	+ "AND e.USE_AT = 'Y' "
//			 	+ "AND b.ORDER_KND_CODE = 'SBS' "
//			 	+ "AND a.SETLE_STTUS_CODE IN('R','P') " 
//			 	+ "AND DATE(DATE_FORMAT(a.SETLE_PRARNDE, '%Y%m%d')) = DATE(NOW()) ";
//    		break;
//    	case "1" :
//    		whereClause = 
//		    	"1 = 1 " 
//		 		+ "AND a.USE_AT = 'Y' " 
//		 		+ "AND b.USE_AT = 'Y' "
//		 		+ "AND c.USE_AT = 'Y' "
//		 		+ "AND e.USE_AT = 'Y' "
//		 		+ "AND b.ORDER_KND_CODE = 'SBS' "
//		 		+ "AND a.SETLE_STTUS_CODE = 'F' " 
//		 		+ "AND DATE(DATE_FORMAT(a.SETLE_PRARNDE, '%Y%m%d')) > DATE(NOW()) - 7";
//    		break;
//		default:
//			break;
//    	}
//
//    	SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
//        queryProvider.setDataSource(dataSource);
//        queryProvider.setSelectClause(selectClause);
//        queryProvider.setFromClause(fromClause);
//        queryProvider.setWhereClause(whereClause);
//        Map<String, Order> sortKeys = new HashMap<>(1);
//        sortKeys.put("a.ORDER_SETLE_NO", Order.ASCENDING);
//        queryProvider.setSortKeys(sortKeys);
//        return queryProvider.getObject();
//    }
    
    
    private String getReaderQuery(String payStatus) {
    	String readerSql = "";
    	switch(payStatus) {
    	case "0" :
    		//결제 대기건 조회
    		readerSql = "SELECT " 
    				+ "a.ORDER_SETLE_NO, a.SETLE_TOT_AMOUNT, a.SETLE_TY_CODE, "
    				+ "a.SETLE_STTUS_CODE, a.SETLE_RESULT_CODE, a.SETLE_RESULT_MSSAGE, "
    				+ "a.EZW_SETLE_CONFM_NO, a.INI_SETLE_CONFM_NO, a.SETLE_PNTTM, "
    				+ "a.SETLE_POINT, a.SETLE_CARD_AMOUNT, a.SETLE_PRARNDE, a.SETLE_RESULT_TY_CODE, a.SETLE_FAIL_CNT, " 
    				 
    				+ "b.ORDER_NO, b.ORDER_GROUP_NO, b.ORDRR_ID, b.ORDRR_NM, b.NOW_ODR, b.GOODS_ID, b.ORDER_KND_CODE, b.SBSCRPT_DLVY_DAY, "
    				+ "b.GOODS_AMOUNT, b.SBSCRPT_CYCLE_SE_CODE, b.SBSCRPT_WEEK_CYCLE, "
    				+ "b.SBSCRPT_MT_CYCLE, b.ORDER_CO, b.DLVY_USER_NM, b.BILL_KEY, b.NEXT_SETLEDE, b.TELNO AS ORDRR_TELNO, b.ORDER_STTUS_CODE, b.DLVY_AMOUNT,"
    				 
    				+ "c.PCMAPNG_ID, c.GOODS_NM, c.GOODS_SPLPC, c.TAXT_SE_CODE, "
					+ "c.VCH_CODE, "
    				 
    				+ "d.POINT_YN, d.EZMIL_YN, d.SPECIAL_USE_YN, d.RECEIPT_YN, d.USER_KEY, "
    				+ "d.EMAIL, d.CLIENT_CD, "
    				 
    			 	+ "e.PRTNR_ID, "
    			 	
    			 	+ "f.ORDER_ODR, f.HDRY_DLVY_DE, f.ORDER_REQ_STTUS_CODE, f.DLVY_ZIP, f.DLVY_ADRES, "
    			 	+ "f.DLVY_ADRES_DETAIL, f.DLVY_MSSAGE, f.TELNO AS DLVY_TELNO, f.SLE_AMOUNT, f.DSCNT_AMOUNT, f.ORDER_INFO, "
    			 	
    			 	+ "g.CHARGER_TELNO,"

    			 	+ "gc.COUPON_NO,"
    			 	+ "gc.COUPON_VALID_PD,"
    			 	+ "gc.COUPON_PD_TY,"
					+ " CASE WHEN DATE_FORMAT(gc.COUPON_END_PNTTM , '%Y%m%d') < DATE_FORMAT(NOW(),'%Y%m%d') THEN 'Y' ELSE 'N' END AS COUPON_DT_END_AT "
   		 	 	+ "FROM "
   		 	 		+ "STN_ORDER_SETLE AS a "
   		 	 		+ "JOIN STN_ORDER_DLVY AS f "
   		 	 		+ "ON a.ORDER_SETLE_NO = f.ORDER_SETLE_NO "	 
   		 	 		+ "JOIN STN_ORDER AS b "
   		 	 		+ "ON f.ORDER_NO = b.ORDER_NO "
   		 	 		+ "JOIN STN_GOODS AS c "
   		 	 		+ "ON b.GOODS_ID = c.GOODS_ID "
   		 	 		+ "LEFT JOIN DTN_MBER AS d "
   		 	 		+ "ON b.FRST_REGISTER_ID = d.ESNTL_ID "
   		 	 		+ "JOIN STN_PRTNR_CMPNY_MAPNG AS e "
   		 	 		+ "ON c.PCMAPNG_ID = e.PCMAPNG_ID "
   		 	 		+ "JOIN STN_CMPNY AS g "
   		 	 		+ "ON e.CMPNY_ID = g.CMPNY_ID "
   		 	 		+ "LEFT JOIN STN_GOODS_COUPON AS gc "
   		 	 		+ "ON gc.ORDER_NO = b.ORDER_NO "
   		 	 	+ "WHERE "
   		 	 		+ "1 = 1 " 
   		 	 		+ "AND a.USE_AT = 'Y' " 
   		 	 		+ "AND b.USE_AT = 'Y' "
   		 	 		+ "AND e.USE_AT = 'Y' "
   		 	 		+ "AND d.CLIENT_CD != 'NMBER'"
   		 	 		+ "AND b.ORDER_KND_CODE = 'SBS' "
   		 	 		+ "AND a.SETLE_STTUS_CODE IN('R','P') " 
   		 	 		+ "AND DATE(DATE_FORMAT(a.SETLE_PRARNDE, '%Y%m%d')) = DATE(NOW()) "
					+ "GROUP BY a.ORDER_SETLE_NO";

    		break;
    	case "1" :
    		//결제 실패건 조회
    		readerSql = "SELECT " 
    				+ "a.ORDER_SETLE_NO, a.SETLE_TOT_AMOUNT, a.SETLE_TY_CODE, "
    				+ "a.SETLE_STTUS_CODE, a.SETLE_RESULT_CODE, a.SETLE_RESULT_MSSAGE, "
    				+ "a.EZW_SETLE_CONFM_NO, a.INI_SETLE_CONFM_NO, a.SETLE_PNTTM, "
    				+ "a.SETLE_POINT, a.SETLE_CARD_AMOUNT, a.SETLE_PRARNDE, a.SETLE_RESULT_TY_CODE, a.SETLE_FAIL_CNT, " 
    				 
    				+ "b.ORDER_NO, b.ORDER_GROUP_NO, b.ORDRR_ID, b.ORDRR_NM, b.NOW_ODR, b.GOODS_ID, b.ORDER_KND_CODE, b.SBSCRPT_DLVY_DAY, "
    				+ "b.GOODS_AMOUNT, b.SBSCRPT_CYCLE_SE_CODE, b.SBSCRPT_WEEK_CYCLE, "
    				+ "b.SBSCRPT_MT_CYCLE, b.ORDER_CO, b.DLVY_USER_NM, b.BILL_KEY, b.NEXT_SETLEDE, b.TELNO AS ORDRR_TELNO, b.ORDER_STTUS_CODE, b.DLVY_AMOUNT, "
    				 
    				+ "c.PCMAPNG_ID, c.GOODS_NM, c.GOODS_SPLPC, c.TAXT_SE_CODE,"
					+ "c.VCH_CODE,"

    				+ "d.POINT_YN, d.EZMIL_YN, d.SPECIAL_USE_YN, d.RECEIPT_YN, d.USER_KEY, "
    				+ "d.EMAIL, d.CLIENT_CD, "
    				 
    			 	+ "e.PRTNR_ID, "
    			 	 
    			 	+ "f.ORDER_ODR, f.HDRY_DLVY_DE, f.ORDER_REQ_STTUS_CODE, f.DLVY_ZIP, f.DLVY_ADRES, "
    			 	+ "f.DLVY_ADRES_DETAIL, f.DLVY_MSSAGE, f.TELNO AS DLVY_TELNO, f.SLE_AMOUNT, f.DSCNT_AMOUNT, f.ORDER_INFO, "

					+ "g.CHARGER_TELNO,"

					+ "gc.COUPON_NO,"
					+ "gc.COUPON_VALID_PD,"
					+ "gc.COUPON_PD_TY,"
					+ " CASE WHEN DATE_FORMAT(gc.COUPON_END_PNTTM , '%Y%m%d') < DATE_FORMAT(NOW(),'%Y%m%d') THEN 'Y' ELSE 'N' END AS COUPON_DT_END_AT "
   		 	 	+ "FROM "
   		 	 		+ "STN_ORDER_SETLE AS a "
   		 	 		+ "JOIN STN_ORDER_DLVY AS f "
   		 	 		+ "ON a.ORDER_SETLE_NO = f.ORDER_SETLE_NO "	 
   		 	 		+ "JOIN STN_ORDER AS b "
   		 	 		+ "ON f.ORDER_NO = b.ORDER_NO "
   		 	 		+ "JOIN STN_GOODS AS c "
   		 	 		+ "ON b.GOODS_ID = c.GOODS_ID "
   		 	 		+ "LEFT JOIN DTN_MBER AS d "
   		 	 		+ "ON b.FRST_REGISTER_ID = d.ESNTL_ID "
   		 	 		+ "JOIN STN_PRTNR_CMPNY_MAPNG AS e "
   		 	 		+ "ON c.PCMAPNG_ID = e.PCMAPNG_ID "
   		 	 		+ "JOIN STN_CMPNY AS g "
		 	 		+ "ON e.CMPNY_ID = g.CMPNY_ID "
					+ "LEFT JOIN STN_GOODS_COUPON AS gc "
					+ "ON gc.ORDER_NO = b.ORDER_NO "
   		 	 	+ "WHERE "
   		 	 		+ "1 = 1 " 
   		 	 		+ "AND a.USE_AT = 'Y' " 
   		 	 		+ "AND b.USE_AT = 'Y' "
   		 	 		+ "AND e.USE_AT = 'Y' "
					+ "AND d.CLIENT_CD != 'NMBER'"
   		 	 		+ "AND b.ORDER_KND_CODE = 'SBS' "
   		 	 		+ "AND a.SETLE_STTUS_CODE = 'F' " 
   		 	 		+ "AND DATE(DATE_FORMAT(a.SETLE_PRARNDE, '%Y%m%d')) > DATE(NOW()) - 7"
					+ "GROUP BY a.ORDER_SETLE_NO";
    		break;
		default :
			break;
    	}
    	return readerSql;
    }
        
    
    public ItemProcessor<ModooPayVO, ModooPayVO> regularPayProcessor() {
    	return new ItemProcessor<ModooPayVO, ModooPayVO>() {
	    	@Override
	    	public ModooPayVO process(ModooPayVO mvo) throws Exception {  	  
	    		if(null == mvo)
	    			return null;
	    		else
	    			return regularPayService.doRegularPay(mvo);
	    	}
    	};
	}
    
    
    public ItemWriter<ModooPayVO> regularPayWriter() {
        return new ItemWriter<ModooPayVO>() {

			@Override
			public void write(List<? extends ModooPayVO> items) throws Exception {
				if(null == items || 0 == items.size()) {
					return;
				}
				LOGGER.info("********* ItemWriter START size : [ {} ]", items.size());
				regularPayService.doWritePayInfo(items);
			}        	
        };
    }
}
