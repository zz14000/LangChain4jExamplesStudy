# Agent 入门学习指南（基于本项目）

这份指南适合“刚开始学习 Agent”的同学。目标不是一次性看完所有示例，而是用最短路径理解这个仓库里和 Agent 最相关的东西：模型调用、Prompt、AI Service、工具调用、记忆、RAG、Agentic Workflow，以及一个完整的客服 Agent 应用。

## 1. 先知道这个项目是什么

这个仓库是 LangChain4j 的示例集合，是一个多模块 Maven 项目。根目录的 `pom.xml` 只负责聚合很多子模块，每个子模块展示一种 LangChain4j 用法。

对 Agent 入门最重要的模块如下：

| 模块 | 作用 | 建议阶段 |
| --- | --- | --- |
| [`tutorials`](tutorials/src/main/java) | LangChain4j 基础教程：模型调用、Prompt、Memory、AI Service、Tools、文档问答 | 第 1 阶段 |
| [`agentic-tutorial`](agentic-tutorial/src/main/java) | Agentic API 主线教程：基础 Agent、顺序、循环、并行、条件、监督者、人参与 | 第 2 阶段 |
| [`rag-examples`](rag-examples/src/main/java) | RAG 从简单到高级：文档加载、切分、向量检索、重排、查询路由等 | 第 3 阶段 |
| [`customer-support-agent-example`](customer-support-agent-example/src/main/java/dev/langchain4j/example) | 完整业务 Agent：Spring Boot + Memory + Tools + RAG | 第 4 阶段 |
| [`open-ai-examples`](open-ai-examples/src/main/java) | OpenAI 接入细节：Chat、Streaming、Embedding、Function Calling、Observability | 辅助阅读 |
| [`mcp-example`](mcp-example/src/main/java/dev/langchain4j/example/mcp) | MCP 工具调用示例 | 进阶阅读 |

一句话理解这个项目的学习顺序：

```text
先会调用模型 -> 再会封装 AI Service -> 再会让模型使用工具和记忆 -> 再会接入知识库 RAG -> 最后学习多个 Agent 如何编排成工作流。
```

## 2. 前置知识清单

不需要一开始就把所有知识都学得很深，但下面这些概念要逐步补齐。

### 2.1 Java 基础

你至少需要理解：

- Java 17 基础语法
- 类、接口、抽象、枚举
- 注解，例如 `@SystemMessage`、`@UserMessage`、`@Tool`
- 泛型，例如 `ResultWithAgenticScope<Map<String, String>>`
- Lambda 表达式，例如 `.output(agenticScope -> { ... })`
- 异常处理，例如 `throws IOException`
- 基本 Maven 项目结构：`pom.xml`、`src/main/java`、`src/main/resources`

如果 Java 还不熟，先补到“能读懂接口、注解、Builder 链式调用”的程度即可。

### 2.2 Maven 与工程运行

你需要知道：

- 这个项目是多模块 Maven 项目
- 每个子目录基本都是一个独立示例模块
- `pom.xml` 里看依赖，比如 `langchain4j`、`langchain4j-open-ai`、`langchain4j-agentic`
- Windows 下优先用项目自带的 `mvnw.cmd`

常用命令：

```powershell
# 在项目根目录运行
.\mvnw.cmd -pl tutorials -Pcomplete package

# 编译 Agentic 教程模块
.\mvnw.cmd -pl agentic-tutorial compile

# 编译客服 Agent 示例
.\mvnw.cmd -pl customer-support-agent-example compile
```

### 2.3 LLM 基础概念

学习 Agent 前，需要先理解 LLM 的几个基本概念：

- Chat Model：聊天模型，负责根据消息生成回复
- System Message：给模型设定角色、规则、边界
- User Message：用户输入
- Prompt Template：带变量的提示词模板
- Token：模型处理文本的基本单位，会影响上下文长度和成本
- Temperature：控制输出随机性
- Structured Output：让模型输出 JSON、枚举、POJO 等结构化结果
- Streaming：流式输出
- Function Calling / Tool Calling：模型判断何时调用外部函数或工具
- Embedding：把文本变成向量
- Vector Store：存储向量，用于相似度检索
- RAG：先检索相关资料，再让模型基于资料回答

