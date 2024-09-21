# SaySomething

![Modrinth Downloads](https://img.shields.io/modrinth/dt/say-something)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/ranzeplay/saysth)
![GitHub contributors](https://img.shields.io/github/contributors/ranzeplay/saysth)

A Minecraft Mod that connects villagers to AI models.

> We are preparing for v1.0, please wait patiently :)
> 
> Any issues are welcome.

## Description

The mod uses `@cf/qwen/qwen1.5-14b-chat-awq` model by default on Cloudflare AI
to enable players to interact with villagers via text.
You can customize many things.

## Usage

Put the string mentioned in config at the beginning of your chat message
to talk to villagers in front of you.

Villagers may not respond if your message isn't related to them.

## Configuration

### Main config

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

### Villager prompt template

Configuration in the `config/saysth-sys-msg-template.txt`, you will see the content below:

You can edit the default prompt applied to each new villager.

#### Variables

- `{name}`: Villager's name, initialized randomly referred to configuration.
- `{personality}`: Villager's personality, initialized randomly referred to configuration.
- `{livingIn}`: The place where the villager lives in, initialized according to game data.
- `{profession}`: The profession of the villager, initialized according to game data.

## Upgrading

When upgrading, for now, please manually add the newly-added config items into `saysth-config.json` file.
