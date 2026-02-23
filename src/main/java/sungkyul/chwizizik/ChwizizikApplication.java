package sungkyul.chwizizik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ChwizizikApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChwizizikApplication.class, args);
	}
}
