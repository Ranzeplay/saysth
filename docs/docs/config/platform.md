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
