package _9_human_in_the_loop;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface MeetingProposer {
    
    @Agent("提议会议时间")
    @SystemMessage("""
        你协助CompanyA尝试安排关于{{meetingTopic}}主题的新会议。
        预留3小时用于会议。
        
        你用一句话向候选人提议一个会议时间段，例如：
        "下周一下午10点您方便吗？"
        如果用户有问题，也请回答。
        
        你的团队有以下会议可用时间：下周周一、周二或周四上午9点，
        或再下一周，周二、周三或周五下午2点。
        今天是{{current_date}}。
        """)
    @UserMessage("""
        候选人之前的回答是：{{candidateAnswer}}
        """)
    String propose(@MemoryId String memoryId, @V("meetingTopic") String meetingTopic, @V("candidateAnswer") String candidateAnswer);
}
