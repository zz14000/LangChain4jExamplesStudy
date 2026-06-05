package _4_parallel_workflow;

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
import java.util.concurrent.Executors;

public class _4_Parallel_Workflow_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // 控制模型调用的日志输出量
    }

     /**
     * 本示例演示了如何实现3个并行的 CvReviewer 智能体，它们将同时评估简历。
     * 我们将实现三个智能体：
     * - ManagerCvReviewer（评估候选人可能胜任工作的程度）
     *      输入：简历和职位描述
     * - TeamMemberCvReviewer（评估候选人融入团队的程度）
     *      输入：简历
     * - HrCvReviewer（从HR角度检查候选人是否符合要求）
     *      输入：简历、HR要求
     */

    // 1. 定义驱动智能体的模型
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. 在本包中定义三个子智能体：
        //      - HrCvReviewer.java
        //      - ManagerCvReviewer.java
        //      - TeamMemberCvReviewer.java

        // 3. 使用 AgenticServices 创建所有智能体
        HrCvReviewer hrCvReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("hrReview") // 每次迭代都会被覆盖，也用作我们想要观察的最终输出
                .build();

        ManagerCvReviewer managerCvReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("managerReview") // 覆盖原始输入指令，每次迭代都会被覆盖并用作 CvTailor 的新指令
                .build();

        TeamMemberCvReviewer teamMemberCvReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("teamMemberReview") // 覆盖原始输入指令，每次迭代都会被覆盖并用作 CvTailor 的新指令
                .build();

        // 4. 构建并行工作流
        var executor = Executors.newFixedThreadPool(3);  // 保留引用以便稍后关闭

        UntypedAgent cvReviewGenerator = AgenticServices // 除非定义了结果组合智能体，否则使用 UntypedAgent，见 _2_Sequential_Agent_Example
                .parallelBuilder()
                .subAgents(hrCvReviewer, managerCvReviewer, teamMemberCvReviewer) // 可以添加任意多个智能体
                .executor(executor) // 可选，默认使用内部缓存线程池，执行完成后会自动关闭
                .outputKey("fullCvReview") // 这是我们想要观察的最终输出
                .output(agenticScope -> {
                    // 从智能体作用域中读取每个审查者的输出
                    CvReview hrReview = (CvReview) agenticScope.readState("hrReview");
                    CvReview managerReview = (CvReview) agenticScope.readState("managerReview");
                    CvReview teamMemberReview = (CvReview) agenticScope.readState("teamMemberReview");
                    // 返回汇总审查，评分为平均值（或在此处使用任何其他聚合方式）
                    String feedback = String.join("\n",
                            "HR审查: " + hrReview.feedback,
                            "经理审查: " + managerReview.feedback,
                            "团队成员审查: " + teamMemberReview.feedback
                    );
                    double avgScore = (hrReview.score + managerReview.score + teamMemberReview.score) / 3.0;

                    return new CvReview(avgScore, feedback);
                        })
                .build();

        // 5. 从 resources/documents/ 中的文本文件加载原始参数
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");

        // 6. 因为使用无类型智能体，我们需要传递参数映射
        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "jobDescription", jobDescription
                ,"hrRequirements", hrRequirements
                ,"phoneInterviewNotes", phoneInterviewNotes
        );

        // 7. 调用组合智能体生成定制简历
        var review = cvReviewGenerator.invoke(arguments);

        // 8. 打印生成的简历
        System.out.println("=== REVIEWED CV ===");
        System.out.println(review);

        // 9. 关闭执行器
        executor.shutdown();
   }
}