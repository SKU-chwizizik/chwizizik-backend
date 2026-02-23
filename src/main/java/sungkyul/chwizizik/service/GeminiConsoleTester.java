package sungkyul.chwizizik.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class GeminiConsoleTester implements CommandLineRunner {

    private final GeminiService geminiService;

    @Override
    public void run(String... args) throws Exception {
        // Gradle 환경에서 System.in을 강제로 기다리게 하기 위해 인스턴스를 루프 밖에서 한 번만 생성
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n========================================");
        System.out.println("   [시스템] 박 부장님과의 모의 면접 시작");
        System.out.println("========================================\n");

        String aiResponse = geminiService.getInitialQuestion();
        System.out.println("[박 부장]: " + aiResponse);

        while (true) {
            System.out.print("\n[지원자(나)]: ");

            // 무한 루프 방지
            // 실제 입력이 들어올 때까지 쓰레드를 일시 정지
            if (scanner.hasNext()) { 
                String userIn = scanner.nextLine();

                if (userIn.trim().isEmpty()) continue; // 빈 입력은 무시
                if (userIn.equalsIgnoreCase("exit")) break;

                String nextQuestion = geminiService.getNextQuestion(userIn);
                System.out.println("\n[박 부장]: " + nextQuestion);

                if (nextQuestion.contains("[면접 종료]")) {
                    System.out.println("\n[시스템]: 면접이 종료되었습니다.");
                    break;
                }
            } else {
                Thread.sleep(1000);
            }
        }
    }
}