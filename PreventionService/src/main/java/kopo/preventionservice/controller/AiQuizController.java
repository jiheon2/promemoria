package kopo.preventionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kopo.preventionservice.controller.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {
        "http://192.168.0.99:16000",  // 프론트 서버
        "https://www.forestbysy.store",
},
        allowedHeaders =  "*",
        methods = {RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.POST},
        allowCredentials = "true")
@Tag(name = "AI Prevention Quiz API", description = "AI Prevention Quiz API 설명입니다.")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("quiz/v1")
@RestController
public class AiQuizController {
    private final OpenAiChatModel chatModel;

    // 치매 예방 퀴즈 생성
    @PostMapping("/getCategoryQuiz")
    public ResponseEntity<CommonResponse> getCategoryQuiz(@RequestParam String category) throws Exception {

        log.info("AI 치매 예방 퀴즈 생성 시작");
        log.info("사용자가 선택한 카테고리는 " + category + "입니다.");

        Map<String, String> response = new HashMap<>();
        response.put("QUIZ", chatModel.call("내가 선택한 분야는" + category + "이거야. 이 분야의 치매 예방 OX 퀴즈 5문제만 내줘," +
                "형식은 문제는 문제 1, 문제 2 이런식으로 내주고 정답은 답 1, 답 2 이렇게 해줘, 설명도 추가해서 줘야해 설명 1, 설명 2 이렇게" +
                "잘라서 배열에 담을 거니까 문제랑 답, 설명 앞 뒤로 # 붙여줘" +
                "문제 1. #~~~~하다.# 이런 형식으로 문제 5개 나오고, 그 뒤에 답 1. #O# 이렇게 답 5개 나오고 그 뒤에 각 문제에 대한 설명 1. #~~~라서 ~~하다# 이렇게 보내주면 돼"
        ));

        String[] quiz = response.get("QUIZ").split("#");

        List<String> qna = new ArrayList<>();
        List<String> questions = new ArrayList<>();
        List<String> answers = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();

        for(int j = 1; j < quiz.length; j++) {
            if(!quiz[j].trim().isEmpty()) {
                if (quiz[j].contains("문제") || quiz[j].contains("답") || quiz[j].contains("설명")) {
                    if (quiz[j].contains("문제 해결")) {
                        qna.add(quiz[j]);
                    }
                    continue;
                }
                qna.add(quiz[j]);

            }

            if(qna.size() == 15) {
                break;
            }
        }

        for(int i = 0; i < qna.size(); i++) {
            if(i < 5) {
                questions.add(qna.get(i));
            } if(i >= 5 && i < 10) {
                answers.add(qna.get(i));
            } else if(i >= 10) {
                descriptions.add(qna.get(i));
            }
        }

        System.out.println("퀴즈 : " + questions);
        System.out.println("답 : " + answers);
        System.out.println("설명 : " + descriptions);
        System.out.println(">>>>>>>>>>>>>>>>>");


        Map<String, List> result = new HashMap<>();
        result.put("questions", questions);
        result.put("answers", answers);
        result.put("descriptions", descriptions);

        System.out.println("보낼 퀴즈 : " + result.get("questions"));
        System.out.println("보낼 답 : " + result.get("answers"));
        System.out.println("보낼 설명 : " + result.get("descriptions"));

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), result)
        );
    }
}