### 2.4 Agent 核心概念

刚开始可以这样理解：

- 普通 LLM 调用：输入一句话，模型回一句话
- AI Service：把 LLM 能力封装成 Java 接口
- Tool Agent：模型可以调用 Java 方法完成计算、查询、操作
- Memory Agent：模型能记住同一会话里的上下文
- RAG Agent：模型能从外部知识库检索资料再回答
- Agentic Workflow：多个 Agent 按顺序、并行、循环、条件分支、监督者模式协作

在本项目中，`agentic-tutorial` 的核心概念是：

- `AgenticServices`：创建单个 Agent 或组合 Agent 的入口
- `AgenticScope`：Agent 工作流里的共享状态，可以理解成多个 Agent 之间传递数据的上下文
- `outputKey`：某个 Agent 输出结果放入 `AgenticScope` 时使用的 key
- `subAgents`：组合工作流里的子 Agent 列表
- `ResultWithAgenticScope`：既拿到最终结果，也拿到执行过程中的内部状态

### 2.5 Spring Boot 基础

如果你要看 `customer-support-agent-example`，需要补一点 Spring Boot：

- `@SpringBootApplication`
- `@Configuration`
- `@Bean`
- Controller 基础
- `application.properties`
- 如何启动一个 Web 服务

不需要先成为 Spring 专家，但至少要知道 Bean 是如何被注入和组合的。

## 3. 环境准备

### 3.1 必备环境

- JDK 17
- Maven，或直接使用项目里的 `mvnw.cmd`
- 一个 Java IDE，例如 IntelliJ IDEA
- OpenAI API Key，或者你自己替换成其他兼容 OpenAI API 的模型提供商

### 3.2 设置 API Key

本项目大量示例会读取环境变量 `OPENAI_API_KEY`。

PowerShell 临时设置：

```powershell
$env:OPENAI_API_KEY="你的 OpenAI API Key"
```

PowerShell 持久设置：

```powershell
setx OPENAI_API_KEY "你的 OpenAI API Key"
```

注意：`setx` 只会影响新打开的终端窗口，当前窗口不会立即生效。

`agentic-tutorial/src/main/java/util/ChatModelProvider.java` 默认使用：

```java
System.getenv("OPENAI_API_KEY")
```

所以如果运行 Agentic 教程失败，第一件事先检查环境变量。

## 4. 推荐学习路线

### 第 0 阶段：先跑通一个最小示例

目标：确认环境、API Key、依赖下载都没问题。

先看：

- [`tutorials/src/main/java/_00_HelloWorld.java`](tutorials/src/main/java/_00_HelloWorld.java)

你要看懂：

- `OpenAiChatModel.builder()` 如何创建模型
- `.apiKey(...)` 从哪里来
- `.modelName(...)` 指定哪个模型
- `model.chat(...)` 是一次最普通的模型调用

建议动手：

- 把 `"Say Hello World"` 改成中文问题
- 换几个问题观察输出
- 找到 `tutorials/src/main/java/ApiKeys.java`，理解 `"demo"` key 和真实 key 的区别

### 第 1 阶段：理解 LangChain4j 基础能力

按下面顺序阅读 `tutorials/src/main/java`：

1. [`_00_HelloWorld.java`](tutorials/src/main/java/_00_HelloWorld.java)
2. [`_01_ModelParameters.java`](tutorials/src/main/java/_01_ModelParameters.java)
3. [`_03_PromptTemplate.java`](tutorials/src/main/java/_03_PromptTemplate.java)
4. [`_04_Streaming.java`](tutorials/src/main/java/_04_Streaming.java)
5. [`_05_Memory.java`](tutorials/src/main/java/_05_Memory.java)
6. [`_08_AIServiceExamples.java`](tutorials/src/main/java/_08_AIServiceExamples.java)
7. [`_10_ServiceWithToolsExample.java`](tutorials/src/main/java/_10_ServiceWithToolsExample.java)
8. [`_11_ServiceWithDynamicToolsExample.java`](tutorials/src/main/java/_11_ServiceWithDynamicToolsExample.java)
9. [`_12_ChatWithDocumentsExamples.java`](tutorials/src/main/java/_12_ChatWithDocumentsExamples.java)

这一阶段你要形成几个判断：

