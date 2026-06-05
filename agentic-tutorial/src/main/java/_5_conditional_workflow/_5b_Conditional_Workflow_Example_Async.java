package _5_conditional_workflow;

import _4_parallel_workflow.ManagerCvReviewer;
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

public class _5b_Conditional_Workflow_Example_Async {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 150);
    }

    /**
     * 本示例演示了多个条件同时满足时，使用异步智能体
     * 允许连续的智能体并行调用以加快执行速度。
     * 在本示例中：
     * - 条件1：如果HR审查通过，简历将传给经理审查，
     * - 条件2：如果HR审查表明缺少信息，则联系候选人获取更多信息。
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 1. 创建所有异步智能体
        ManagerCvReviewer managerCvReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .async(true) // 异步智能体
                .outputKey("managerReview")
                .build();
        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(CHAT_MODEL)
                .async(true)
                .tools(new OrganizingTools())
                .outputKey("sentEmailId")
                .build();
        InfoRequester infoRequester = AgenticServices.agentBuilder(InfoRequester.class)
                .chatModel(CHAT_MODEL)
                .async(true)
                .tools(new OrganizingTools())
                .outputKey("sentEmailId")
                .build();

        // 2. 构建异步条件工作流
        UntypedAgent candidateResponder = AgenticServices
                .conditionalBuilder()
                .subAgents(scope -> {
                    CvReview hrReview = (CvReview) scope.readState("cvReview");
                    return hrReview.score >= 0.8; // 如果HR通过，发送给经理审查
                }, managerCvReviewer)
                .subAgents(scope -> {
                    CvReview hrReview = (CvReview) scope.readState("cvReview");
                    return hrReview.score < 0.8; // 如果HR未通过，发送拒绝邮件
                }, emailAssistant)
                .subAgents(scope -> {
                    CvReview hrReview = (CvReview) scope.readState("cvReview");
                    return hrReview.feedback.toLowerCase().contains("missing information:");
                }, infoRequester) // 如果需要，向候选人请求更多信息
                .output(agenticScope ->
                        (agenticScope.readState("managerReview", new CvReview(0, "no manager review needed"))).toString() +
                                "\n" + agenticScope.readState("sentEmailId", 0)
                ) // 最终输出是经理审查（如果有）
                .build();

        // 3. 输入参数
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        CvReview hrReview = new CvReview(
                0.85,
                """
                        优秀的候选人，薪资期望在范围内，能在期望的时间框架内入职。
                        缺少信息：比利时工作授权状态的详细信息。
                        """
        );

        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "candidateContact", candidateContact,
                "jobDescription", jobDescription,
                "cvReview", hrReview
        );


        // 4. 运行异步条件工作流
        candidateResponder.invoke(arguments);

        System.out.println("=== 异步条件工作流执行完成 ===");
    }
}
