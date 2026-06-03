import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;

public class _08_AIServiceExamples {

    static ChatModel model = OpenAiChatModel.builder()
            .apiKey(ApiKeys.OPENAI_API_KEY)
            .baseUrl("https://api.deepseek.com")
            .modelName("deepseek-v4-flash")
            .timeout(ofSeconds(60))
            .build();

    ////////////////// SIMPLE EXAMPLE //////////////////////

    static class Simple_AI_Service_Example {

        interface Assistant {

            String chat(String message);
        }

        public static void main(String[] args) {

            Assistant assistant = AiServices.create(Assistant.class, model);

            String userMessage = "翻译'Plus-Values des cessions de valeurs mobilières, de droits sociaux et gains assimilés'";

            String answer = assistant.chat(userMessage);

            System.out.println(answer);
        }
    }

    ////////////////// WITH MESSAGE AND VARIABLES //////////////////////

    static class AI_Service_with_System_Message_Example {

        interface Chef {

            @SystemMessage("你是一位专业厨师。你友好、有礼貌且简洁。")
            String answer(String question);
        }

        public static void main(String[] args) {

            Chef chef = AiServices.create(Chef.class, model);

            String answer = chef.answer("我应该烤鸡肉多久？");

            System.out.println(answer); // Grilling chicken usually takes around 10-15 minutes per side ...
        }
    }

    static class AI_Service_with_System_and_User_Messages_Example {

        interface TextUtils {

            @SystemMessage("你是一位专业的{{language}}翻译员")
            @UserMessage("将以下文本翻译成{{language}}：{{text}}")
            String translate(@V("text") String text, @V("language") String language);

            @SystemMessage("将用户的每条消息总结成{{n}}个要点。只提供要点。")
            List<String> summarize(@UserMessage String text, @V("n") int n);
        }

        public static void main(String[] args) {

            TextUtils utils = AiServices.create(TextUtils.class, model);

            String translation = utils.translate("你好，你好吗？", "意大利语");
            System.out.println(translation); // Ciao, come stai?

            String text = "人工智能（AI）是计算机科学的一个分支，旨在创造能够模拟人类智能的机器。"
                    + "这可以涵盖从识别模式或语音等简单任务，到做出决策或预测等更复杂的任务。";

            List<String> bulletPoints = utils.summarize(text, 3);
            bulletPoints.forEach(System.out::println);
            // [
            // "- AI is a branch of computer science",
            // "- It aims to create machines that mimic human intelligence",
            // "- It can perform simple or complex tasks"
            // ]
        }
    }

    //////////////////// EXTRACTING DIFFERENT DATA TYPES ////////////////////

    static class Sentiment_Extracting_AI_Service_Example {

        enum Sentiment {
            POSITIVE, NEUTRAL, NEGATIVE
        }

        interface SentimentAnalyzer {

            @UserMessage("分析{{it}}的情感")
            Sentiment analyzeSentimentOf(String text);

            @UserMessage("{{it}}是否有积极的情感？")
            boolean isPositive(String text);
        }

        public static void main(String[] args) {

            SentimentAnalyzer sentimentAnalyzer = AiServices.create(SentimentAnalyzer.class, model);

            Sentiment sentiment = sentimentAnalyzer.analyzeSentimentOf("这很好！");
            System.out.println(sentiment); // POSITIVE

            boolean positive = sentimentAnalyzer.isPositive("这很糟糕！");
            System.out.println(positive); // false
        }
    }

    static class Hotel_Review_AI_Service_Example {

        public enum IssueCategory {
            MAINTENANCE_ISSUE,
            SERVICE_ISSUE,
            COMFORT_ISSUE,
            FACILITY_ISSUE,
            CLEANLINESS_ISSUE,
            CONNECTIVITY_ISSUE,
            CHECK_IN_ISSUE,
            OVERALL_EXPERIENCE_ISSUE
        }

        interface HotelReviewIssueAnalyzer {

            @UserMessage("请分析以下评论：|||{{it}}|||")
            List<IssueCategory> analyzeReview(String review);
        }