- 什么时候直接用 `ChatModel`
- 什么时候用 `AiServices`
- `@SystemMessage` 和 `@UserMessage` 分别控制什么
- `@Tool` 暴露的 Java 方法如何被模型调用
- Memory 只是上下文管理，不等于长期数据库
- 文档问答本质上是检索 + 生成

这里最重要的两个文件是：

- [`_08_AIServiceExamples.java`](tutorials/src/main/java/_08_AIServiceExamples.java)：理解“用接口描述 AI 能力”
- [`_10_ServiceWithToolsExample.java`](tutorials/src/main/java/_10_ServiceWithToolsExample.java)：理解“模型什么时候调用工具”

### 第 2 阶段：进入 Agentic Tutorial 主线

主线目录：

- [`agentic-tutorial/src/main/java`](agentic-tutorial/src/main/java)

目录里已经有一份索引：

- [`agentic-tutorial/src/main/java/README.md`](agentic-tutorial/src/main/java/README.md)

建议严格按顺序读：

1. [`_1_basic_agent/_1a_Basic_Agent_Example.java`](agentic-tutorial/src/main/java/_1_basic_agent/_1a_Basic_Agent_Example.java)
2. [`_1_basic_agent/_1b_Basic_Agent_Example_Structured.java`](agentic-tutorial/src/main/java/_1_basic_agent/_1b_Basic_Agent_Example_Structured.java)
3. [`_2_sequential_workflow/_2a_Sequential_Agent_Example.java`](agentic-tutorial/src/main/java/_2_sequential_workflow/_2a_Sequential_Agent_Example.java)
4. [`_2_sequential_workflow/_2b_Sequential_Agent_Example_Typed.java`](agentic-tutorial/src/main/java/_2_sequential_workflow/_2b_Sequential_Agent_Example_Typed.java)
5. [`_3_loop_workflow/_3a_Loop_Agent_Example.java`](agentic-tutorial/src/main/java/_3_loop_workflow/_3a_Loop_Agent_Example.java)
6. [`_4_parallel_workflow/_4_Parallel_Workflow_Example.java`](agentic-tutorial/src/main/java/_4_parallel_workflow/_4_Parallel_Workflow_Example.java)
7. [`_5_conditional_workflow/_5a_Conditional_Workflow_Example.java`](agentic-tutorial/src/main/java/_5_conditional_workflow/_5a_Conditional_Workflow_Example.java)
8. [`_6_composed_workflow/_6_Composed_Workflow_Example.java`](agentic-tutorial/src/main/java/_6_composed_workflow/_6_Composed_Workflow_Example.java)
9. [`_7_supervisor_orchestration/_7a_Supervisor_Orchestration.java`](agentic-tutorial/src/main/java/_7_supervisor_orchestration/_7a_Supervisor_Orchestration.java)
10. [`_9_human_in_the_loop/_9a_HumanInTheLoop_Simple_Validator.java`](agentic-tutorial/src/main/java/_9_human_in_the_loop/_9a_HumanInTheLoop_Simple_Validator.java)

这一阶段要特别关注：

- 单 Agent 其实不神秘，本质还是一个带 Prompt 的 AI Service
- Agent 的价值主要来自组合：顺序、循环、并行、条件、监督者
- `outputKey` 是多个 Agent 传递数据的关键
- `AgenticScope` 是调试 Agent 工作流最重要的对象
- Typed workflow 比 Map 参数更适合真实项目

推荐重点读这个文件：

- [`_2_sequential_workflow/_2b_Sequential_Agent_Example_Typed.java`](agentic-tutorial/src/main/java/_2_sequential_workflow/_2b_Sequential_Agent_Example_Typed.java)

它展示了：

- 如何创建多个子 Agent
- 如何把第一个 Agent 的输出传给第二个 Agent
- 如何自定义最终输出
- 如何打印 `AgenticScope` 观察中间状态

### 第 3 阶段：学习 RAG

Agent 如果只靠模型本身，容易出现知识过时、胡编、无法访问私有数据的问题。RAG 是业务 Agent 的核心能力之一。

先看简单版本：

- [`rag-examples/src/main/java/_1_easy/Easy_RAG_Example.java`](rag-examples/src/main/java/_1_easy/Easy_RAG_Example.java)

