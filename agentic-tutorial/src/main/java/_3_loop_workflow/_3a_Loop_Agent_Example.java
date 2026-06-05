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
import java.util.Map;

public class _3a_Loop_Agent_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // 控制模型调用的日志输出量
    }

    /**
     * 本示例演示了如何实现一个 CvReviewer 智能体，并将其与 CvTailor 智能体组成循环。
     * 我们将实现两个智能体：
     * - ScoredCvTailor（接收简历并根据 CvReview（反馈/指令 + 评分）进行定制）
     * - CvReviewer（接收定制后的简历和职位描述，返回 CvReview 对象（反馈 + 评分））
     * 此外，当评分超过某个阈值（例如 0.7）时循环结束（退出条件）
     */

    // 1. 定义驱动智能体的模型
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. 在本包中定义两个子智能体：
        //      - CvReviewer.java
        //      - CvTailor.java

        // 3. 使用 AgenticServices 创建所有智能体
        CvReviewer cvReviewer = AgenticServices.agentBuilder(CvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("cvReview") // 每次迭代都会用新的反馈更新，用于下一次定制
                .build();
        ScoredCvTailor scoredCvTailor = AgenticServices.agentBuilder(ScoredCvTailor.class)
                .chatModel(CHAT_MODEL)
                .outputKey("cv") // 每次迭代都会更新，持续改进简历
                .build();

        // 4. 构建循环工作流
        UntypedAgent reviewedCvGenerator = AgenticServices // 除非定义了结果组合智能体，否则使用 UntypedAgent，见 _2_Sequential_Agent_Example
                .loopBuilder().subAgents(cvReviewer, scoredCvTailor) // 可以添加任意多个智能体，顺序很重要
                .outputKey("cv") // 这是我们想要观察的最终输出（改进后的简历）
                .exitCondition(agenticScope -> {
                            CvReview review = (CvReview) agenticScope.readState("cvReview");
                            System.out.println("检查退出条件，评分=" + review.score); // 记录中间评分
                            return review.score > 0.8;
                        }) // 基于 CvReviewer 智能体给出的评分的退出条件，当 > 0.8 时表示满意
                // 注意，退出条件在每次智能体调用后都会检查，而不仅仅是在整个循环结束后
                .maxIterations(3) // 安全措施，避免无限循环，以防退出条件永远无法满足
                .build();

        // 5. 从 resources/documents/ 中的文本文件加载原始参数
        // - master_cv.txt
        // - job_description_backend.txt
        String masterCv = StringLoader.loadFromResource("/documents/master_cv.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");

        // 5. 因为使用无类型智能体，我们需要传递参数映射
        Map<String, Object> arguments = Map.of(
                "cv", masterCv, // 从主简历开始，它将被持续改进
                "jobDescription", jobDescription
        );

        // 5. 调用组合智能体生成定制简历
        String tailoredCv = (String) reviewedCvGenerator.invoke(arguments);

        // 6. 打印生成的简历
        System.out.println("=== REVIEWED CV UNTYPED ===");
        System.out.println((String) tailoredCv);

        // 这份简历可能在第一次定制+审查轮次后就通过了
        // 如果你想看到失败的情况，可以尝试使用长笛教师的职位描述
        // 如示例 3b 所示，我们还会检查简历的中间状态
        // 并获取最终的审查和评分。

    }
}
