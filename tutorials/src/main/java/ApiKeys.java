import static dev.langchain4j.internal.Utils.getOrDefault;

public class ApiKeys {

    //AI加工数据
    public static final String OPENAI_API_KEY = getOrDefault(System.getenv("OPENAI_API_KEY"), "demo");

    //第3方API聚合超市，用来拿原始业务数据的
    public static final String RAPID_API_KEY = System.getenv("RAPID_API_KEY");
}
