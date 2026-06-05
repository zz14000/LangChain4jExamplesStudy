import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static java.time.Duration.ofSeconds;

/**
 * 通过提供示例，AI 会模仿示例中的格式和语气：
 *
 * - 先判断情感倾向（正面/负面）
 * - 执行相应操作（转发/创建工单）
 * - 用专业、礼貌的语气回复
 * 这是一种 无需训练 的提示工程技术，让模型快速学会特定任务的处理方式。
 */
public class _06_FewShot {

    public static void main(String[] args) {

        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-v4-flash")
                .timeout(ofSeconds(100))
                .build();

        List<ChatMessage> fewShotHistory = new ArrayList<>();

        // 添加正面反馈示例到历史记录
        fewShotHistory.add(UserMessage.from(
                "我喜欢这个新更新！界面非常友好，新功能也很棒！"));
        fewShotHistory.add(AiMessage.from(
                "操作：将输入转发到正面反馈存储\n回复：非常感谢您的反馈！我们已将您的消息传达给我们的产品开发团队，他们一定会很高兴听到这个消息。希望您继续享受使用我们的产品。"));

        // 添加负面反馈示例到历史记录
        fewShotHistory.add(UserMessage
                .from("新更新后我的安卓设备频繁崩溃。"));
        fewShotHistory.add(AiMessage.from(
                "操作：创建新工单 - 更新后安卓崩溃\n回复：非常抱歉听到您遇到的问题。我们已将此问题报告给我们的开发团队，并将尽快解决。修复完成后我们会发送电子邮件给您，如有任何进一步帮助，我们随时为您服务。"));

        // 添加另一个正面反馈示例到历史记录
        fewShotHistory.add(UserMessage
                .from("您的应用让我的日常任务变得如此简单！向团队致敬！"));
        fewShotHistory.add(AiMessage.from(
                "操作：将输入转发到正面反馈存储\n回复：非常感谢您的赞誉！我们很高兴听到我们的应用让您的日常任务变得更轻松。您的反馈已与我们团队分享。希望您继续享受使用我们的应用！"));

        // 添加另一个负面反馈示例到历史记录
        fewShotHistory.add(UserMessage
                .from("新功能没有按预期工作，导致数据丢失。"));
        fewShotHistory.add(AiMessage.from(
                "操作：创建新工单 - 新功能导致数据丢失\n回复：我们对造成的不便表示歉意。您的反馈对我们至关重要，我们已将此问题报告给技术团队。他们正在优先处理。我们将随时向您通报进展，并在问题解决后通知您。感谢您的耐心和支持。"));

        // 添加真实用户的消息
        UserMessage customerComplaint = UserMessage
                .from("你的应用怎么能这么慢？请做点什么！");
        fewShotHistory.add(customerComplaint);

        System.out.println("[User]: " + customerComplaint.singleText());
        System.out.print("[LLM]: ");

        CompletableFuture<ChatResponse> futureChatResponse = new CompletableFuture<>();

        model.chat(fewShotHistory, new StreamingChatResponseHandler() {


            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }


            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                futureChatResponse.complete(completeResponse);
            }


            @Override
            public void onError(Throwable error) {
                futureChatResponse.completeExceptionally(error);
            }
        });

        futureChatResponse.join();

        // Extract reply and send to customer
        // Perform necessary action in back-end
    }
}