        public static void main(String[] args) {

            HotelReviewIssueAnalyzer hotelReviewIssueAnalyzer = AiServices.create(HotelReviewIssueAnalyzer.class, model);

            String review = "我们入住酒店的体验喜忧参半。位置非常完美，距离海滩仅一步之遥，" +
                    "这让我们每天的出行都非常方便。房间宽敞，装饰精美，" +
                    "提供舒适宜人的环境。然而，我们在" +
                    "住宿期间遇到了几个问题。房间里的空调无法正常工作，让夜晚非常不舒服。" +
                    "此外，客房服务很慢，我们不得不多次打电话才拿到额外的毛巾。尽管有" +
                    "友好的员工和令人愉快的早餐自助餐，但这些" +
                    "问题严重影响了我们的住宿体验。";

            List<IssueCategory> issueCategories = hotelReviewIssueAnalyzer.analyzeReview(review);

            // Should output [MAINTENANCE_ISSUE, SERVICE_ISSUE, COMFORT_ISSUE, OVERALL_EXPERIENCE_ISSUE]
            System.out.println(issueCategories);
        }
    }

    static class Number_Extracting_AI_Service_Example {

        interface NumberExtractor {

            @UserMessage("从{{it}}中提取数字")
            int extractInt(String text);

            @UserMessage("从{{it}}中提取数字")
            long extractLong(String text);

            @UserMessage("从{{it}}中提取数字")
            BigInteger extractBigInteger(String text);

            @UserMessage("从{{it}}中提取数字")
            float extractFloat(String text);

            @UserMessage("从{{it}}中提取数字")
            double extractDouble(String text);

            @UserMessage("从{{it}}中提取数字")
            BigDecimal extractBigDecimal(String text);
        }

        public static void main(String[] args) {

            NumberExtractor extractor = AiServices.create(NumberExtractor.class, model);

            String text = "经过无数千年的计算，超级计算机'深思'终于宣布，"
                    + "关于生命、宇宙及一切的终极问题的答案是四十二。";

            int intNumber = extractor.extractInt(text);
            System.out.println(intNumber); // 42

            long longNumber = extractor.extractLong(text);
            System.out.println(longNumber); // 42

            BigInteger bigIntegerNumber = extractor.extractBigInteger(text);
            System.out.println(bigIntegerNumber); // 42

            float floatNumber = extractor.extractFloat(text);
            System.out.println(floatNumber); // 42.0

            double doubleNumber = extractor.extractDouble(text);
            System.out.println(doubleNumber); // 42.0

            BigDecimal bigDecimalNumber = extractor.extractBigDecimal(text);
            System.out.println(bigDecimalNumber); // 42.0
        }
    }

    static class Date_and_Time_Extracting_AI_Service_Example {

        interface DateTimeExtractor {

            @UserMessage("从{{it}}中提取日期")
            LocalDate extractDateFrom(String text);

            @UserMessage("从{{it}}中提取时间")
            LocalTime extractTimeFrom(String text);

            @UserMessage("从{{it}}中提取日期和时间")
            LocalDateTime extractDateTimeFrom(String text);
        }

        public static void main(String[] args) {

            DateTimeExtractor extractor = AiServices.create(DateTimeExtractor.class, model);

            String text = "1968 年的傍晚，独立日庆祝活动的余晖中，"
                    + "一个名叫约翰的孩子在宁静的夜空下降生。";

            LocalDate date = extractor.extractDateFrom(text);
            System.out.println(date); // 1968-07-04

            LocalTime time = extractor.extractTimeFrom(text);
            System.out.println(time); // 23:45

            LocalDateTime dateTime = extractor.extractDateTimeFrom(text);
            System.out.println(dateTime); // 1968-07-04T23:45
        }
    }

    static class POJO_Extracting_AI_Service_Example {

        static class Person {

            @Description("first name of a person") // you can add an optional description to help an LLM have a better understanding
            private String firstName;
            private String lastName;
            private LocalDate birthDate;

            @Override
            public String toString() {
                return "Person {" +
                        " firstName = \"" + firstName + "\"" +
                        ", lastName = \"" + lastName + "\"" +
                        ", birthDate = " + birthDate +
                        " }";
            }
        }

        interface PersonExtractor {

            @UserMessage("从以下文本中提取人物信息：{{it}}")
            Person extractPersonFrom(String text);
        }

