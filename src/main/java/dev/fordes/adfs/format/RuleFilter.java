package dev.fordes.adfs.format;

import dev.fordes.adfs.config.OutputSpec;
import dev.fordes.adfs.config.RuleCategory;
import dev.fordes.adfs.rule.model.DnsAddressRule;
import dev.fordes.adfs.rule.model.DnsResponse;
import dev.fordes.adfs.rule.model.DomainRule;
import dev.fordes.adfs.rule.model.HostMappingRule;
import dev.fordes.adfs.rule.model.RuleAction;
import dev.fordes.adfs.rule.model.RuleEntry;
import dev.fordes.adfs.rule.model.SafariRule;

/**
 * 按输出声明的类别排除规则。
 *
 * <p>{@link RuleCategory#DNS_DOMAIN} 的判定刻意基于“规则类型”而不是“渲染后的文本形态”：
 * hosts / dns / mihomo / sing-box 类输入会解析成 {@link DomainRule}、{@link HostMappingRule}、
 * {@link DnsAddressRule}，而 Adblock 上游的 {@code ||domain^} 是网络规则。二者渲染结果相同，
 * 但前者只对 DNS 级拦截有意义，后者（如 {@code ||doubleclick.net^}）在浏览器里仍然有效，
 * 不能被一并剔除。</p>
 */
public final class RuleFilter {

    private RuleFilter() {
    }

    public static boolean isExcluded(OutputSpec spec, RuleEntry entry) {
        if (spec.exclude().isEmpty()) {
            return false;
        }
        for (RuleCategory category : spec.exclude()) {
            if (matches(category, entry)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(RuleCategory category, RuleEntry entry) {
        return switch (category) {
            case DNS_DOMAIN -> isDnsDomain(entry);
        };
    }

    /** 是否来自 DNS 类输入的域名阻断规则。 */
    static boolean isDnsDomain(RuleEntry entry) {
        return switch (entry) {
            case SafariRule safari -> isDnsDomain(safari.rule());
            case DomainRule(_, RuleAction action) -> action == RuleAction.BLOCK;
            case HostMappingRule _ -> true;
            case DnsAddressRule rule -> rule.response() != DnsResponse.IGNORE;
            default -> false;
        };
    }
}
