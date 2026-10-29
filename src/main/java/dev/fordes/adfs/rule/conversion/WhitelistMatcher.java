package dev.fordes.adfs.rule.conversion;

import java.util.List;

import dev.fordes.adfs.rule.model.AdblockNetworkRule;
import dev.fordes.adfs.rule.model.AdblockPattern;
import dev.fordes.adfs.rule.model.DnsAddressRule;
import dev.fordes.adfs.rule.model.DnsResponse;
import dev.fordes.adfs.rule.model.DomainMatch;
import dev.fordes.adfs.rule.model.DomainName;
import dev.fordes.adfs.rule.model.DomainPattern;
import dev.fordes.adfs.rule.model.DomainRule;
import dev.fordes.adfs.rule.model.ExactDomain;
import dev.fordes.adfs.rule.model.HostMappingRule;
import dev.fordes.adfs.rule.model.MatchExpression;
import dev.fordes.adfs.rule.model.RouteRule;
import dev.fordes.adfs.rule.model.RuleAction;
import dev.fordes.adfs.rule.model.RuleEntry;
import dev.fordes.adfs.rule.model.Subdomain;
import dev.fordes.adfs.rule.model.SuffixDomain;

/** 判断简单规则是否作用于白名单域名及其子域。 */
public final class WhitelistMatcher {

    private WhitelistMatcher() {
    }

    private static boolean matches(MatchExpression expression, DomainName allowed) {
        return switch (expression) {
            case DomainMatch(var pattern) -> matches(pattern, allowed);
            default -> false;
        };
    }

    private static boolean matches(DomainPattern pattern, DomainName allowed) {
        return switch (pattern) {
            case ExactDomain(var domain), Subdomain(var domain), SuffixDomain(var domain) ->
                    covers(domain, allowed);
            default -> false;
        };
    }

    /**
     * 被阻断的域名是否属于被放行的域名。
     *
     * <p>白名单语义是“放行该域名及其子域”（adblock 的 {@code @@||a.example^} 与 DNS 的
     * allow 记录都是这个覆盖面）。hosts/dnsmasq/mihomo/sing-box 等只能删除、不能表达放行的格式
     * 必须用同一套判定，否则同一个白名单在不同产物里会得到不同结果。</p>
     */
    private static boolean covers(DomainName blocked, DomainName allowed) {
        return blocked.equals(allowed) || blocked.value().endsWith("." + allowed.value());
    }

    private static boolean matches(RuleEntry entry, DomainName allowed) {
        return switch (entry) {
            case DomainRule(var pattern, RuleAction action) -> action == RuleAction.BLOCK
                    && matches(pattern, allowed);
            case HostMappingRule(_, DomainName hostname) -> covers(hostname, allowed);
            case RouteRule(var expression) -> matches(expression, allowed);
            case AdblockNetworkRule rule -> matchesAdblock(rule, allowed);
            case DnsAddressRule rule -> rule.response() != DnsResponse.IGNORE
                    && matches(rule.pattern(), allowed);
            default -> false;
        };
    }

    public static boolean matches(RuleEntry entry, List<DomainName> allowedDomains) {
        return allowedDomains.stream().anyMatch(allowed -> matches(entry, allowed));
    }

    private static boolean matchesAdblock(AdblockNetworkRule rule, DomainName allowed) {
        if (rule.action() != RuleAction.BLOCK
                || rule.pattern().kind() != AdblockPattern.Kind.DOMAIN_ANCHOR) {
            return false;
        }
        // 规则主体不一定能解析为域名(通配、含非法标签等), 解析失败即视为不命中, 不能让异常中断写出。
        return DomainName.tryParse(rule.pattern().value())
                .map(domain -> covers(domain, allowed))
                .orElse(false);
    }
}
