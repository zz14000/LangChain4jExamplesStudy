package _8_non_ai_agents;

import _4_parallel_workflow.HrCvReviewer;
import _4_parallel_workflow.ManagerCvReviewer;
import _4_parallel_workflow.TeamMemberCvReviewer;
import _5_conditional_workflow.EmailAssistant;
import _5_conditional_workflow.InterviewOrganizer;
import _5_conditional_workflow.OrganizingTools;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.model.chat.ChatModel;
import domain.CvReview;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;

public class _8_Non_AI_Agents {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 100);  // 控制模型调用的日志输出量
    }

    /**
     * 这里我们展示如何在智能体工作流中使用非 AI 智能体（普通 Java 操作符）。
     * 非 AI 智能体就是普通方法，但可以像任何其他类型的智能体一样使用。
     * 它们非常适合确定性操作，如计算、数据转换和聚合，
     * 在这些场景下你不希望有 LLM 的参与。
     * 你能将越多的步骤外包给非 AI 智能体，你的工作流就越快、越准确、越便宜。
     * 在需要强制某些步骤确定性的工作流中，非 AI 智能体比工具更受青睐。
     * 在本例中，我们希望审查者的聚合评分是确定性计算的，而不是由 LLM 计算。
     * 我们还根据聚合评分确定性更新数据库中的申请状态。
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 1. 在本包中定义 ScoreAggregator 非 AI 智能体

        // 2. 构建并行审查步骤的 AI 子智能体
        HrCvReviewer hrReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("hrReview")
                .build();

        ManagerCvReviewer managerReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("managerReview")
                .build();

        TeamMemberCvReviewer teamReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("teamMemberReview")
                .build();

        // 3. 构建组合并行智能体
        var executor = Executors.newFixedThreadPool(3);  // 保留引用以便稍后关闭

        UntypedAgent parallelReviewWorkflow = AgenticServices
                .parallelBuilder()
                .subAgents(hrReviewer, managerReviewer, teamReviewer)
                .executor(executor)
                .build();

        // 4. 构建包含非 AI 智能体的完整工作流
        UntypedAgent collectFeedback = AgenticServices
                .sequenceBuilder()
                .subAgents(
                        parallelReviewWorkflow,
                        new ScoreAggregator(), // 非 AI 智能体不需要 AgenticServices 构建器。outputKey 'combinedCvReview' 在类中定义
                        new StatusUpdate(), // 接收 'combinedCvReview' 作为输入，不需要输出
                        AgenticServices.agentAction(agenticScope -> { // 另一种添加非 AI 智能体的方式，可以操作 AgenticScope
                            CvReview review = (CvReview) agenticScope.readState("combinedCvReview");
                            agenticScope.writeState("scoreAsPercentage", review.score * 100); // 当来自不同系统的智能体通信时，通常需要输出转换
                        })
                )
                .outputKey("scoreAsPercentage") // outputKey 在 ScoreAggregator.java 的非 AI 智能体注解中定义
                .build();

        // 5. 加载输入数据
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");

        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "candidateContact", candidateContact,
                "hrRequirements", hrRequirements,
                "phoneInterviewNotes", phoneInterviewNotes,
                "jobDescription", jobDescription
        );

        // 6. 调用工作流
        double scoreAsPercentage = (double) collectFeedback.invoke(arguments);
        executor.shutdown();

        System.out.println("=== 评分百分比 ===");
        System.out.println(scoreAsPercentage);
        // 从日志中可以看到，申请状态也已相应更新

    }
}