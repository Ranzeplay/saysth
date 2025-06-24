# 主配置



## 内容

在 `saysth/saysth-config.json` 中的配置，您将看到以下内容：

```json
{
  "personalities": [],
  "nameCandidates": [],
  "conclusionMessageLimit": 10,
  "villagerChatPrefix": "$",
  "useExistingVillagerName": true,
  "showTimeConsumption": false,
  "timeoutSeconds": 10,
  "apiConfigPlatform": "cloudflare"
}
```

- `personalities`：描述村民个性的字符串列表。
- `nameCandidates`：将分配给村民的名字字符串列表。
- `conclusionMessageLimit`：当消息达到此限制时，将被总结为一条简洁的消息。
- `villagerChatPrefix`：在聊天消息开头添加的字符串，表示您正在与村民对话。
- `useExistingVillagerName`：如果可能，根据 `CustomName` 使用村民的现有名称。
- `showTimeConsumption`：显示从发送到接收之间消耗的时间。
- `timeoutSeconds`：每个 LLM 模型请求的时间限制。
- `apiConfigPlatform`：LLM/AI 模型的平台。我们目前支持 3 种类型的平台。
  - `cloudflare`：Cloudflare AI worker。
  - `openai`：OpenAI。
  - `openai-compatible`：OpenAI 兼容接口。

:::tip

`apiConfigPlatform` 用于确定加载哪个 API 配置文件，该文件位于配置目录中的 `saysth/api-config.json` 文件中。

有关 API 配置的更多信息，请参阅 [平台特定配置](./platform.md)。

:::

## 备注

对于 NeoForge 和 Fabric，配置目录是相对于游戏 jar 文件的 `config/saysth`。

对于 Spigot，配置目录是相对于 `spigot.jar` 的 `plugins/saysth`。
