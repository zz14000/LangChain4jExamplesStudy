import dev.langchain4j.code.judge0.Judge0JavaScriptExecutionTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static java.time.Duration.ofSeconds;

public class _11_ServiceWithDynamicToolsExample {

    interface Assistant {

        String chat(String message);
    }

    public static void main(String[] args) {

        Judge0JavaScriptExecutionTool judge0Tool = new Judge0JavaScriptExecutionTool(ApiKeys.RAPID_API_KEY);

        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-v4-flash")
                .temperature(0.0)
                .timeout(ofSeconds(60))
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .tools(judge0Tool)
                .build();

        interact(assistant, "49506838032859 的平方根是多少？");
        interact(assistant, "将每第三个字母大写：abcabc");
        interact(assistant, "1988 年 2 月 21 日 17:00 到 2014 年 4 月 12 日 04:00 之间有多少小时？");
    }

    private static void interact(Assistant assistant, String userMessage) {
        System.out.println("[User]: " + userMessage);
        String answer = assistant.chat(userMessage);
        System.out.println("[Assistant]: " + answer);
        System.out.println();
        System.out.println();
    }
}
