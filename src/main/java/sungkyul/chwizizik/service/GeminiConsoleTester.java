package sungkyul.chwizizik.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Scanner;

@Component
public class GeminiConsoleTester implements CommandLineRunner {

    private final GeminiService geminiService;

    public GeminiConsoleTester(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\n[시스템] 박 부장님과의 모의 면접 시작");
            System.out.println("================================");

            // 첫 질문 가져오기
            String initialQuestion = geminiService.getInitialQuestion();
            System.out.println("\n[박 부장]: " + initialQuestion);

            while (true) {
                // 터미널 안정화를 위한 대기
                Thread.sleep(300);

                System.out.print("\n[지원자(나)]: ");
                System.out.flush();

                if (scanner.hasNextLine()) {
                    String userResponse = scanner.nextLine();

                    // 유저가 직접 종료를 원할 때
                    if (userResponse.equalsIgnoreCase("종료")) {
                        System.out.println("\n[시스템] 면접을 중단합니다.");
                        break;
                    }

                    // 박 부장의 다음 질문 생성
                    String nextQuestion = geminiService.getNextQuestion(userResponse);
                    System.out.println("\n[박 부장]: " + nextQuestion);

                    // 면접 종료 태그
                    if (nextQuestion.contains("[면접 종료]")) {
                        break;
                    }
                }
            }
        }
    }
}