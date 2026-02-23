package sungkyul.chwizizik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import io.github.cdimascio.dotenv.Dotenv;

@EnableJpaAuditing
@SpringBootApplication
public class ChwizizikApplication {
	public static void main(String[] args) {

	// .env 파일 로드
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    
    // .env에 있는 모든 변수를 시스템 프로퍼티로 등록
    dotenv.entries().forEach(entry -> {
        System.setProperty(entry.getKey(), entry.getValue());
    });

		SpringApplication.run(ChwizizikApplication.class, args);
	}
}
