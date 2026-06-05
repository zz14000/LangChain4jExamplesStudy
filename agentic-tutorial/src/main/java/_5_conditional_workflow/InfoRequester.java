package _5_conditional_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface InfoRequester {

    @Agent("向候选人发送邮件以获取额外信息")
    @SystemMessage("""
            你向候选人发送友好的邮件，请求公司在审查申请时需要的额外信息。
            请明确说明他们的申请仍在考虑中。
            """)
    @UserMessage("""
            包含缺失信息描述的HR审查：{{cvReview}}
            
            候选人联系信息：{{candidateContact}}
            
            职位描述：{{jobDescription}}
            """)
    String send(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription, @V("cvReview") CvReview hrReview);
}
