package modoo.ext.module.payment;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@EnableBatchProcessing
public class ModoopaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModoopaymentApplication.class, args);
	}

}
