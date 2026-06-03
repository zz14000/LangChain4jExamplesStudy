import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

/**
 * 最基本的调用模型
 */
public class _00_HelloWorld {

    public static void main(String[] args) {

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-v4-flash")
                .build();

        String answer = model.chat("说你好世界");

        System.out.println(answer);
    }
}
