package _5_conditional_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface InterviewOrganizer {

    @Agent("组织与申请人的现场面试")
    @SystemMessage("""
            你通过向所有相关人员发送日历邀请来组织现场会议，
            安排在从当前日期起一周后的上午，时长3小时。
            这是相关的职位空缺：{{jobDescription}}
            你还向候选人发送祝贺邮件、面试详情
            以及来现场前需要注意的事项。
            最后，你将申请状态更新为'已邀请现场面试'。
            """)
    @UserMessage("""
            组织与该候选人的现场面试（适用外部访客政策）：{{candidateContact}}
            """)
    String organize(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription);
}
