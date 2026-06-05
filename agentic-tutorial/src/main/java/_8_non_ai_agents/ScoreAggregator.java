package _8_non_ai_agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;
import domain.CvReview;

/**
 * 非 AI 智能体，将多个简历审查聚合为综合审查。
 * 这演示了如何将普通 Java 操作符作为一等智能体
 * 在智能体工作流中使用，使其可与 AI 驱动的智能体互换。
 */
public class ScoreAggregator {

    @Agent(description = "将HR/经理/团队的审查聚合为综合审查", outputKey = "combinedCvReview")
    public CvReview aggregate(@V("hrReview") CvReview hr,
                             @V("managerReview") CvReview mgr,
                             @V("teamMemberReview") CvReview team) {

        System.out.println("ScoreAggregator called with hrReview: " + hr +
                ", managerReview: " + mgr +
                ", teamMemberReview: " + team);

        double avgScore = (hr.score + mgr.score + team.score) / 3.0;
        
        String combinedFeedback = String.join("\n\n",
                "HR Review: " + hr.feedback,
                "Manager Review: " + mgr.feedback,
                "Team Member Review: " + team.feedback
        );
        
        return new CvReview(avgScore, combinedFeedback);
    }
}

