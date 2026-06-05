import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static org.mapdb.Serializer.INTEGER;
import static org.mapdb.Serializer.STRING;

public class _09_ServiceWithPersistentMemoryForEachUserExample {

    interface Assistant {

        String chat(@MemoryId int memoryId, @UserMessage String userMessage);
    }

    public static void main(String[] args) {

        PersistentChatMemoryStore store = new PersistentChatMemoryStore();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-v4-flash")
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemoryProvider(chatMemoryProvider)
                .build();

        System.out.println(assistant.chat(1, "你好，我的名字是克劳斯"));
        System.out.println(assistant.chat(2, "嗨，我的名字是弗朗辛"));

        // 现在，注释掉上面的两行，取消下面两行的注释，然后重新运行。

        // System.out.println(assistant.chat(1, "我的名字是什么？"));
        // System.out.println(assistant.chat(2, "我的名字是什么？"));
    }

    // You can create your own implementation of ChatMemoryStore and store chat memory whenever you'd like
    static class PersistentChatMemoryStore implements ChatMemoryStore {

        //- 使用 MapDB 创建一个文件数据库（ multi-user-chat-memory.db ）
        //- 启用事务支持（ transactionEnable() ），确保数据一致性
        //MapDB是内嵌数据库，不需要单独安装数据库服务器，只需要在Java应用中引入MapDB依赖即可，以文件形式存储数据
        private final DB db = DBMaker.fileDB("multi-user-chat-memory.db").transactionEnable().make();
        //创建一个哈希映射（ messages ），用于存储每个用户的聊天记录
        //"messages" 是集合名 ，类似数据库表名，一个文件数据库里可以有多个集合，所以在创建时需要指定集合名，这里使用 INTEGER, STRING 分别表示键和值的类型
        private final Map<Integer, String> map = db.hashMap("messages", INTEGER, STRING).createOrOpen();

        @Override
        public List<ChatMessage> getMessages(Object memoryId) {
            String json = map.get((int) memoryId);//根据 memoryId 从数据库中获取对应的 JSON 字符串
            return messagesFromJson(json);//将 JSON 字符串转换为 ChatMessage 列表
        }

        @Override
        public void updateMessages(Object memoryId, List<ChatMessage> messages) {
            String json = messagesToJson(messages);
            map.put((int) memoryId, json);
            db.commit();    //开启了事务支持，所以这里需要提交事务，确保数据一致性
        }

        @Override
        public void deleteMessages(Object memoryId) {
            map.remove((int) memoryId);
            db.commit();    //开启了事务支持，所以这里需要提交事务，确保数据一致性
        }
    }
}