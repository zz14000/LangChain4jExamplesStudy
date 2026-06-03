import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;

import java.util.concurrent.CompletableFuture;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

/**
 * 流式chat，接收一个文字，打印一个文字，普通chat:阻塞式，所有内容接收完全才打印
 * 工作流程图
 * 主线程                          后台线程（流式传输）
  │
  ├─ 创建 CompletableFuture
  │
  ├─ 调用 model.chat() ────────→ 开始流式传输
  │                                 │
  │                                 ├→ onPartialResponse("这")
  │                                 ├→ onPartialResponse("是")
  │                                 ├→ ...
  │                                 │
  ├─ future.join() ───────────────┤
  │  (阻塞等待)                     │
  │                                 ├→ onCompleteResponse(...)
  │                                 │    └─ future.complete()
  │                                 │
  │ ←───────────────────────────────┘
  │
  ├─ join() 返回，继续执行
  │
  └─ 程序结束
 */
public class _04_Streaming {

    public static void main(String[] args) {

        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-v4-flash")
                .build();

        String prompt = "写一首关于开发者和空指针的简短有趣诗歌，最多 10 行";

        System.out.println("Nr of chars: " + prompt.length());
        //使用jtokkit统计提示词token数
        // System.out.println("Nr of tokens: " + new OpenAiTokenCountEstimator("deepseek-v4-flash").estimateTokenCountInText(prompt));

        //是 Java 8 引入的 异步编程工具 ，用于处理异步计算的结果
        CompletableFuture<ChatResponse> futureChatResponse = new CompletableFuture<>();

        // 如果没有 CompletableFuture，程序会这样：
        // 1. 调用 model.chat() - 立即返回
        // 2. 主线程继续执行 - 但没有后续代码了
        // 3. 程序结束退出 ❌
        // 4. 但流式响应还在后台传输... 但程序已经没了！
        model.chat(prompt, new StreamingChatResponseHandler() {

            /**
             * - 触发时机 ：每次接收到部分响应时立即调用
             * - 作用 ：实时处理接收到的文本片段
             * - 特点 ：会被多次调用，每次返回一小段文本
             * - 类比 ：像打字机一样，一个字一个字显示
             */
            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            /**
             * - 触发时机 ：所有响应内容接收完毕后调用
             * - 作用 ：处理完整的响应，执行后续逻辑
             * - 特点 ：只调用一次，包含完整的聊天响应
             * - 类比 ：文章写完了，可以做总结处理
             */
            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                System.out.println("\n\nDone streaming");
                futureChatResponse.complete(completeResponse);//标识结束
            }

            /**
             * - 触发时机 ：发生错误时调用
             * - 作用 ：错误处理和异常恢复
             * - 特点 ：只调用一次（如果出错）
             * - 类比 ：出错了，需要处理异常
             */
            @Override
            public void onError(Throwable error) {
                futureChatResponse.completeExceptionally(error);
            }
        });

        // 主进程等待 CompletableFuture 完成，确保所有响应都被处理，核心方法
        futureChatResponse.join();
         // 如果没有 CompletableFuture，主程序会立即执行到这里并结束
         // 流式响应可能还没传输完！
    }
}
