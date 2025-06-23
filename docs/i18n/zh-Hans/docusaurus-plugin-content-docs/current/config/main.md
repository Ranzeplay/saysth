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
- `timeoutSeconds`：每个LLM模型请求的时间限制。
- `apiConfigPlatform`：LLM/AI模型的平台。我们目前支持3种类型的平台。
  - `cloudflare`：Cloudflare AI worker。
  - `openai`：OpenAI。
  - `openai-compatible`：OpenAI兼容接口。

## 备注

对于NeoForge和Fabric，配置目录是相对于游戏jar文件的 `config/saysth`。

对于Spigot，配置目录是相对于 `spigot.jar` 的 `plugins/saysth`。
