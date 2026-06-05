import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class _10_ServiceWithToolsExample {

    // Please also check CustomerSupportApplication and CustomerSupportApplicationTest
    // from spring-boot-example module

    static class Calculator {

        @Tool("计算字符串的长度")
        int stringLength(String s) {
            System.out.println("调用了 stringLength()，参数 s='" + s + "'");
            return s.length();
        }

        @Tool("计算两个数字的和")
        int add(int a, int b) {
            System.out.println("调用了 add()，参数 a=" + a + ", b=" + b);
            return a + b;
        }

        @Tool("计算一个数的平方根")
        double sqrt(int x) {
            System.out.println("调用了 sqrt()，参数 x=" + x);
            return Math.sqrt(x);
        }
    }

    interface Assistant {

        String chat(String userMessage);
    }

    public static void main(String[] args) {

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY) // 警告！工具不支持 "demo" API 密钥
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-v4-flash")
                .strictTools(true) // https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-tools（工具的结构化输出）
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(new Calculator())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        String question = "单词\"hello\"和\"world\"的字母数之和的平方根是多少？";

        String answer = assistant.chat(question);

        System.out.println(answer);
        // "hello"和"world"两个单词的字母数之和的平方根约为 3.162。
    }
}
