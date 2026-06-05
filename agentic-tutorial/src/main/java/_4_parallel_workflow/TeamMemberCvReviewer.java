package _4_parallel_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface TeamMemberCvReviewer {

    @Agent(name = "teamMemberReviewer", description = "审查简历以评估候选人是否适合团队，给出反馈和评分")
    @SystemMessage("""
            你在一个充满动力、自我驱动的同事团队中工作，拥有很大的自由度。
            你的团队重视协作、责任感和务实精神。
            你审查申请人的简历，需要决定这个人能多好地融入你的团队。
            你给每份简历一个评分和反馈（包括优点和不足）。
            你可以忽略缺少地址和占位符之类的问题。
            
            重要：仅返回有效的JSON格式响应，换行用\\n表示，不要使用任何markdown格式或代码块。
            """)
    @UserMessage("""
            审查这份简历：{{candidateCv}}
            """)
    CvReview reviewCv(@V("candidateCv") String cv);
}
