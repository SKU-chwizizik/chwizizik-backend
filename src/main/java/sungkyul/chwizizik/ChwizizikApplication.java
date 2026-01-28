package sungkyul.chwizizik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class}) // db없이 실행
public class ChwizizikApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChwizizikApplication.class, args);
	}
}
