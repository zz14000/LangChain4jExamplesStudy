package _5_conditional_workflow;

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

public class _5a_Conditional_Workflow_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 200);  // 控制模型调用的日志输出量
    }

    /**
     * 本示例演示了条件智能体工作流。
     * 根据评分和候选人资料，我们将
     * - 调用准备现场面试的智能体
     * - 或调用发送拒绝邮件的智能体
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. 在本包中定义两个子智能体：
        //      - EmailAssistant.java
        //      - InterviewOrganizer.java

        // 3. 使用 AgenticServices 创建所有智能体
        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools()) // 智能体可以使用其中定义的所有工具
                .build();
        InterviewOrganizer interviewOrganizer = AgenticServices.agentBuilder(InterviewOrganizer.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .contentRetriever(RagProvider.loadHouseRulesRetriever()) // 这是我们向智能体添加RAG的方式
                .build();

        // 4. 构建条件工作流
        UntypedAgent candidateResponder = AgenticServices // 除非定义了结果组合智能体，否则使用 UntypedAgent，见 _2_Sequential_Agent_Example
                .conditionalBuilder()
                .subAgents(agenticScope -> ((CvReview) agenticScope.readState("cvReview")).score >= 0.8, interviewOrganizer)
                .subAgents(agenticScope -> ((CvReview) agenticScope.readState("cvReview")).score < 0.8, emailAssistant)
                .build();
        // 提示：当定义了多个条件时，它们会按顺序执行。
        // 如果你想要并行执行，请使用异步智能体，如 _5b_Conditional_Workflow_Example_Async 所示

        // 5. 从 resources/documents/ 中的文本文件加载参数
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        CvReview cvReviewFail = new CvReview(0.6, "简历不错，但缺少与后端职位相关的一些技术细节。");
        CvReview cvReviewPass = new CvReview(0.9, "简历非常优秀，符合后端职位的所有要求。");

        // 5. 因为使用无类型智能体，我们需要传递所有输入参数的映射
        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "candidateContact", candidateContact,
                "jobDescription", jobDescription,
                "cvReview", cvReviewPass // 改为 cvReviewFail 可以看到另一个分支
        );

        // 5. 调用条件智能体根据审查结果回复候选人
        candidateResponder.invoke(arguments);
        // 在本示例中，我们没有对 AgenticScope 做有意义的修改
        // 也没有有意义的输出可打印，因为工具执行了最终操作。
        // 我们通过工具执行的操作（发送的邮件、更新的申请状态）打印到控制台

        // 当你在调试模式下观察日志时，工具调用结果'success'仍会发送给模型
        // 模型仍会回答类似"邮件已发送给John Doe，通知他..."

        // 提示：如果工具是你的最后操作，并且你不想之后再回调模型，
        // 通常会添加 @Tool(returnBehavior = ReturnBehavior.IMMEDIATE)`
        // https://docs.langchain4j.dev/tutorials/tools#returning-immediately-the-result-of-a-tool-execution-request
        // !!! 但在智能体工作流中，不推荐对工具使用即时返回行为，
        // 因为即时返回行为会将工具结果存储在 AgenticScope 中，可能会出问题

        // 提示：这是一个通过代码检查条件实现路由行为的示例。
        // 路由行为也可以通过让LLM决定继续使用哪个工具/智能体来实现，
        // 可以使用以下方式：
        // - 监督者智能体：操作智能体，见 _7_supervisor_orchestration
        // - AiServices 作为工具，像这样：
        // RouterService routerService = AiServices.builder(RouterAgent.class)
        //        .chatModel(model)
        //        .tools(medicalExpert, legalExpert, technicalExpert)
        //        .build();
        //
        // 最佳选择取决于你的用例：
        //
        // - 使用条件智能体，你硬编码调用条件
        // - 而使用 AiServices 或监督者，LLM决定调用哪个专家
        //
        // - 使用智能体方案（条件、监督者），所有中间状态和调用链都存储在 AgenticScope 中
        // - 而使用 AiServices，追踪调用链或中间状态要困难得多

    }
}