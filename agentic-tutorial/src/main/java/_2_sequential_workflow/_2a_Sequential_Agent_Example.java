package _2_sequential_workflow;

import _1_basic_agent.CvGenerator;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.Map;

public class _2a_Sequential_Agent_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // 控制模型调用的日志输出量
    }

    /**
     * 本示例演示了如何实现两个智能体：
     * - CvGenerator（接收人生故事并生成完整的主简历）
     * - CvTailor（接收主简历并根据特定指令（职位描述、反馈等）进行定制）
     * 然后我们将使用 sequenceBuilder 按固定顺序依次调用它们，
     * 并演示如何在它们之间传递参数。
     * 当组合多个智能体时，所有输入、中间和输出参数以及调用链
     * 都存储在 AgenticScope 中，可在高级用例中访问。
     */

    // 1. 定义驱动智能体的模型
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. 在本包中定义两个子智能体：
        //      - CvGenerator.java
        //      - CvTailor.java

        // 3. 使用 AgenticServices 创建两个智能体
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

        ////////////////// 无类型示例 //////////////////////

        // 4. 构建顺序工作流
        UntypedAgent tailoredCvGenerator = AgenticServices // 除非定义了结果组合智能体，否则使用 UntypedAgent，见下文
                .sequenceBuilder()
                .subAgents(cvGenerator, cvTailor) // 可以添加任意多个智能体，顺序很重要
                .outputKey("tailoredCv") // 这是组合智能体的最终输出
                // 注意，你可以使用 AgenticScope 中的任何字段作为输出
                // 例如你可以输出 'masterCv' 而不是 tailoredCv（尽管在这种情况下没有意义）
                .build();

        // 4. 从 resources/documents/ 中的文本文件加载参数
        // - user_life_story.txt
        // - job_description_backend.txt
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");
        String instructions = "根据以下职位描述调整简历。" + StringLoader.loadFromResource("/documents/job_description_backend.txt");

        // 5. 因为使用无类型智能体，我们需要传递参数映射
        Map<String, Object> arguments = Map.of(
                "lifeStory", lifeStory, // 与 agent_interfaces/CvGenerator.java 中的变量名匹配
                "instructions", instructions // 与 agent_interfaces/CvTailor.java 中的变量名匹配
        );

        // 5. 调用组合智能体生成定制简历
        String tailoredCv = (String) tailoredCvGenerator.invoke(arguments);

        // 6. 打印生成的简历
        System.out.println("=== TAILORED CV UNTYPED ===");
        System.out.println((String) tailoredCv); // 你可以观察到，当使用 job_description_fullstack.txt 作为输入时，
        // 简历看起来会非常不同

        // 在示例 2b 中，我们将构建相同的顺序智能体，但使用类型化输出，
        // 并检查 AgenticScope

    }
}