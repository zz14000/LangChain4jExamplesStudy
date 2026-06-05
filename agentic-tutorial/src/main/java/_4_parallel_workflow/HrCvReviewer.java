package _4_parallel_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface HrCvReviewer {

    @Agent(name = "hrReviewer", description = "审查简历以检查候选人是否符合HR要求，给出反馈和评分")
    @SystemMessage("""
            你是HR部门的工作人员，审查简历以填补具有以下要求的职位：
            {{hrRequirements}}
            你给每份简历一个评分和反馈（包括优点和不足）。
            你可以忽略缺少地址和占位符之类的问题。
            
            重要：仅返回有效的JSON格式响应，换行用\\n表示，不要使用任何markdown格式或代码块。
            """)
    @UserMessage("""
            审查这份简历：{{candidateCv}}，附带电话面试记录：{{phoneInterviewNotes}}
            """)
    CvReview reviewCv(@V("candidateCv") String cv, @V("phoneInterviewNotes") String phoneInterviewNotes, @V("hrRequirements") String hrRequirements);
}
