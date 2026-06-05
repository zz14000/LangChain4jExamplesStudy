package _3_loop_workflow;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import domain.CvReview;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class _3b_Loop_Agent_Example_States_And_Fail {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // 控制模型调用的日志输出量
    }

    /**
     * 这里我们构建与 3a 相同的循环智能体，但这次我们应该看到它失败，
     * 因为尝试将简历定制到不匹配的职位描述上。
     * 我们还将返回最新的评分和反馈，以及最终的简历，
     * 这将允许我们检查是否获得了好的评分，以及是否值得提交这份简历。
     * 我们还展示了一个技巧，通过在每次检查退出条件时（即每次智能体调用后）
     * 将审查结果存储到列表中，来检查审查的中间状态（它在每次循环中会被覆盖）。
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 1. 创建所有子智能体（与之前相同）
        CvReviewer cvReviewer = AgenticServices.agentBuilder(CvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("cvReview") // 每次迭代都会用新的反馈更新，用于下一次定制
                .build();
        ScoredCvTailor scoredCvTailor = AgenticServices.agentBuilder(ScoredCvTailor.class)
                .chatModel(CHAT_MODEL)
                .outputKey("cv") // 每次迭代都会更新，持续改进简历
                .build();

        // 2. 构建循环工作流并在每次退出条件检查时存储审查结果
        // 了解退出条件是否满足还是仅达到最大迭代次数可能很重要
        // （例如，John 可能根本不想申请这份工作）。
        // 你可以更改输出变量，使其也包含最后的评分和反馈，并在循环结束后自行检查。
        // 你还可以将中间值存储在可变列表中以便稍后检查。
        // 下面的代码同时完成了这两件事。
        List<CvReview> reviewHistory = new ArrayList<>();

        UntypedAgent reviewedCvGenerator = AgenticServices // 除非定义了结果组合智能体，否则使用 UntypedAgent，见下文
                .loopBuilder().subAgents(cvReviewer, scoredCvTailor) // 可以添加任意多个智能体，顺序很重要
                .outputKey("cvAndReview") // 这是我们想要观察的最终输出
                .output(agenticScope -> {
                    Map<String, Object> cvAndReview = Map.of(
                            "cv", agenticScope.readState("cv"),
                            "finalReview", agenticScope.readState("cvReview")
                    );
                    return cvAndReview;
                })
                .exitCondition(scope -> {
                    CvReview review = (CvReview) scope.readState("cvReview");
                    reviewHistory.add(review); // 在每次智能体调用时捕获评分+反馈
                    System.out.println("退出检查，评分=" + review.score);
                    return review.score >= 0.8;
                })
                .maxIterations(3) // 安全措施，避免无限循环，以防退出条件永远无法满足
                .build();

        // 3. 从 resources/documents/ 中的文本文件加载原始参数
        // - master_cv.txt
        // - job_description_backend.txt
        String masterCv = StringLoader.loadFromResource("/documents/master_cv.txt");
        String fluteJobDescription = "我们正在寻找一位热情的长笛教师加入我们的音乐学院。";

        // 4. 因为使用无类型智能体，我们需要传递参数映射
        Map<String, Object> arguments = Map.of(
                "cv", masterCv, // 从主简历开始，它将被持续改进
                "jobDescription", fluteJobDescription
        );

        // 5. 调用组合智能体生成定制简历
        Map<String, Object> cvAndReview = (Map<String, Object>) reviewedCvGenerator.invoke(arguments);

        // 你可以在日志中观察到各步骤，例如：
        // 第1轮输出: "content": "{\n  \"score\": 0.0,\n  \"feedback\": \"This CV is not suitable for the flute teacher position at our music academy...
        // 第2轮输出: "content": "{\n  \"score\": 0.3,\n  \"feedback\": \"John's CV demonstrates strong soft skills such as communication, patience, and adaptability, which are important in a teaching role. However, the absence of formal music training or ...
        // 第3轮输出: "content": "{\n  \"score\": 0.4,\n  \"feedback\": \"John Doe demonstrates strong soft skills and mentoring experience,...

        System.out.println("=== REVIEWED CV FOR FLUTE TEACHER ===");
        System.out.println(cvAndReview.get("cv")); // 循环后的最终简历

        // 现在你在输出映射中获取了 finalReview，可以检查
        // 最终的评分和反馈是否满足你的要求
        CvReview review = (CvReview) cvAndReview.get("finalReview");
        System.out.println("=== FINAL REVIEW FOR FLUTE TEACHER ===");
        System.out.println("简历" + (review.score >= 0.8 ? "通过" : "未通过") + "，评分=" + review.score);
        System.out.println("最终反馈：" + review.feedback);

        // 在 reviewHistory 中你可以找到完整的审查历史
        System.out.println("=== FULL REVIEW HISTORY FOR FLUTE TEACHER ===");
        System.out.println(reviewHistory);

    }
}
