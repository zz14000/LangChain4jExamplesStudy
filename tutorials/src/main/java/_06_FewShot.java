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

public class _06_FewShot {

    public static void main(String[] args) {

        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-v4-flash")
                .timeout(ofSeconds(100))
                .build();

        List<ChatMessage> fewShotHistory = new ArrayList<>();

        // Adding positive feedback example to history
        fewShotHistory.add(UserMessage.from(
                "I love the new update! The interface is very user-friendly and the new features are amazing!"));
        fewShotHistory.add(AiMessage.from(
                "Action: forward input to positive feedback storage\nReply: Thank you very much for this great feedback! We have transmitted your message to our product development team who will surely be very happy to hear this. We hope you continue enjoying using our product."));

        // Adding negative feedback example to history
        fewShotHistory.add(UserMessage
                .from("I am facing frequent crashes after the new update on my Android device."));
        fewShotHistory.add(AiMessage.from(
                "Action: open new ticket - crash after update Android\nReply: We are so sorry to hear about the issues you are facing. We have reported the problem to our development team and will make sure this issue is addressed as fast as possible. We will send you an email when the fix is done, and we are always at your service for any further assistance you may need."));

        // Adding another positive feedback example to history
        fewShotHistory.add(UserMessage
                .from("Your app has made my daily tasks so much easier! Kudos to the team!"));
        fewShotHistory.add(AiMessage.from(
                "Action: forward input to positive feedback storage\nReply: Thank you so much for your kind words! We are thrilled to hear that our app is making your daily tasks easier. Your feedback has been shared with our team. We hope you continue to enjoy using our app!"));

        // Adding another negative feedback example to history
        fewShotHistory.add(UserMessage
                .from("The new feature is not working as expected. It’s causing data loss."));
        fewShotHistory.add(AiMessage.from(
                "Action: open new ticket - data loss by new feature\nReply:We apologize for the inconvenience caused. Your feedback is crucial to us, and we have reported this issue to our technical team. They are working on it on priority. We will keep you updated on the progress and notify you once the issue is resolved. Thank you for your patience and support."));

        // Adding real user's message
        UserMessage customerComplaint = UserMessage
                .from("How can your app be so slow? Please do something about it!");
        fewShotHistory.add(customerComplaint);

        System.out.println("[User]: " + customerComplaint.singleText());
        System.out.print("[LLM]: ");

        CompletableFuture<ChatResponse> futureChatResponse = new CompletableFuture<>();

        model.chat(fewShotHistory, new StreamingChatResponseHandler() {

            /**
             * - 触发时机 ：每次接收到部分响应时立即调用
             * - 作用 ：实时处理流式响应，将其打印到控制台
             * - 特点 ：可以多次调用，每次调用处理部分响应
             * - 类比 ：就像在写文章时，每写一个句子就打印出来一样
             */
            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            /**
             * - 触发时机 ：完整响应接收完成后调用
             * - 作用 ：将完整响应存储到 CompletableFuture 中
             * - 特点 ：只调用一次（如果出错）
             * - 类比 ：就像在写文章时，写完最后一句话后，需要存储完整的内容一样
             */
            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                futureChatResponse.complete(completeResponse);
            }

            /**
             * - 触发时机 ：在处理流式响应时发生错误时调用
             * - 作用 ：将错误存储到 CompletableFuture 中
             * - 特点 ：只调用一次（如果出错）
             * - 类比 ：就像在写文章时，如果发生错误，需要记录下来一样
             */
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