再看低层实现：

- [`rag-examples/src/main/java/_2_naive/Naive_RAG_Example.java`](rag-examples/src/main/java/_2_naive/Naive_RAG_Example.java)
- [`rag-examples/src/main/java/_4_low_level/_01_Low_Level_Naive_RAG_Example.java`](rag-examples/src/main/java/_4_low_level/_01_Low_Level_Naive_RAG_Example.java)

最后看高级能力：

- [`rag-examples/src/main/java/_3_advanced/_01_Advanced_RAG_with_Query_Compression_Example.java`](rag-examples/src/main/java/_3_advanced/_01_Advanced_RAG_with_Query_Compression_Example.java)
- [`rag-examples/src/main/java/_3_advanced/_02_Advanced_RAG_with_Query_Routing_Example.java`](rag-examples/src/main/java/_3_advanced/_02_Advanced_RAG_with_Query_Routing_Example.java)
- [`rag-examples/src/main/java/_3_advanced/_03_Advanced_RAG_with_ReRanking_Example.java`](rag-examples/src/main/java/_3_advanced/_03_Advanced_RAG_with_ReRanking_Example.java)
- [`rag-examples/src/main/java/_3_advanced/_09_Advanced_RAG_Return_Sources_Example.java`](rag-examples/src/main/java/_3_advanced/_09_Advanced_RAG_Return_Sources_Example.java)

这一阶段要理解完整链路：

```text
原始文档 -> Document -> TextSegment -> Embedding -> EmbeddingStore -> ContentRetriever -> Prompt 注入 -> 模型回答
```

重点概念：

- 文档如何加载
- 文档为什么要切分
- Embedding 模型和 Chat 模型不是一回事
- 向量检索为什么要设置 `maxResults` 和 `minScore`
- RAG 不是保证正确，只是给模型提供更可靠的上下文
- 真实项目要返回引用来源，方便用户验证答案

### 第 4 阶段：看完整业务 Agent

重点模块：

- [`customer-support-agent-example`](customer-support-agent-example)

先看入口和接口：

- [`CustomerSupportAgentApplication.java`](customer-support-agent-example/src/main/java/dev/langchain4j/example/CustomerSupportAgentApplication.java)
- [`CustomerSupportAgent.java`](customer-support-agent-example/src/main/java/dev/langchain4j/example/CustomerSupportAgent.java)
- [`CustomerSupportAgentController.java`](customer-support-agent-example/src/main/java/dev/langchain4j/example/CustomerSupportAgentController.java)

再看配置：

- [`CustomerSupportAgentConfiguration.java`](customer-support-agent-example/src/main/java/dev/langchain4j/example/CustomerSupportAgentConfiguration.java)
- [`BookingTools.java`](customer-support-agent-example/src/main/java/dev/langchain4j/example/BookingTools.java)

最后看测试：

- [`CustomerSupportAgentIT.java`](customer-support-agent-example/src/test/java/dev/langchain4j/example/CustomerSupportAgentIT.java)
- [`JudgeModelAssertions.java`](customer-support-agent-example/src/test/java/dev/langchain4j/example/utils/JudgeModelAssertions.java)

这个示例把几个核心能力组合起来了：

- `@AiService`：Spring Boot 里声明一个 AI 服务
- `@SystemMessage`：定义客服 Agent 的角色和业务规则
- `@MemoryId`：区分不同用户的对话记忆
- `TokenWindowChatMemory`：按 token 控制记忆窗口
- `BookingTools`：让模型能查订单、取消订单
- `ContentRetriever`：让模型能基于租车条款回答
- 测试：用集成测试和模型判断辅助验证行为

读这个模块时，不要只看“它能聊天”，要问自己：

- 用户说一句话后，请求从 Controller 到 Agent 的路径是什么？
- Agent 什么情况下会调用 `BookingTools`？
- Agent 什么情况下会走 RAG？
- Memory 是如何按用户隔离的？
- 业务规则写在 Prompt 里有什么风险？
- 哪些规则应该放进 Java 代码强约束，而不是只靠 Prompt？

## 5. 学习 Agent 时最容易卡住的点

### 5.1 把 Agent 想得太玄

在工程里，Agent 通常就是：

