package _3_loop_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface ScoredCvTailor {

    @Agent("根据特定指令定制简历")
    @SystemMessage("""
            这是一份需要根据特定职位描述、反馈或其他指令进行定制的简历。
            你可以让简历看起来更符合要求，但不要编造事实。
            如果去掉不相关的内容能让简历更符合指令要求，你可以删除它们。
            目标是让申请人获得面试机会，然后能够兑现简历中的内容。
            当前简历：{{cv}}
            """)
    @UserMessage("""
            以下是定制简历的指令和反馈：
            （再次强调，不要编造原始简历中没有的事实。
            如果申请人不太合适，请突出他最匹配的现有特征，
            但不要捏造事实）
            审查结果：{{cvReview}}
            """)
    String tailorCv(@V("cv") String cv, @V("cvReview") CvReview cvReview);
}
