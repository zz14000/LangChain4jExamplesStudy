package _2_sequential_workflow;

import _1_basic_agent.CvGenerator;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.model.chat.ChatModel;
import util.AgenticScopePrinter;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.Map;

public class _2b_Sequential_Agent_Example_Typed {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 150);  // 控制模型调用的日志输出量
    }

    /**
     * 我们将实现与 2a 相同的顺序工作流，但这次我们将
     * - 为组合智能体使用类型化接口（SequenceCvGenerator）
     * - 这将允许我们使用带参数的方法，而不是 .invoke(argsMap)
     * - 以自定义方式收集输出
     * - 在调用后检索和检查 AgenticScope，用于调试或测试目的
     */

    // 1. 定义驱动智能体的模型
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. 在本包中定义顺序智能体接口：
        //      - SequenceCvGenerator.java
        // 方法签名为：
        // ResultWithAgenticScope<Map<String, String>> generateTailoredCv(@V("lifeStory") String lifeStory, @V("instructions") String instructions);

        // 3. 像之前一样使用 AgenticServices 创建两个子智能体
        CvGenerator cvGenerator = AgenticServices
                .agentBuilder(CvGenerator.class)
                .chatModel(CHAT_MODEL)
                .outputKey("masterCv") // 如果你想将此变量从智能体1传递到智能体2，
                // 请确保此处的输出键名与第二个智能体接口 agent_interfaces/CvTailor.java 中
                // 指定的输入变量名匹配
                .build();
        CvTailor cvTailor = AgenticServices
                .agentBuilder(CvTailor.class)
                .chatModel(CHAT_MODEL) // 注意，也可以为不同的智能体使用不同的模型
                .outputKey("tailoredCv") // 我们需要定义输出对象的键名
                // 如果在这里填 "masterCv"，原始主简历将被第二个智能体覆盖。
                // 在这种情况下我们不希望如此，但这是一个有用的功能。
                .build();


        // 4. 从 resources/documents/ 中的文本文件加载参数
        // （这次不需要放入 Map 中）
        // - user_life_story.txt
        // - job_description_backend.txt
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");
        String instructions = "根据以下职位描述调整简历。" + StringLoader.loadFromResource("/documents/job_description_backend.txt");

        // 5. 构建带自定义输出处理的类型化顺序工作流
        SequenceCvGenerator sequenceCvGenerator = AgenticServices
                .sequenceBuilder(SequenceCvGenerator.class) // 在此指定类型化接口
                .subAgents(cvGenerator, cvTailor)
                .outputKey("bothCvsAndLifeStory")
                .output(agenticScope -> { // 可以使用任何方法，但我们收集了一些内部变量。
                    Map<String, String> bothCvsAndLifeStory = Map.of(
                            "lifeStory", agenticScope.readState("lifeStory", ""),
                            "masterCv", agenticScope.readState("masterCv", ""),
                            "tailoredCv", agenticScope.readState("tailoredCv", "")
                    );
                    return bothCvsAndLifeStory;
                    })
                .build();

        // 6. 调用类型化组合智能体
        ResultWithAgenticScope<Map<String,String>> bothCvsAndScope = sequenceCvGenerator.generateTailoredCv(lifeStory, instructions);

        // 7. 提取结果和 agenticScope
        AgenticScope agenticScope = bothCvsAndScope.agenticScope();
        Map<String,String> bothCvsAndLifeStory = bothCvsAndScope.result();

        System.out.println("=== USER INFO (input) ===");
        String userStory = bothCvsAndLifeStory.get("lifeStory");
        System.out.println(userStory.length() > 100 ? userStory.substring(0, 100) + " [truncated...]" : lifeStory);
        System.out.println("=== MASTER CV TYPED (intermediary variable) ===");
        String masterCv = bothCvsAndLifeStory.get("masterCv");
        System.out.println(masterCv.length() > 100 ? masterCv.substring(0, 100) + " [truncated...]" : masterCv);
        System.out.println("=== TAILORED CV TYPED (output) ===");
        String tailoredCv = bothCvsAndLifeStory.get("tailoredCv");
        System.out.println(tailoredCv.length() > 100 ? tailoredCv.substring(0, 100) + " [truncated...]" : tailoredCv);

        // 无类型和类型化智能体会产生相同的 tailoredCv 结果
        // （任何差异都是由于 LLM 的非确定性造成的），
        // 但类型化智能体使用更优雅，并且由于编译时类型检查更安全

        System.out.println("=== AGENTIC SCOPE ===");
        System.out.println(AgenticScopePrinter.printPretty(agenticScope, 100));
        // 这将返回此对象（已填充）：
        // AgenticScope {
        //     memoryId = "e705028d-e90e-47df-9709-95953e84878c",
        //             state = {
        //                     bothCvsAndLifeStory = { // 输出
        //                             masterCv = "...",
        //                            lifeStory = "...",
        //                            tailoredCv = "..."
        //                     },
        //                     instructions = "...", // 输入和中间变量
        //                     tailoredCv = "...",
        //                     masterCv = "...",
        //                     lifeStory = "..."
        //             }
        // }
        System.out.println("=== CONTEXT AS CONVERSATION (all messages in the conversation) ===");
        System.out.println(AgenticScopePrinter.printConversation(agenticScope.contextAsConversation(), 100));

    }
}