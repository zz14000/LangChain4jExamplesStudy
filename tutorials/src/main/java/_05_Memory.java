import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4;

/**
 * 复杂场景下（多轮对话）的记忆管理，可重复调用的chat模型演示
 * 流程图
 * main()
  ├─ 创建 model
  ├─ 创建 chatMemory
  ├─ 添加 systemMessage
  │
  ├─ 第一轮对话
  │   ├─ 添加 userMessage1 到记忆
  │   ├─ 调用 streamChat()
  │   │    ├─ 创建 future
  │   │    ├─ 调用 model.chat(messages, handler)
  │   │    ├─ future.get() 等待
  │   │    └→ 返回 AiMessage
  │   └─ 添加 aiMessage1 到记忆
  │
  └─ 第二轮对话（同上）
 */
public class _05_Memory {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-v4-flash")
                .build();

        // 使用 GPT_4 作为 token 估算器，因为 jtokkit 不支持 deepseek-v4-flash
        // DeepSeek 的 token 计算方式与 GPT-4 类似，可以用于演示目的
        ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(1000, new OpenAiTokenCountEstimator(GPT_4));

        SystemMessage systemMessage = SystemMessage.from(
                "你是一位高级开发者，正在向另一位高级开发者解释，"
                        + "你正在开发的项目是一个电子商务平台，使用 Java 后端、" +
                        "Oracle 数据库和 Spring Data JPA");
        chatMemory.add(systemMessage);


        UserMessage userMessage1 = userMessage(
                "如何为大规模电子商务平台优化数据库查询？简短回答，最多 3 到 5 行。");
        chatMemory.add(userMessage1);

        System.out.println("[User]: " + userMessage1.singleText());
        System.out.print("[LLM]: \n");

        AiMessage aiMessage1 = streamChat(model, chatMemory);
        chatMemory.add(aiMessage1);

        UserMessage userMessage2 = userMessage(
                "给出第一点的实际示例实现？" +
                        "简短，最多 10 行代码。");
        chatMemory.add(userMessage2);

        System.out.println("\n\n[User]: " + userMessage2.singleText());
        System.out.print("[LLM]: ");

        AiMessage aiMessage2 = streamChat(model, chatMemory);
        chatMemory.add(aiMessage2);
    }

    private static AiMessage streamChat(OpenAiStreamingChatModel model, ChatMemory chatMemory)
            throws ExecutionException, InterruptedException {

        CompletableFuture<AiMessage> futureAiMessage = new CompletableFuture<>();

        //显示声明handler，方便在chat中重复使用
        StreamingChatResponseHandler handler = new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                futureAiMessage.complete(completeResponse.aiMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                futureAiMessage.completeExceptionally(throwable);
            }
        };

        model.chat(chatMemory.messages(), handler);
        /**
         * 对于get和join方法
         * 1.从对异常处理来区分
         * get方法需要在函数头声明异常 throws xxxx
         *    get异常层次
         *     ExecutionException (检查异常)
                └─ getCause() → 原始异常
                    ├─ RuntimeException
                    ├─ IOException
                    └─ 其他异常
         *    join异常层次
                    CompletionException (非检查异常)
                        └─ getCause() → 原始异常
                            ├─ RuntimeException
                            ├─ IOException
                            └─ 其他异常          
         * 2.从对超时时间来区分
            get可设置超时时间
            join不支持超时
         */
        try {
            return futureAiMessage.get(10, TimeUnit.SECONDS);  // ✅ 设置超时时间为 10 秒
        } catch (Exception e) {
            throw new RuntimeException("聊天失败", e);
        }
    }
}
