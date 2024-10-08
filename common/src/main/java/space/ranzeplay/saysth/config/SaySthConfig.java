package space.ranzeplay.saysth.config;

import lombok.Getter;

@Getter
public class SaySthConfig {
    public SaySthConfig() {
        cloudflareApiKey = "";
        cloudflareAccountId = "";
        personalities = new String[]{};
        nameCandidates = new String[]{};
        modelName = "@cf/qwen/qwen1.5-14b-chat-awq";
        conclusionMessageLimit = 10;
        villagerChatPrefix = "$";
        useExistingVillagerName = true;
        showTimeConsumption = false;
    }

    String cloudflareApiKey;
    String cloudflareAccountId;
    String[] personalities;
    String[] nameCandidates;
    String modelName;
    int conclusionMessageLimit;
    String villagerChatPrefix;
    boolean useExistingVillagerName;
    boolean showTimeConsumption;
}
