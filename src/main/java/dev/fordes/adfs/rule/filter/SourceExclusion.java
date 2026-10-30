package dev.fordes.adfs.rule.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import dev.fordes.adfs.rule.model.AdblockNetworkRule;
import dev.fordes.adfs.rule.model.AdblockPattern;
import dev.fordes.adfs.rule.model.AllOf;
import dev.fordes.adfs.rule.model.AnyOf;
import dev.fordes.adfs.rule.model.CosmeticRule;
import dev.fordes.adfs.rule.model.DnsAddressRule;
import dev.fordes.adfs.rule.model.DnsResponse;
import dev.fordes.adfs.rule.model.DomainConstraint;
import dev.fordes.adfs.rule.model.DomainMatch;
import dev.fordes.adfs.rule.model.DomainName;
import dev.fordes.adfs.rule.model.DomainPattern;
import dev.fordes.adfs.rule.model.DomainRule;
import dev.fordes.adfs.rule.model.ExactDomain;
import dev.fordes.adfs.rule.model.HostMappingRule;
import dev.fordes.adfs.rule.model.IpCidrMatch;
import dev.fordes.adfs.rule.model.IpCidrRule;
import dev.fordes.adfs.rule.model.KeywordDomain;
import dev.fordes.adfs.rule.model.MatchExpression;
import dev.fordes.adfs.rule.model.NetworkMatch;
import dev.fordes.adfs.rule.model.Not;
import dev.fordes.adfs.rule.model.OpaqueRule;
import dev.fordes.adfs.rule.model.PortMatch;
import dev.fordes.adfs.rule.model.ProcessMatch;
import dev.fordes.adfs.rule.model.RegexDomain;
import dev.fordes.adfs.rule.model.RouteRule;
import dev.fordes.adfs.rule.model.RuleAction;
import dev.fordes.adfs.rule.model.RuleEntry;
import dev.fordes.adfs.rule.model.SafariRule;
import dev.fordes.adfs.rule.model.Subdomain;
import dev.fordes.adfs.rule.model.SuffixDomain;
import dev.fordes.adfs.rule.model.WildcardDomain;

/**
 * 源级排除（{@code adfs.input[].exclude}）：把某个上游的误报挡在处理管线之外。
 *
 * <p>上游误报必须“丢弃”，不能交给 {@code rules.whitelist} 兜底：白名单只能事后追加
 * {@code @@} 放行，而 hosts / dnsmasq / mihomo / sing-box 根本不表达 allow，同一个白名单
 * 在不同产物里会得到不同结果。典型例子是 1Hosts Lite 把 GitHub 加速网关
 * （{@code gh-proxy.com} / {@code gh-proxy.org}）当作垃圾域名整域拦截。</p>
 *
 * <p>条目支持三种写法：</p>
 * <ul>
 *   <li>{@code example.com} —— 该域及其子域（含点且能解析为域名）；</li>
 *   <li>{@code *.example.com} —— 仅子域；</li>
 *   <li>其它字符串 —— 关键词，不区分大小写地匹配规则主体（单个词永远按关键词处理）。</li>
 * </ul>
 *
 * <p>只丢弃“阻断/改写”类规则：放行规则（{@code @@}）与 IP/CIDR 规则不受影响，避免把
 * 用户显式给出的放行也一起删掉。</p>
 */
public final class SourceExclusion {

    private static final SourceExclusion EMPTY = new SourceExclusion(List.of(), List.of(), List.of());

    private final List<DomainName> domains;
    private final List<DomainName> subdomains;
    private final List<String> keywords;

    private SourceExclusion(List<DomainName> domains, List<DomainName> subdomains, List<String> keywords) {
        this.domains = domains;
        this.subdomains = subdomains;
        this.keywords = keywords;
    }