        public static void main(String[] args) {

            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .modelName(GPT_4_O_MINI)
                    // 当使用支持"json mode"功能的大语言模型提取 POJO 时
                    // （如 OpenAI、Azure OpenAI、Vertex AI Gemini、Ollama 等），
                    // 建议启用该功能（json mode）以获得更可靠的结果。
                    // 使用此功能时，LLM 将被强制输出有效的 JSON。
                    .responseFormat("json_schema")
                    .strictJsonSchema(true) // https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-json-mode
                    .timeout(ofSeconds(60))
                    .build();

            PersonExtractor extractor = AiServices.create(PersonExtractor.class, model);

            String text = "1968 年，在独立日逐渐消逝的余晖中，"
                    + "一个名叫约翰·多伊的孩子在宁静的傍晚降生。";

            Person person = extractor.extractPersonFrom(text);

            System.out.println(person); // Person { firstName = "John", lastName = "Doe", birthDate = 1968-07-04 }
        }
    }

    ////////////////////// DESCRIPTIONS ////////////////////////

    static class POJO_With_Descriptions_Extracting_AI_Service_Example {

        static class Recipe {

            @Description("简短标题，最多 3 个词")
            private String title;

            @Description("简短描述，最多 2 句话")
            private String description;

            @Description("每个步骤用 6 到 8 个词描述，步骤之间要押韵")
            private List<String> steps;

            private Integer preparationTimeMinutes;

            @Override
            public String toString() {
                return "食谱 {" +
                        " 标题 = \"" + title + "\"" +
                        ", 描述 = \"" + description + "\"" +
                        ", 步骤 = " + steps +
                        ", 准备时间（分钟） = " + preparationTimeMinutes +
                        " }";
            }
        }

        @StructuredPrompt("使用仅能使用{{ingredients}}制作的{{dish}}创建一个食谱")
        static class CreateRecipePrompt {

            private String dish;
            private List<String> ingredients;
        }

        interface Chef {

            Recipe createRecipeFrom(String... ingredients);

            Recipe createRecipe(CreateRecipePrompt prompt);
        }

        public static void main(String[] args) {

            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .modelName(GPT_4_O_MINI)
                    // 当使用支持"json mode"功能的大语言模型提取 POJO 时
                    //（如 OpenAI、Azure OpenAI、Vertex AI Gemini、Ollama 等），
                    // 建议启用该功能（json mode）以获得更可靠的结果。
                    // 使用此功能时，LLM 将被强制输出有效的 JSON。
                    .responseFormat("json_schema")
                    .strictJsonSchema(true) // https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-json-mode
                    .timeout(ofSeconds(60))
                    .build();

            Chef chef = AiServices.create(Chef.class, model);

            Recipe recipe = chef.createRecipeFrom("黄瓜", "番茄", "菲达奶酪", "洋葱", "橄榄", "柠檬");

            System.out.println(recipe);
            // Recipe {
            // title = "Greek Salad",
            // description = "A refreshing mix of veggies and feta cheese in a zesty
            // dressing.",
            // steps = [
            // "Chop cucumber and tomato",
            // "Add onion and olives",
            // "Crumble feta on top",
            // "Drizzle with dressing and enjoy!"
            // ],
            // preparationTimeMinutes = 10
            // }

            CreateRecipePrompt prompt = new CreateRecipePrompt();
            prompt.dish = "oven dish";
            prompt.ingredients = asList("cucumber", "tomato", "feta", "onion", "olives", "potatoes");

            Recipe anotherRecipe = chef.createRecipe(prompt);
            System.out.println(anotherRecipe);
            // Recipe ...
        }
    }


    ////////////////////////// WITH MEMORY /////////////////////////

    static class ServiceWithMemoryExample {

        interface Assistant {

            String chat(String message);
        }

        public static void main(String[] args) {

            ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatModel(model)
                    .chatMemory(chatMemory)
                    .build();

            String answer = assistant.chat("Hello! My name is Klaus.");
            System.out.println(answer); // Hello Klaus! How can I assist you today?

            String answerWithName = assistant.chat("What is my name?");
            System.out.println(answerWithName); // Your name is Klaus.
        }
    }

    static class ServiceWithMemoryForEachUserExample {

        interface Assistant {

            String chat(@MemoryId int memoryId, @UserMessage String userMessage);
        }

        public static void main(String[] args) {

            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatModel(model)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                    .build();

            System.out.println(assistant.chat(1, "Hello, my name is Klaus"));
            // Hi Klaus! How can I assist you today?

            System.out.println(assistant.chat(2, "Hello, my name is Francine"));
            // Hello Francine! How can I assist you today?

            System.out.println(assistant.chat(1, "What is my name?"));
            // Your name is Klaus.

            System.out.println(assistant.chat(2, "What is my name?"));
            // Your name is Francine.
        }
    }
}
