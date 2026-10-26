package space.ranzeplay.saysth.villager;

import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;

public final class VillagerTradeFormatter {
    private VillagerTradeFormatter() {
    }

    public static String format(MerchantOffers trades) {
        if (trades == null || trades.isEmpty()) {
            return "You don't sell anything for now.";
        }

        var formattedTrades = new ArrayList<String>();
        formattedTrades.add("You ONLY have the following trades:");
        for (var trade : trades) {
            if (trade == null || trade.getCostA() == null || trade.getResult() == null) {
                continue;
            }

            var formattedTrade = new StringBuilder("- ")
                    .append(trade.getCostA().getCount())
                    .append("x ")
                    .append(trade.getCostA().getHoverName().getString().toLowerCase());
            if (trade.getCostB() != null && !trade.getCostB().isEmpty()) {
                formattedTrade.append(" and ")
                        .append(trade.getCostB().getCount())
                        .append("x ")
                        .append(trade.getCostB().getHoverName().getString().toLowerCase());
            }
            formattedTrade.append(" for ")
                    .append(trade.getResult().getCount())
                    .append("x ")
                    .append(trade.getResult().getHoverName().getString().toLowerCase());
            formattedTrades.add(formattedTrade.toString());
        }
        return String.join("\n", formattedTrades);
    }
}
