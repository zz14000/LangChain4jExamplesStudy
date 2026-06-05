package _9_human_in_the_loop;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface HiringDecisionProposer {
    
    @Agent("总结招聘决策供最终验证")
    @SystemMessage("""
        你用最多3行总结给定审查的招聘理由，
        供人类做出最终决定是否继续。
        """)
    @UserMessage("""
        招聘流程中所有相关方的反馈：{{cvReview}}
        """)
    String propose(@V("cvReview") CvReview cvReview);
}
