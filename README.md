# SaySomething

A Minecraft Mod that connects villagers to AI models.

## Description

The mod uses `@cf/qwen/qwen1.5-14b-chat-awq` model on Cloudflare AI
to enable players to interact with villagers via text.

## Usage

Put `$` sign at the beginning of your chat message
to talk to villagers in front of you.

Villagers may not respond if your message isn't related to them.

## Configuration

Configurations are in the `config/saysth-config.json`, you will see the content below:

```json
{
  "cloudflareApiKey": "",
  "cloudflareAccountId": "",
  "personalities": [],
  "nameCandidates": []
}
```

- `cloudflareApiKey`: A Cloudflare API key generated that has access to AI features.
- `cloudflareAccountId`: Your Cloudflare account ID.
- `personalities`: A list of string that describes the personality of a villager.
- `nameCandidates`: A list of string of names that will be put on villagers.
