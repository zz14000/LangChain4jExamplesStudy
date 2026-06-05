package domain;

import dev.langchain4j.model.output.structured.Description;

public class CvReview {
    @Description("从0到1的评分，表示你邀请该候选人面试的可能性")
    public double score;

    @Description("对简历的反馈，优点、需要改进的地方、缺少的技能、危险信号等")
    public String feedback;

    public CvReview() {} // 反序列化需要的无参构造函数，因为存在其他构造函数！

    public CvReview(double score, String feedback) {
        this.score = score;
        this.feedback = feedback;
    }

    @Override
    public String toString() {
        return "\nCvReview: " +
                " - score = " + score +
                "\n- feedback = \"" + feedback + "\"\n";
    }
}
