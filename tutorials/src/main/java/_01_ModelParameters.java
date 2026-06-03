import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static java.time.Duration.ofSeconds;

/**
 * 简单调用api回答问题
 */
public class _01_ModelParameters {

    public static void main(String[] args) {

        // OpenAI parameters are explained here: https://platform.openai.com/docs/api-reference/chat/create

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-v4-flash")
                .temperature(0.3)
                .timeout(ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        String prompt = "如何缓解焦虑，回答在50字作用";

        String response = model.chat(prompt);

        System.out.println(response);
    }
}
