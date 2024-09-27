package kappzzang.jeongsan.global.client.openai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Getter
@Component
public class GptPromptManager {

    private final String instructionPath;
    private final String instruction;

    public GptPromptManager(@Value("${gpt.instruction.path}") String instructionPath) {
        this.instructionPath = instructionPath;
        this.instruction = loadPrompt();
    }

    private String loadPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource(instructionPath);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JeongsanException(ErrorType.INTERNAL_SERVER_ERROR);
        }
    }

}
