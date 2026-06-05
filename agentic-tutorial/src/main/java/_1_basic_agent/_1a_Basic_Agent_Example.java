package _1_basic_agent;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;

public class _1a_Basic_Agent_Example {

    /**
     * 本示例演示了如何实现一个基本智能体，以展示语法用法。
     * 注意，智能体只有与其他智能体组合使用时才有意义，我们将在后续步骤中展示。
     * 如果只需要一个智能体，最好直接使用 AiService。
     *
     * 这个基本智能体将用户的人生故事转换为一份简洁完整的简历。
     * 注意，运行可能需要一些时间，因为生成的简历内容较长，
     * 模型需要一定时间来处理。
     */

    // 设置日志级别
    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // 控制模型调用的日志输出量
    }

    // 1. 定义驱动智能体的模型
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel("DEEPSEEK");

    public static void main(String[] args) throws IOException {

        // 2. 在 agent_interfaces/CvGenerator.java 中定义智能体行为

        // 3. 使用 AgenticServices 创建智能体
        CvGenerator cvGenerator = AgenticServices
                .agentBuilder(CvGenerator.class)
                .chatModel(CHAT_MODEL)
                .outputKey("masterCv") // 可以选择性地定义输出对象的键名
                .build();

        // 4. 从 resources/documents/user_life_story.txt 加载文本文件
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");

        // 5. 调用智能体生成简历
        String cv = cvGenerator.generateCv(lifeStory);

        // 6. 打印生成的简历
        System.out.println("=== CV ===");
        System.out.println(cv);

        // 在示例 1b 中，我们将构建相同的智能体，但使用结构化输出

    }
}