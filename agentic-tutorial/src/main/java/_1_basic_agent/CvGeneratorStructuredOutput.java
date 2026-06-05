package _1_basic_agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.Cv;

public interface CvGeneratorStructuredOutput {
    @UserMessage("""
            以下是我的生活和职业轨迹信息，
            请将其整理为一份简洁完整的简历。
            不要编造事实，也不要遗漏技能或经历。
            这份简历稍后还会被进一步润色，现在请确保内容完整。
            我的人生故事：{{lifeStory}}
            """)
    @Agent("根据用户提供的信息生成一份简洁的简历")
    Cv generateCv(@V("lifeStory") String userInfo);
}
