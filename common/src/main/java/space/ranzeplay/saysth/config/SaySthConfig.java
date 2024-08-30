package space.ranzeplay.saysth.config;

import lombok.Getter;

@Getter
public class SaySthConfig {
    public SaySthConfig() {
        cloudflareApiKey = "";
        cloudflareUserId = "";
        personalities = new String[]{};
        nameCandidates = new String[]{};
    }

    String cloudflareApiKey;
    String cloudflareUserId;
    String[] personalities;
    String[] nameCandidates;
}
