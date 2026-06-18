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
