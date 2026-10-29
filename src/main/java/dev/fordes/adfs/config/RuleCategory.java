package dev.fordes.adfs.config;

import java.util.Locale;
import java.util.Optional;

/**
 * 输出侧可排除的规则类别。
 *
 * <p>用于把“只对 DNS 拦截有意义”的规则从浏览器列表中剔除：hosts / dns / mihomo / sing-box 等
 * DNS 类输入解析出的域名阻断规则，在浏览器里既无法覆盖全部子域，又会把产物撑到超出 CDN 的
 * 文件大小限制。</p>
 */
public enum RuleCategory {

    /** DNS 类输入解析出的域名阻断规则（{@code ||domain^}、{@code 0.0.0.0 domain} 等）。 */
    DNS_DOMAIN("dns-domain");

    private final String value;

    RuleCategory(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Optional<RuleCategory> find(String value) {
        String normalized = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        for (RuleCategory category : values()) {
            if (category.value.equals(normalized)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }
}
