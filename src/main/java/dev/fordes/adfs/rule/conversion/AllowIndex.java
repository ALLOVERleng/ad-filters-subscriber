package dev.fordes.adfs.rule.conversion;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import dev.fordes.adfs.rule.model.AdblockNetworkRule;
import dev.fordes.adfs.rule.model.AdblockPattern;
import dev.fordes.adfs.rule.model.CosmeticOperator;
import dev.fordes.adfs.rule.model.CosmeticRule;
import dev.fordes.adfs.rule.model.DnsAddressRule;
import dev.fordes.adfs.rule.model.DnsResponse;
import dev.fordes.adfs.rule.model.DomainConstraint;
import dev.fordes.adfs.rule.model.DomainName;
import dev.fordes.adfs.rule.model.DomainPattern;
import dev.fordes.adfs.rule.model.DomainRule;
import dev.fordes.adfs.rule.model.HostMappingRule;
import dev.fordes.adfs.rule.model.RuleAction;
import dev.fordes.adfs.rule.model.RuleEntry;
import dev.fordes.adfs.rule.model.SafariRule;

/**
 * 收集放行（{@code @@}）规则的目标，用于剔除同一目标上的阻断规则。
 *
 * <p>同一目标既有放行又有阻断时，客户端里放行优先、阻断规则根本不会生效，属于“无效规则”；
 * 默认保留白名单（放行）规则、剔除阻断规则。</p>
 *
 * <p>目标按<b>域名归一</b>：adblock 的 {@code ||x^}、DNS 的 {@code |x|}、hosts 的
 * {@code 0.0.0.0 x}、dnsmasq/smartdns 的地址赋值都落到同一个 {@code domain:x} 键上，
 * 因此即使黑白名单来自不同格式的不同上游，也能互相抵消。父域级别的覆盖不在这里处理，
 * 由 {@code rules.whitelist} 与 {@link WhitelistMatcher} 负责。</p>
 */
public final class AllowIndex {

    private static final String DOMAIN = "domain:";

    private final Set<String> allowed = new HashSet<>();

    public void accept(RuleEntry entry) {
        if (isAllow(entry)) {
            String target = target(entry);
            if (target != null) {
                allowed.add(target);
            }
        }
    }

    /** 该阻断规则是否被同目标的放行规则覆盖。 */
    public boolean overrides(RuleEntry entry) {
        if (allowed.isEmpty() || !isBlock(entry)) {
            return false;
        }
        String target = target(entry);
        return target != null && allowed.contains(target);
    }

    public boolean isEmpty() {
        return allowed.isEmpty();
    }

    private static boolean isAllow(RuleEntry entry) {
        return switch (entry) {
            case SafariRule safari -> isAllow(safari.rule());
            case DomainRule(_, RuleAction action) -> action == RuleAction.ALLOW;
            case AdblockNetworkRule rule -> rule.action() == RuleAction.ALLOW;
            // smartdns 的 `address /x/-`（返回 IGNORE）等价于放行该域名。
            case DnsAddressRule rule -> rule.response() == DnsResponse.IGNORE;
            case CosmeticRule rule -> rule.exception();
            default -> false;
        };
    }

    private static boolean isBlock(RuleEntry entry) {
        return switch (entry) {
            case SafariRule safari -> isBlock(safari.rule());
            case DomainRule(_, RuleAction action) -> action == RuleAction.BLOCK;
            case AdblockNetworkRule rule -> rule.action() == RuleAction.BLOCK;
            case CosmeticRule rule -> !rule.exception();
            case HostMappingRule _ -> true;
            case DnsAddressRule rule -> rule.response() != DnsResponse.IGNORE;
            default -> false;
        };
    }

    private static String target(RuleEntry entry) {
        return switch (entry) {
            case SafariRule safari -> target(safari.rule());
            case DomainRule(DomainPattern pattern, _) -> domainTarget(pattern.value());
            case AdblockNetworkRule rule -> rule.pattern().kind() == AdblockPattern.Kind.DOMAIN_ANCHOR
                    ? domainTarget(rule.pattern().value())
                    : "network:" + rule.pattern().kind() + ':' + rule.pattern().value();
            case CosmeticRule rule -> "cosmetic:" + elementKind(rule.operator()) + ':'
                    + scope(rule) + ':' + rule.body();
            case HostMappingRule(_, DomainName hostname) -> DOMAIN + hostname.value();
            case DnsAddressRule rule -> domainTarget(rule.pattern().value());
            default -> null;
        };
    }

    /**
     * 把不同书写形态的域名归一成同一个键：{@code ||x^}、{@code |x|}、{@code 0.0.0.0 x}、
     * {@code -.x} 都视为对 {@code x} 的同一目标。无法解析为域名的模式（通配、关键词、正则）
     * 保留原始文本，只在完全相同才互相抵消。
     */
    private static String domainTarget(String value) {
        return DOMAIN + DomainName.tryParse(value)
                .map(DomainName::value)
                .orElseGet(() -> value.strip().toLowerCase(Locale.ROOT));
    }

    private static String elementKind(CosmeticOperator operator) {
        return operator.name().replace("_EXCEPTION", "");
    }

    private static String scope(CosmeticRule rule) {
        return rule.domains().stream()
                .map(AllowIndex::constraint)
                .sorted()
                .collect(Collectors.joining(","));
    }

    private static String constraint(DomainConstraint constraint) {
        return (constraint.excluded() ? "~" : "") + constraint.domain().value();
    }
}
