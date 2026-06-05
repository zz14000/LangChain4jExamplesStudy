package _9_human_in_the_loop;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;
import dev.langchain4j.model.chat.ChatModel;
import domain.CvReview;
import util.ChatModelProvider;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Scanner;

public class _9a_HumanInTheLoop_Simple_Validator {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);
    }

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {
        // 3. 创建相关智能体
        HiringDecisionProposer decisionProposer = AgenticServices.agentBuilder(HiringDecisionProposer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("modelDecision")
                .build();

        // 2. 定义人工验证的人机交互环节
        HumanInTheLoop humanValidator = AgenticServices.humanInTheLoopBuilder()
                .description("验证模型提出的招聘决策")
                .outputKey("finalDecision") // 由人工检查
                .responseProvider(scope -> {
                    System.out.println("AI招聘助手建议：" + scope.readState("request"));
                    System.out.println("请确认最终决策。");
                    System.out.println("选项：邀请现场面试(I)，拒绝(R)，暂缓(H)");
                    System.out.print("> "); // 在实际系统中需要输入验证和错误处理
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                        return reader.readLine();
                    } catch (IOException e) {
                        throw new RuntimeException("读取输入失败", e);
                    }
                })
                .build();

        // 3. 将智能体链接为工作流
        UntypedAgent hiringDecisionWorkflow = AgenticServices.sequenceBuilder()
                .subAgents(decisionProposer, humanValidator)
                .outputKey("finalDecision")
                .build();

        // 4. 准备输入参数
        Map<String, Object> input = Map.of(
                "cvReview", new CvReview(0.85,
                        """
                                除了要求的React经验外，技术能力很强。
                                不过似乎是一个快速且独立的学习者。文化契合度好。
                                工作许可问题可能可以解决。
                                薪资期望略高于计划预算。
                                决定继续进行现场面试。
                                """)
        );

        // 5. 运行工作流
        String finalDecision = (String) hiringDecisionWorkflow.invoke(input);

        System.out.println("\n=== 人工最终决策 ===");
        System.out.println("(邀请现场面试(I)，拒绝(R)，暂缓(H))\n");
        System.out.println(finalDecision);

        // 注意：人机交互和人工验证通常需要很长时间等待用户响应。
        // 在这种情况下，建议使用异步智能体，这样它们不会阻塞工作流的其余部分
        // 那些可以在用户回答之前执行的部分。
    }
}
