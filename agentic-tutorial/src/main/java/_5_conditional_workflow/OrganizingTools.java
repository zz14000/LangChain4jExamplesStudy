package _5_conditional_workflow;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrganizingTools {

    @Tool
    public Date getCurrentDate(){
        return new Date();
    }

    @Tool("查找需要参加给定职位描述ID的现场面试的员工的邮箱地址和姓名")
    public List<String> getInvolvedEmployeesForInterview(@P("职位描述ID") String jobDescriptionId){
        // 演示用的模拟实现
        return new ArrayList<>(List.of(
                "Anna Bolena: hiring.manager@company.com",
                "Chris Durue: near.colleague@company.com",
                "Esther Finnigan: vp@company.com"));
    }

    @Tool("根据邮箱地址为员工创建日程条目")
    public void createCalendarEntry(@P("员工邮箱地址列表") List<String> emailAddress, @P("会议主题") String topic, @P("开始日期和时间，格式 yyyy-mm-dd hh:mm") String start, @P("结束日期和时间，格式 yyyy-mm-dd hh:mm") String end){
        // 演示用的模拟实现
        System.out.println("*** CALENDAR ENTRY CREATED ***");
        System.out.println("Topic: " + topic);
        System.out.println("Start: " + start);
        System.out.println("End: " + end);
    }

    @Tool
    public int sendEmail(@P("收件人邮箱地址列表") List<String> to, @P("抄送邮箱地址列表") List<String> cc, @P("邮件主题") String subject, @P("正文") String body){
        // 演示用的模拟实现
        System.out.println("*** EMAIL SENT ***");
        System.out.println("To: " + to);
        System.out.println("Cc: " + cc);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        return 1234; // 模拟邮件ID
    }

    @Tool
    public void updateApplicationStatus(@P("职位描述ID") String jobDescriptionId, @P("候选人（名，姓）") String candidateName, @P("新的申请状态") String newStatus){
        // 演示用的模拟实现
        System.out.println("*** APPLICATION STATUS UPDATED ***");
        System.out.println("Job Descirption ID: " + jobDescriptionId);
        System.out.println("Candidate Name: " + candidateName);
        System.out.println("New Status: " + newStatus);
    }
}
