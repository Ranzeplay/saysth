# Configuration

## Main config

Configuration in the `config/saysth-config.json`, you will see the content below:  

```json
{  
  "cloudflareApiKey": "",  
  "cloudflareAccountId": "",  
  "personalities": [],  
  "nameCandidates": [],  
  "modelName": "@cf/qwen/qwen1.5-14b-chat-awq",  
  "conclusionMessageLimit": 10,  
  "villagerChatPrefix": "$",  
  "useExistingVillagerName": true,  
  "showTimeConsumption": false  
}  
```

- `cloudflareApiKey`: A Cloudflare API key generated that has access to AI features.  
- `cloudflareAccountId`: Your Cloudflare account ID.  
- `personalities`: A list of string that describes the personality of a villager.  
- `nameCandidates`: A list of string of names that will be put on villagers.  
- `modelName`: The LLM model to be used, should exist on Cloudflare  
- `conclusionMessageLimit`: When messages reach this limit, it will be concluded into one concise message.  
- `villagerChatPrefix`: Add the string at the beginning of chat message to show that you are talking to villagers.  
- `useExistingVillagerName`: Use the existing name of a villager according to `CustomName` if possible.  
- `showTimeConsumption`: Show how much time consumed between sending to receiving.  

## Villager prompt template

Configuration in the `config/saysth-sys-msg-template.txt`, you will see the content below:  

You can edit the default prompt applied to each new villager.  

### Variables

- `{name}`: Villager's name, initialized randomly referred to configuration.  
- `{personality}`: Villager's personality, initialized randomly referred to configuration.  
- `{livingIn}`: The place where the villager lives in, initialized according to game data.  
- `{profession}`: The profession of the villager, initialized according to game data.