    public static SourceExclusion of(List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return EMPTY;
        }
        List<DomainName> domains = new ArrayList<>();
        List<DomainName> subdomains = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        for (String raw : patterns) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String value = raw.strip();
            if (value.startsWith("*.")) {
                DomainName.tryParse(value.substring(2)).ifPresentOrElse(subdomains::add,
                        () -> keywords.add(value.toLowerCase(Locale.ROOT)));
                continue;
            }
            // 允许直接粘贴 adblock 规则（`||example.com^`、`@@||example.com^`）。
            String candidate = stripAdblockSyntax(value);
            if (candidate.isEmpty()) {
                continue;
            }
            // 只有“带点的合法域名”才按域名匹配; 单个词条（如 adserver）一律当关键词,
            // 否则任何单词都会被 DomainName 接受, 关键词写法就失效了。
            Optional<DomainName> domain = candidate.indexOf('.') >= 0
                    ? DomainName.tryParse(candidate) : Optional.empty();
            if (domain.isPresent()) {
                domains.add(domain.orElseThrow());
            } else {
                keywords.add(candidate.toLowerCase(Locale.ROOT));
            }
        }
        return new SourceExclusion(List.copyOf(domains), List.copyOf(subdomains), List.copyOf(keywords));
    }

    public boolean isEmpty() {
        return domains.isEmpty() && subdomains.isEmpty() && keywords.isEmpty();
    }

    public boolean excludes(RuleEntry entry) {
        if (isEmpty()) {
            return false;
        }
        return switch (entry) {
            case SafariRule safari -> excludes(safari.rule());
            case OpaqueRule opaque -> matchesOpaque(opaque.payload());
            case DomainRule(DomainPattern pattern, RuleAction action) ->
                    action == RuleAction.BLOCK && matchesPattern(pattern);
            case HostMappingRule(_, DomainName hostname) -> matchesDomain(hostname);
            case DnsAddressRule rule ->
                    rule.response() != DnsResponse.IGNORE && matchesPattern(rule.pattern());
            case AdblockNetworkRule rule -> rule.action() == RuleAction.BLOCK && matchesAdblock(rule);
            case RouteRule(MatchExpression expression) -> matchesExpression(expression);
            case CosmeticRule rule -> matchesCosmetic(rule);
            case IpCidrRule _ -> false;
        };
    }

    private static String stripAdblockSyntax(String value) {
        String candidate = value;
        if (candidate.startsWith("@@")) {
            candidate = candidate.substring(2);
        }
        if (candidate.startsWith("||")) {
            candidate = candidate.substring(2);
        }
        int end = candidate.length();
        for (int index = 0; index < candidate.length(); index++) {
            char character = candidate.charAt(index);
            if (character == '^' || character == '/' || character == '$' || character == '|') {
                end = index;
                break;
            }
        }
        return candidate.substring(0, end).strip();
    }

    private boolean matchesPattern(DomainPattern pattern) {
        if (matchesDomainValue(pattern.value())) {
            return true;
        }
        return switch (pattern) {
            case ExactDomain _, SuffixDomain _ -> false;
            case Subdomain _, WildcardDomain _, RegexDomain _, KeywordDomain _ ->
                    matchesKeyword(pattern.value());
        };
    }

    private boolean matchesAdblock(AdblockNetworkRule rule) {
        if (rule.pattern().kind() == AdblockPattern.Kind.DOMAIN_ANCHOR && matchesDomainValue(rule.pattern().value())) {
            return true;
        }
        return matchesKeyword(rule.pattern().value());
    }

    private boolean matchesOpaque(String payload) {
        if (matchesKeyword(payload)) {
            return true;
        }
        return matchesDomainValue(stripAdblockSyntax(payload));
    }

    private boolean matchesExpression(MatchExpression expression) {
        return switch (expression) {
            case DomainMatch(DomainPattern pattern) -> matchesPattern(pattern);
            case AllOf(List<MatchExpression> expressions) -> matchesAny(expressions);
            case AnyOf(List<MatchExpression> expressions) -> matchesAny(expressions);
            case Not(MatchExpression operand) -> matchesExpression(operand);
            case IpCidrMatch _, NetworkMatch _, PortMatch _, ProcessMatch _ -> false;
        };
    }

    private boolean matchesAny(List<MatchExpression> expressions) {
        for (MatchExpression expression : expressions) {
            if (matchesExpression(expression)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 元素规则只有在“全部生效域名都被排除”时才丢弃，避免把同时作用于其它站点的规则一起删掉。
     */
    private boolean matchesCosmetic(CosmeticRule rule) {
        List<DomainName> targets = rule.domains().stream()
                .filter(constraint -> !constraint.excluded())
                .map(DomainConstraint::domain)
                .filter(domain -> matchesDomain(domain))
                .toList();
        long included = rule.domains().stream().filter(constraint -> !constraint.excluded()).count();
        return included > 0 && targets.size() == included;
    }

    private boolean matchesDomainValue(String value) {
        return DomainName.tryParse(value).map(this::matchesDomain).orElse(false);
    }

    private boolean matchesDomain(DomainName candidate) {
        for (DomainName domain : domains) {
            if (covers(candidate, domain)) {
                return true;
            }
        }
        for (DomainName domain : subdomains) {
            if (candidate.value().endsWith("." + domain.value())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesKeyword(String value) {
        if (keywords.isEmpty() || value.isEmpty()) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static boolean covers(DomainName candidate, DomainName excluded) {
        return candidate.equals(excluded) || candidate.value().endsWith("." + excluded.value());
    }
}