```text
LLM + Prompt + Tools + Memory + State + Workflow + Guardrails
```

不要一开始就追求“自主智能体”。先把每一块拆开学明白。

### 5.2 不知道工具调用到底是谁决定的

在 Tool Calling 中，模型根据用户问题和工具描述决定是否调用工具。Java 方法不会自动执行，必须由模型选择调用，再由框架执行。

所以 `@Tool` 的描述非常重要。描述不清楚，模型就可能不用、乱用或用错。

### 5.3 以为 Memory 等于长期记忆

示例里的 `MessageWindowChatMemory` 或 `TokenWindowChatMemory` 主要是会话上下文窗口，不是用户画像数据库。

真实项目一般会有：

- 短期对话记忆
- 长期用户资料
- 可检索知识库
- 操作日志
- 审计记录

### 5.4 以为 RAG 一定准确

RAG 只是把相关资料放进上下文。准确性还取决于：

- 文档质量
- 切分策略
- Embedding 模型
- 检索参数
- Prompt 是否要求基于资料回答
- 是否返回来源
- 是否有拒答策略

### 5.5 把所有逻辑都写进 Prompt

Prompt 适合描述语言风格、任务目标、业务规则，但关键业务约束最好用代码兜底。

例如“取消订单前必须确认”可以写在 System Message 里，但真正取消订单的 Java 方法也应该检查权限、状态和确认信号。

## 6. 建议的 10 天学习计划

### 第 1 天：跑通环境

- 配好 JDK 17 和 `OPENAI_API_KEY`
- 运行或在 IDE 中启动 `_00_HelloWorld.java`
- 看懂 `tutorials/pom.xml`
- 记下模型调用的最小代码

### 第 2 天：Prompt 与模型参数

- 阅读 `_01_ModelParameters.java`
- 阅读 `_03_PromptTemplate.java`
- 改几个 Prompt，观察输出差异
- 理解 System Message 和 User Message 的区别

### 第 3 天：AI Service

- 阅读 `_08_AIServiceExamples.java`
- 重点看接口、注解、结构化输出、枚举、POJO
- 自己新增一个小接口，例如“把用户反馈分类为 BUG、FEATURE、QUESTION”

### 第 4 天：Memory 与 Tools

- 阅读 `_05_Memory.java`
- 阅读 `_10_ServiceWithToolsExample.java`
- 自己新增一个 `@Tool` 方法，例如计算折扣、查询模拟库存
- 观察模型什么时候调用工具

### 第 5 天：RAG 入门

- 阅读 `Easy_RAG_Example.java`
- 阅读 `Naive_RAG_Example.java`
- 理解 Document、TextSegment、EmbeddingStore、ContentRetriever
- 尝试替换或新增一个本地 txt 文档

### 第 6 天：第一个 Agent

- 阅读 `_1a_Basic_Agent_Example.java`
- 阅读 `_1b_Basic_Agent_Example_Structured.java`
- 理解 Agent 和 AI Service 的关系
- 看懂 `AgenticServices.agentBuilder(...)`

### 第 7 天：顺序工作流

- 阅读 `_2a_Sequential_Agent_Example.java`
- 重点阅读 `_2b_Sequential_Agent_Example_Typed.java`
- 理解 `outputKey` 和 `AgenticScope`
- 打印并观察中间变量

### 第 8 天：循环、并行、条件

- 阅读 `_3a_Loop_Agent_Example.java`
- 阅读 `_4_Parallel_Workflow_Example.java`
- 阅读 `_5a_Conditional_Workflow_Example.java`
- 画出每个工作流的流程图

### 第 9 天：监督者与人参与

- 阅读 `_7a_Supervisor_Orchestration.java`
- 阅读 `_9a_HumanInTheLoop_Simple_Validator.java`
- 思考哪些场景必须让人确认
- 理解“自动化”和“可控性”的平衡

### 第 10 天：完整业务 Agent

- 阅读 `customer-support-agent-example`
- 从 Controller、Agent 接口、Tools、Configuration、测试五个角度拆解
- 总结它如何组合 Memory、Tools、RAG
- 设计一个你自己的业务 Agent，例如“图书馆助手”“课程顾问”“售后工单助手”

## 7. 读源码的方法

每看一个示例，按这个顺序拆：

