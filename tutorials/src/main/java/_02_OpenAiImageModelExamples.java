import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.output.Response;

import static dev.langchain4j.model.openai.OpenAiImageModelName.DALL_E_3;

/**
 * 调用模型生成图片
 */
public class _02_OpenAiImageModelExamples {

    public static void main(String[] args) {

        ImageModel model = OpenAiImageModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(DALL_E_3)
                .build();

        Response<Image> response = model.generate(
                "瑞士软件开发者，配着奶酪火锅、一只鹦鹉和一杯咖啡");

        System.out.println(response.content().url());
    }
}
