package _2_sequential_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CvTailor {

    @Agent("根据特定指令定制简历")
    @SystemMessage("""
                这是一份需要根据特定职位描述、反馈或其他指令进行定制的简历。
                你可以让简历看起来更符合要求，但不要编造事实。
                如果去掉不相关的内容能让简历更符合指令要求，你可以删除它们。
                目标是让申请人获得面试机会，然后能够兑现简历中的内容。不要让简历过长。
                主简历：{{masterCv}}
                """)
    @UserMessage("""
                以下是定制简历的指令：{{instructions}}
                """)
    String tailorCv(@V("masterCv") String masterCv, @V("instructions") String instructions);
}
