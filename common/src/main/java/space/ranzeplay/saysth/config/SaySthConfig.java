package space.ranzeplay.saysth.config;

import lombok.Getter;

@Getter
public class SaySthConfig {
    public SaySthConfig() {
        cloudflareApiKey = "";
        cloudflareAccountId = "";
        personalities = new String[]{};
        nameCandidates = new String[]{};
    }

    String cloudflareApiKey;
    String cloudflareAccountId;
    String[] personalities;
    String[] nameCandidates;
}
