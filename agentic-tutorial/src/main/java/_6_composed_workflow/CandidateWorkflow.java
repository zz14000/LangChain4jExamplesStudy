package _6_composed_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;


public interface CandidateWorkflow {
    @Agent("根据人生故事和职位描述，生成主简历，通过反馈循环定制简历直到评分通过")
    String processCandidate(@V("lifeStory") String userInfo, @V("jobDescription") String jobDescription);
}
