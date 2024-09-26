package kappzzang.jeongsan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kappzzang.jeongsan.dto.Image;
import kappzzang.jeongsan.dto.response.ChatGptResponse;
import kappzzang.jeongsan.dto.response.GeneralOcrResponse;
import kappzzang.jeongsan.dto.response.ParsedReceiptResponse;
import kappzzang.jeongsan.global.client.clova.ClovaApiClient;
import kappzzang.jeongsan.global.client.openai.OpenAiApiClient;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ClovaApiClient clovaApiClient;
    private final OpenAiApiClient openAiApiClient;
    private final ObjectMapper objectMapper;

    public ParsedReceiptResponse extractReceiptData(Image image) {
        try {
            GeneralOcrResponse generalOcrResponse = clovaApiClient.requestClovaGeneralOcr(image);
            String parsedOcrResponse = objectMapper.writeValueAsString(
                generalOcrResponse.images().getFirst());
            ChatGptResponse gptResponse = openAiApiClient.extractDataUsingGPT(
                parsedOcrResponse);
            return objectMapper.readValue(gptResponse.choices().getFirst().message().content(),
                ParsedReceiptResponse.class);
        } catch (JsonProcessingException e) {
            throw new JeongsanException(ErrorType.INTERNAL_SERVER_ERROR);
        }
    }
}
