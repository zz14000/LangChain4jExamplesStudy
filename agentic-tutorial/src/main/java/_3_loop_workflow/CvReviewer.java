package _3_loop_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface CvReviewer {

    @Agent("根据特定指令审查简历，给出反馈和评分。考虑简历与职位的匹配程度")
    @SystemMessage("""
            你是这份工作的招聘经理：
            {{jobDescription}}
            你审查申请人的简历，需要决定在众多申请人中邀请谁来现场面试。
            你给每份简历一个评分和反馈（包括优点和不足）。
            你可以忽略缺少地址和占位符之类的问题。
            """)
    @UserMessage("""
            审查这份简历：{{cv}}
            """)
    CvReview reviewCv(@V("cv") String cv, @V("jobDescription") String jobDescription);
}
