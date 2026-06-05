package _5_conditional_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface EmailAssistant {

    @Agent("向未通过的候选人发送拒绝邮件，返回已发送的邮件ID，如果无法发送则返回0")
    @SystemMessage("""
            你向未通过初审的申请候选人发送一封友好的邮件。
            你还将申请状态更新为'已拒绝'。
            你返回已发送的邮件ID。
            """)
    @UserMessage("""
            被拒绝的候选人：{{candidateContact}}
            
            应聘职位：{{jobDescription}}
            """)
    int send(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription);
}
