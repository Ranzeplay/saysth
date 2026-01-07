# Platform-specific config

The following is the configuration for different platforms supported by SaySomething.
You need to choose one of them to use.

The configuration file is `saysth/api-config.json` in the config directory.

## Cloudflare

```json
{
    "modelName": "@cf/qwen/qwen1.5-14b-chat-awq",
    "accountId": "<guid>",
    "apiKey": "xxx-xxx-xxx"
}
```

- `modelName`: The name of the AI model to use, must be exist on Cloudflare and you have access to it.

- `accountId`: The account ID of your Cloudflare account, it could be the tail route when you access https://dash.cloudflare.com/ .

- `apiKey`: An API key issued by your account, must be valid and have permission `Account.Workers AI`.

## OpenAI

```json
{
    "authCredentials": "xxx-xxx-xxx",
    "modelName": "gpt-4"
}
```

- `authCredentials`: Authorization credentials, will be added into `Autorizations` header.

- `modelName`: The name of the model you use.

## OpenAI-compatible

```json
{
    "authCredentials": "xxx-xxx-xxx",
    "modelName": "gpt-4",
    "chatCompletionEndpoint": "https://.../v1/chat/completion"
}
```

- `authCredentials`: Authorization credentials, will be added into `Autorizations` header.

- `modelName`: The name of the model you use.

- `chatCompletionEndpoint`: The URL to POST messages. If you use LM Studio, you may use `http://localhost:<port>/v1/chat/completion` where `port` is the port that LM Studio is listening on.

## LangChain4j with MCP

This integration uses LangChain4j's unified interface to communicate with language models and supports the Model Context Protocol (MCP) for enhanced tool integration.

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

- `apiKey`: Your API key for authentication with the LLM provider.

- `modelName`: The name of the model to use (e.g., "gpt-4", "gpt-3.5-turbo").

- `baseUrl`: (Optional) The base URL for the API endpoint. Defaults to OpenAI's endpoint if not specified. Use this to connect to OpenAI-compatible services.

- `temperature`: (Optional) Controls randomness in responses (0.0 to 2.0). Higher values make output more random. Defaults to model's default.

- `maxTokens`: (Optional) Maximum number of tokens to generate in the response.

- `enableMcpTools`: (Optional) Enable MCP plugin tools. Defaults to `true`. See MCP Plugin Development Guide for creating custom plugins.

**Note**: To use this platform, set `apiConfigPlatform` to `"langchain4j-mcp"` in your `config.json` file.

**MCP Plugin Development**: See `docs/MCP_PLUGIN_DEVELOPMENT.md` for a comprehensive guide on creating custom MCP server plugins.
