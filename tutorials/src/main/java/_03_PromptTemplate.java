import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.input.structured.StructuredPromptProcessor;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;

/**
 * 简单提示词构建，格式化提示词构建，可以像标准输出一样使用占位符
 */
public class _03_PromptTemplate {

    static class Simple_Prompt_Template_Example {

        public static void main(String[] args) {

            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .baseUrl("https://api.deepseek.com")
                    .modelName("deepseek-v4-flash")
                    .timeout(ofSeconds(60))
                    .build();

            String template = "为{{dishType}}创建一个食谱，使用以下食材：{{ingredients}}";
            PromptTemplate promptTemplate = PromptTemplate.from(template);

            Map<String, Object> variables = new HashMap<>();
            variables.put("dishType", "烤箱菜肴");
            variables.put("ingredients", "土豆、番茄、菲达奶酪、橄榄油");

            Prompt prompt = promptTemplate.apply(variables);

            String response = model.chat(prompt.text());

            System.out.println(response);
        }

    }

    static class Structured_Prompt_Template_Example {
        @StructuredPrompt({
                "使用仅能使用{{ingredients}}制作的{{dish}}创建一个食谱。",
                "按照以下结构组织你的回答：",

                "食谱名称：...",
                "描述：...",
                "准备时间：...",

                "所需食材：",
                "- ...",
                "- ...",

                "制作步骤：",
                "- ...",
                "- ..."
        })
        static class CreateRecipePrompt {

            String dish;
            List<String> ingredients;

            CreateRecipePrompt(String dish, List<String> ingredients) {
                this.dish = dish;
                this.ingredients = ingredients;
            }
        }

        public static void main(String[] args) {

            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .baseUrl("https://api.deepseek.com")
                    .modelName("deepseek-v4-flash")
                    .timeout(ofSeconds(60))
                    .build();

            Structured_Prompt_Template_Example.CreateRecipePrompt createRecipePrompt = new Structured_Prompt_Template_Example.CreateRecipePrompt(
                    "沙拉",
                    asList("黄瓜", "番茄", "菲达奶酪", "洋葱", "橄榄")
            );

            Prompt prompt = StructuredPromptProcessor.toPrompt(createRecipePrompt);

            String recipe = model.chat(prompt.text());

            System.out.println(recipe);
        }
    }
}
