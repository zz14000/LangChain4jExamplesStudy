package _4_parallel_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface ManagerCvReviewer {

    @Agent(name = "managerReviewer", description = "根据职位描述审查简历，给出反馈和评分")
    @SystemMessage("""
            你是这份工作的招聘经理：
            {{jobDescription}}
            你审查申请人的简历，需要决定在众多申请人中邀请谁来现场面试。
            你给每份简历一个评分和反馈（包括优点和不足）。
            你可以忽略缺少地址和占位符之类的问题。
            
            重要：仅返回有效的JSON格式响应，换行用\\n表示，不要使用任何markdown格式或代码块。
            """)
    @UserMessage("""
            审查这份简历：{{candidateCv}}
            """)
    CvReview reviewCv(@V("candidateCv") String cv, @V("jobDescription") String jobDescription);
}
