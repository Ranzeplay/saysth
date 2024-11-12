package space.ranzeplay.saysth.config;

import lombok.Getter;

@Getter
public class SaySthConfig {
    public SaySthConfig() {
        personalities = new String[]{};
        nameCandidates = new String[]{};
        conclusionMessageLimit = 10;
        villagerChatPrefix = "$";
        useExistingVillagerName = true;
        showTimeConsumption = false;
        apiConfigPlatform = "cloudflare";
        timeoutSeconds = 10;
    }

    String[] personalities;
    String[] nameCandidates;
    int conclusionMessageLimit;
    String villagerChatPrefix;
    boolean useExistingVillagerName;
    boolean showTimeConsumption;
    String apiConfigPlatform;
    int timeoutSeconds;
}
