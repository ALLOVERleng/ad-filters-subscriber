package dev.fordes.adfs.rule.conversion;

import java.util.Optional;

import dev.fordes.adfs.rule.model.AdblockNetworkRule;
import dev.fordes.adfs.rule.model.AdblockPattern;
import dev.fordes.adfs.rule.model.DomainName;

/**
 * 裸域名的"可收紧"判定。
 *
 * <p>AdGuard / uBO 里裸域名（如 {@code ad.qq.com}）是 URL 子串匹配，会连 {@code weread.qq.com}
 * 一起命中；只有能安全收紧成 {@code ||domain^} 时才视为域名规则。这条判定原先只存在于写出阶段
 * （{@code AdblockDomainConversion#bareDomain}），导致黑白名单消解看到的目标键仍是
 * {@code network:URL:...}，与放行规则的 {@code domain:...} 永远不相等，同一域名的阻断与放行会
 * 同时进入产物。</p>
 *
 * <p>本类把该判定下沉到 {@code rule} 层，供写出阶段与冲突消解共用同一份实现，避免两处判定漂移。</p>
 */
public final class BareDomain {

    private BareDomain() {
    }

    /**
     * 规则主体整体就是一个域名时返回该域名。
     *
     * <p>单标签（{@code ads}）、通配符、路径、锚点、{@code match-case} 以及会被改写成 punycode 的
     * IDN 一律返回空：这些规则锚定后要么失效，要么改变匹配对象。</p>
     *
     * <p>本方法只回答"能否收紧为域锚"这一格式问题，不掺杂策略判断；需要额外约束的调用方
     * （例如冲突消解要求规则不带请求级修饰符）应自行叠加条件。</p>
     */
    public static Optional<DomainName> of(AdblockNetworkRule rule) {
        if (rule.pattern().kind() != AdblockPattern.Kind.URL || rule.matchCase()) {
            return Optional.empty();
        }
        String value = rule.pattern().value().strip();
        if (value.indexOf('.') < 0) {
            return Optional.empty();
        }
        if (value.chars().anyMatch(character -> character == '*' || character == '/' || character == '^'
                || character == '|' || character == '$')) {
            return Optional.empty();
        }
        // 根标签尾点在 DNS 里合法, 在规则里只会让子串匹配更容易命中, 统一去掉再比较。
        String candidate = value.endsWith(".") ? value.substring(0, value.length() - 1) : value;
        if (candidate.isEmpty()) {
            return Optional.empty();
        }
        return DomainName.tryParse(candidate).filter(domain -> domain.value().equals(candidate));
    }
}