1. 输入是什么？
2. 输出是什么？
3. 用了哪个模型？
4. Prompt 在哪里？
5. 是否用了 Memory？
6. 是否用了 Tools？
7. 是否用了 RAG？
8. 是否有结构化输出？
9. 中间状态存在哪里？
10. 如果模型答错，代码里有没有兜底？

这比从上到下硬读源码更有效。

## 8. 动手练习建议

### 练习 1：改一个分类器

基于 `_08_AIServiceExamples.java` 新增一个接口：

```java
enum TicketType {
    BUG, FEATURE_REQUEST, BILLING, OTHER
}
```

让模型把用户反馈分类成其中一种。

你要练到：

- 会写 `@UserMessage`
- 会返回枚举
- 会观察模型分类是否稳定

### 练习 2：加一个工具

基于 `_10_ServiceWithToolsExample.java` 新增一个工具类：

```java
@Tool("Calculates the final price after discount")
double finalPrice(double originalPrice, double discountRate) {
    return originalPrice * (1 - discountRate);
}
```

你要观察：

- 工具描述清楚时，模型是否会调用
- 参数名称和类型是否影响调用效果
- 工具返回值如何进入最终回答

### 练习 3：做一个小 RAG

在 `rag-examples/src/main/resources/documents` 放一个自己的 txt 文档，然后让示例基于文档回答问题。

你要练到：

- 会加载文档
- 会切分和向量化
- 会调 `maxResults` 和 `minScore`
- 会识别“检索不到时应该拒答”

### 练习 4：做一个两步 Agent 工作流

基于 `agentic-tutorial` 写一个简单流程：

```text
用户输入需求 -> Agent A 提取结构化任务 -> Agent B 生成执行计划
```

你要用到：

- `AgenticServices.agentBuilder(...)`
- `AgenticServices.sequenceBuilder(...)`
- `outputKey`
- `AgenticScope`

### 练习 5：改造客服 Agent

在 `customer-support-agent-example` 里加一个新工具，例如：

- 查询会员等级
- 计算退款金额
- 检查优惠券是否可用

同时加测试验证：

- 用户信息不完整时不能调用关键操作
- 需要确认时必须先问用户
- 和业务无关的问题要拒答

## 9. 你应该形成的工程判断

学完这条路线后，你应该能回答这些问题：

- 一个需求该用普通 ChatModel、AI Service，还是 Agentic Workflow？
- 哪些信息应该放 Prompt，哪些必须用代码硬约束？
- Tools 的粒度应该多大？
- Agent 的中间状态如何观察和测试？
- RAG 结果不可靠时怎么处理？
- 多 Agent 编排是顺序、并行、循环、条件，还是监督者模式？
- 如何给不同用户隔离 Memory？
- 如何设计可测试、可审计、可回滚的 Agent？

## 10. 推荐的最终小项目

做一个“课程顾问 Agent”，功能如下：

- 用户说出自己的背景和目标
- Agent 询问缺失信息
- 根据本地课程文档做 RAG
- 调用工具计算学习时间和预算
- 生成学习计划
- 高风险建议需要用户确认
- 保留每个用户的短期对话记忆

对应到本项目能力：

| 功能 | 对应示例 |
| --- | --- |
| 对话模型 | `_00_HelloWorld.java` |
| 接口封装 | `_08_AIServiceExamples.java` |
| 工具调用 | `_10_ServiceWithToolsExample.java` |
| 记忆 | `_05_Memory.java` |
| 文档问答 | `rag-examples` |
| 多步骤编排 | `agentic-tutorial` |
| Web 应用 | `customer-support-agent-example` |

如果你能独立做完这个小项目，就说明你已经不是“看过 Agent 示例”，而是开始具备用 Agent 做业务功能的能力了。

## 11. 最小学习闭环

每天学习时固定做四件事：

```text
读一个示例 -> 跑起来 -> 改一个小点 -> 写 5 行总结
```

5 行总结可以这样写：

```text
今天看的文件：
它解决的问题：
核心 API：
我改了什么：
我还没懂的问题：
```

Agent 学习最重要的不是背 API，而是逐渐建立“模型能力 + 工具能力 + 数据能力 + 流程控制 + 安全边界”的工程直觉。

