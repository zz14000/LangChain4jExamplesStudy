package _7_supervisor_orchestration;

import _4_parallel_workflow.HrCvReviewer;
import _4_parallel_workflow.ManagerCvReviewer;
import _4_parallel_workflow.TeamMemberCvReviewer;
import _5_conditional_workflow.EmailAssistant;
import _5_conditional_workflow.InterviewOrganizer;
import _5_conditional_workflow.OrganizingTools;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;

/**
 * 到目前为止，我们构建的都是确定性工作流：
 * - 顺序、并行、条件、循环，以及它们的组合。
 * 你还可以构建监督者智能体系统，其中一个智能体会
 * 动态决定以什么顺序调用哪些子智能体。
 * 在本示例中，监督者协调招聘工作流：
 * 他应该运行HR/经理/团队审查，然后安排面试
 * 或发送拒绝邮件。
 * 就像组合工作流示例的第2部分，但现在是"自组织"的。
 * 注意，监督者超级智能体可以像其他超级智能体类型一样在组合工作流中使用。
 * 重要提示：本示例使用 GPT-4o-mini 运行大约需要50秒。你可以在 PRETTY 日志中持续查看正在发生的事情。
 * 有一些方法可以加速执行，请参见本文件末尾的注释。
 */
public class _7a_Supervisor_Orchestration {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 200);  // 控制模型调用的日志输出量
    }

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 1. 定义所有子智能体
        HrCvReviewer hrReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("hrReview")
                .build();
        // 重要的是，如果我们对多个智能体使用相同的方法名
        // （在本例中：所有审查者都使用 'reviewCv'），我们最好为智能体命名，像这样：
        // @Agent(name = "managerReviewer", description = "根据职位描述审查简历，给出反馈和评分")

        ManagerCvReviewer managerReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("managerReview")
                .build();

        TeamMemberCvReviewer teamReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("teamMemberReview")
                .build();

        InterviewOrganizer interviewOrganizer = AgenticServices.agentBuilder(InterviewOrganizer.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .build();

        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .build();

        // 2. 构建监督者智能体
        SupervisorAgent hiringSupervisor = AgenticServices.supervisorBuilder()
                .chatModel(CHAT_MODEL)
                .subAgents(hrReviewer, managerReviewer, teamReviewer, interviewOrganizer, emailAssistant)
                .contextGenerationStrategy(SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION)
                .responseStrategy(SupervisorResponseStrategy.SUMMARY) // 我们想要发生的事情的摘要，而不是检索响应
                .supervisorContext("始终使用完整的审查小组。始终用英语回答。调用智能体时，使用纯JSON（不要反引号，换行用反斜杠+n）。") // 可选的监督者行为上下文
                .build();
        // 重要提示：监督者每次只会调用1个智能体，然后审查他的计划以选择下一个要调用的智能体
        // 监督者无法并行执行智能体
        // 如果智能体被标记为异步，监督者将覆盖该设置（不异步执行）并发出警告

        // 3. 加载候选人简历和职位描述
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");

        // 开始计时
        long start = System.nanoTime();
        // 4. 用自然语言请求调用监督者
        String result = (String) hiringSupervisor.invoke(
                "评估以下候选人：\n" +
                        "候选人简历：\n" + candidateCv + "\n\n" +
                        "候选人联系方式：\n" + candidateContact + "\n\n" +
                        "职位描述：\n" + jobDescription + "\n\n" +
                        "HR要求：\n" + hrRequirements + "\n\n" +
                        "电话面试记录：\n" + phoneInterviewNotes
        );
        long end = System.nanoTime();
        double elapsedSeconds = (end - start) / 1_000_000_000.0;
        // 在日志中你会注意到最终调用了 'done' 智能体，这是监督者结束调用序列的方式

        System.out.println("=== 监督者运行完成，耗时 " + elapsedSeconds + " 秒 ===");
        System.out.println(result);
    }

    // 高级用例：
    // 参见 _7b_Supervisor_Orchestration_Advanced.java 了解
    // - 类型化监督者，
    // - 上下文工程，
    // - 输出策略，
    // - 调用链观察，

    // 关于延迟：
    // 此流程的完整运行通常需要超过60秒。
    // 一种解决方案是使用像 CEREBRAS 这样的快速推理提供商，
    // 它将在10秒内运行整个流程，但会犯更多错误。
    // 要使用 CEREBRAS 尝试此示例，请获取密钥（点击获取免费API密钥）
    // https://inference-docs.cerebras.ai/quickstart
    // 并在环境变量中保存为 "CEREBRAS_API_KEY"
    // 然后将第38行改为：
    // private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel("CEREBRAS");

}
