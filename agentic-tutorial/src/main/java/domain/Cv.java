package domain;

import dev.langchain4j.model.output.structured.Description;

public class Cv {
    @Description("候选人的技能，逗号分隔")
    private String skills;

    @Description("候选人的职业经历")
    private String professionalExperience;

    @Description("候选人的教育背景")
    private String studies;

    @Override
    public String toString() {
        return "CV:\n" +
                "skills = \"" + skills + "\"\n" +
                "professionalExperience = \"" + professionalExperience + "\"\n" +
                "studies = \"" + studies + "\"\n";
    }
}
