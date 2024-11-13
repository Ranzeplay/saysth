# Main config

## Content

Configuration in the `saysth/saysth-config.json`, you will see the content below:

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

- `personalities`: A list of string that describes the personality of a villager.
- `nameCandidates`: A list of string of names that will be put on villagers.
- `conclusionMessageLimit`: When messages reach this limit, it will be concluded into one concise message.
- `villagerChatPrefix`: Add the string at the beginning of chat message to show that you are talking to villagers.
- `useExistingVillagerName`: Use the existing name of a villager according to `CustomName` if possible.
- `showTimeConsumption`: Show how much time consumed between sending to receiving.
- `timeoutSconds`: Time time limit of each request to the LLM model.
- `apiConfigPlatform`: The platform of the LLM/AI model. We currently support 3 types of platforms. 
  - `cloudflare`: Cloudflare AI worker.
  - `openai`: OpenAI.
  - `openai-compatible`: OpenAI-compatible interfaces.

## Remark

For NeoForge and Fabric, the config directory is `config/saysth` relative to the game jar file.

For Spigot, the config directory is `plugins/saysth` relative to the `spigot.jar`.
