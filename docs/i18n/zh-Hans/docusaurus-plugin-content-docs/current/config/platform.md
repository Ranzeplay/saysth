# 平台特定配置

以下是SaySomething支持的不同平台的配置。
您需要选择其中一个来使用。

配置文件是配置目录中的 `saysth/api-config.json`。

## Cloudflare

```json
{
    "modelName": "@cf/qwen/qwen1.5-14b-chat-awq",
    "accountId": "<guid>",
    "apiKey": "xxx-xxx-xxx"
}
```

- `modelName`：要使用的AI模型名称，必须存在于Cloudflare上且您有权访问。

- `accountId`：您的Cloudflare账户的账户ID，可以在您访问 https://dash.cloudflare.com/ 时的末尾路由。

- `apiKey`：由您的账户签发的API密钥，必须有效且具有 `Account.Workers AI` 权限。

## OpenAI

```json
{
    "authCredentials": "xxx-xxx-xxx",
    "modelName": "gpt-4"
}
```

- `authCredentials`：授权凭据，将添加到 `Authorization` 头中。

- `modelName`：您使用的模型名称。

## OpenAI兼容

```json
{
    "authCredentials": "xxx-xxx-xxx",
    "modelName": "gpt-4",
    "chatCompletionEndpoint": "https://.../v1/chat/completion"
}
```

- `authCredentials`：授权凭据，将添加到 `Authorization` 头中。

- `modelName`：您使用的模型名称。

- `chatCompletionEndpoint`：用于POST消息的URL。如果您使用LM Studio，可以使用 `http://localhost:<port>/v1/chat/completion`，其中 `port` 是LM Studio监听的端口。

## LangChain4j with MCP

此集成使用LangChain4j的统一接口与语言模型通信，并支持模型上下文协议（MCP）以增强工具集成。

```json
{
    "apiKey": "xxx-xxx-xxx",
    "modelName": "gpt-3.5-turbo",
    "baseUrl": "https://api.openai.com/v1",
    "temperature": 0.7,
    "maxTokens": 1000,
    "enableMcpTools": true
}
```

- `apiKey`：用于与LLM提供商进行身份验证的API密钥。

- `modelName`：要使用的模型名称（例如，"gpt-4"、"gpt-3.5-turbo"）。

- `baseUrl`：（可选）API端点的基础URL。如果未指定，则默认为OpenAI的端点。使用此选项连接到与OpenAI兼容的服务。

- `temperature`：（可选）控制响应的随机性（0.0至2.0）。值越高，输出越随机。默认为模型的默认值。

- `maxTokens`：（可选）响应中生成的最大令牌数。

- `enableMcpTools`：（可选）启用MCP插件工具。默认为 `true`。有关创建自定义插件的信息，请参阅MCP插件开发指南。

**注意**：要使用此平台，请在 `config.json` 文件中将 `apiConfigPlatform` 设置为 `"langchain4j-mcp"`。

**MCP插件开发**：有关创建自定义MCP服务器插件的综合指南，请参阅 `docs/MCP_PLUGIN_DEVELOPMENT.md`。
