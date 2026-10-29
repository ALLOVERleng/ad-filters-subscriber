package dev.fordes.adfs.rule.model;

import java.util.Locale;
import java.util.Set;

/**
 * 过滤保留主机名与本机专用域名。
 *
 * <p>上游规则集常混入 /etc/hosts 模板行(如 {@code 127.0.0.1 localhost}), 若不加区分地当作广告域名
 * 发布, 订阅者会拦掉本机回环名, 破坏本机开发与部分应用的域名解析。此类条目对广告拦截没有意义,
 * 因此在写出前统一丢弃。</p>
 */
public final class ReservedHosts {

    /** 精确匹配的保留名(已统一小写)。 */
    private static final Set<String> EXACT = Set.of(
            "localhost", "localhost6", "localhost.localdomain", "localhost6.localdomain6",
            "local", "localdomain",
            "ip6-localhost", "ip6-loopback", "ip6-allnodes", "ip6-allrouters", "ip6-allhosts",
            "broadcasthost");

    /** 后缀匹配的保留域(以点开头, 避免命中 ordinary.local 之外的同名前缀)。 */
    private static final Set<String> SUFFIXES = Set.of(".local", ".localdomain", ".localhost");

    private ReservedHosts() {
    }

    /** 规则是否作用于保留主机名。 */
    public static boolean isReserved(RuleEntry entry) {
        String host = host(entry);
        return host != null && isReservedName(host);
    }

    /** 归一后的域名是否为保留名。 */
    public static boolean isReservedName(String value) {
        String host = normalize(value);
        if (host.isEmpty()) {
            return false;
        }
        if (EXACT.contains(host)) {
            return true;
        }
        for (String suffix : SUFFIXES) {
            if (host.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static String host(RuleEntry entry) {
        return switch (entry) {
            case SafariRule safari -> host(safari.rule());
            case DomainRule(DomainPattern pattern, RuleAction _) -> pattern.value();
            case HostMappingRule(IpAddress _, DomainName hostname) -> hostname.value();
            case DnsAddressRule rule -> rule.pattern().value();
            case AdblockNetworkRule rule -> rule.pattern().value();
            default -> null;
        };
    }

    private static String normalize(String value) {
        String host = value.strip().toLowerCase(Locale.ROOT);
        int start = 0;
        while (start < host.length() && (host.charAt(start) == '|' || host.charAt(start) == '.'
                || host.charAt(start) == '*')) {
            start++;
        }
        int end = host.length();
        while (end > start && (host.charAt(end - 1) == '^' || host.charAt(end - 1) == '|'
                || host.charAt(end - 1) == '.')) {
            end--;
        }
        return host.substring(start, end);
    }
}
