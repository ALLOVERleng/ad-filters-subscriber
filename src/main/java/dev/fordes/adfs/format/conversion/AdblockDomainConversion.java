package dev.fordes.adfs.format.conversion;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import dev.fordes.adfs.rule.conversion.ConversionDecision;
import dev.fordes.adfs.rule.conversion.ConversionLoss;
import dev.fordes.adfs.rule.conversion.ConversionScope;
import dev.fordes.adfs.rule.model.AdblockNetworkRule;
import dev.fordes.adfs.rule.model.AdblockPattern;
import dev.fordes.adfs.rule.model.PartyConstraint;

/** 计算 Adblock 域名规则转换为非浏览器过滤格式时的语义损失。 */
public final class AdblockDomainConversion {

    private AdblockDomainConversion() {
    }

    /**
     * 目标是否能把规则限制在特定站点内。
     *
     * <p>{@code $domain=} 表示“只在这些站点上生效”，hosts / DNS / dnsmasq / smartdns / mihomo /
     * sing-box 都只能表达域名本身，没有站点作用域：放大成整域规则时，阻断会误伤所有站点，
     * 放行则会变成整域放行、抵消其它列表的拦截（例如只对某个站点的 {@code @@||x^$domain=y}
     * 被写成 {@code @@||x^} 后，会把其它来源对 {@code x} 的恶意 URL 拦截整体作废）。
     * 两个方向都不安全，因此这类规则在无法保真时直接丢弃，由报告计入“无法转换”。</p>
     */
    public static boolean supportsRequestScope(AdblockNetworkRule rule) {
        return rule.domainConstraints().isEmpty();
    }

    public static boolean supportsSubject(AdblockNetworkRule rule) {
        return supportsRequestScope(rule)
                && rule.pattern().kind() == AdblockPattern.Kind.DOMAIN_ANCHOR
                && rule.modifiers().isEmpty();
    }

    public static ConversionDecision decide(
            AdblockNetworkRule rule,
            boolean preserveImportant,
            ConversionDecision base) {
        boolean expanded = !rule.includedResourceTypes().isEmpty()
                || !rule.excludedResourceTypes().isEmpty()
                || !rule.domainConstraints().isEmpty()
                || rule.partyConstraint() != PartyConstraint.ANY
                || rule.matchCase();
        boolean reduced = rule.important() && !preserveImportant;
        ConversionScope scope = merge(base.scope(), expanded, reduced);
        if (!expanded && !reduced) {
            return base;
        }
        List<String> reasons = new ArrayList<>();
        EnumSet<ConversionLoss> losses = EnumSet.noneOf(ConversionLoss.class);
        losses.addAll(base.losses());
        reasons.add(base.reason());
        if (expanded) {
            losses.add(ConversionLoss.DROPPED_REQUEST_CONSTRAINT);
            reasons.add("目标丢弃 Adblock 请求级约束");
        }
        if (reduced) {
            losses.add(ConversionLoss.DROPPED_PRIORITY);
            reasons.add("目标丢弃 Adblock important 优先级");
        }
        return new ConversionDecision(scope, losses, String.join("; ", reasons));
    }

    private static ConversionScope merge(ConversionScope base, boolean expanded, boolean reduced) {
        if (base == ConversionScope.UNSUPPORTED) {
            return ConversionScope.UNSUPPORTED;
        }
        ConversionScope additional = expanded && reduced ? ConversionScope.MIXED
                : expanded ? ConversionScope.EXPANDED : reduced ? ConversionScope.REDUCED : ConversionScope.EXACT;
        return base.combine(additional);
    }
}
