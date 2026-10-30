package dev.fordes.adfs.format;

import dev.fordes.adfs.config.OutputSpec;
import dev.fordes.adfs.config.RuleCategory;
import dev.fordes.adfs.config.RuleShape;
import dev.fordes.adfs.config.RuleType;
import dev.fordes.adfs.format.adblock.AdblockSyntax;
import dev.fordes.adfs.rule.model.CosmeticRule;
import dev.fordes.adfs.rule.model.DnsAddressRule;
import dev.fordes.adfs.rule.model.DnsResponse;
import dev.fordes.adfs.rule.model.DomainRule;
import dev.fordes.adfs.rule.model.HostMappingRule;
import dev.fordes.adfs.rule.model.OpaqueRule;
import dev.fordes.adfs.rule.model.RuleAction;
import dev.fordes.adfs.rule.model.RuleEntry;
import dev.fordes.adfs.rule.model.SafariRule;

/**
 * 按输出声明的类别与形状排除规则。
 *
 * <p>{@link RuleCategory#DNS_DOMAIN} 的判定刻意基于“规则类型”而不是“渲染后的文本形态”：
 * hosts / dns / mihomo / sing-box 类输入会解析成 {@link DomainRule}、{@link HostMappingRule}、
 * {@link DnsAddressRule}，而 Adblock 上游的 {@code ||domain^} 是网络规则。二者渲染结果相同，
 * 但前者只对 DNS 级拦截有意义，后者（如 {@code ||doubleclick.net^}）在浏览器里仍然有效，
 * 不能被一并剔除。</p>
 *
 * <p>{@link RuleShape#ELEMENT} / {@link RuleShape#URL} 用于“元素规则与 URL 规则分开成文件”：
 * 元素隐藏、样式注入一类外观规则属于前者，域名/网络/路由规则属于后者。</p>
 */
public final class RuleFilter {

    private RuleFilter() {
    }

    public static boolean isExcluded(OutputSpec spec, RuleEntry entry) {
        if (!acceptsShape(spec.shape(), entry)) {
            return true;
        }
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

    private static boolean acceptsShape(RuleShape shape, RuleEntry entry) {
        return switch (shape) {
            case ALL -> true;
            case ELEMENT -> isElement(entry);
            case URL -> !isElement(entry);
        };
    }

    /** 是否元素隐藏 / 样式注入一类的外观规则。 */
    static boolean isElement(RuleEntry entry) {
        return switch (entry) {
            case SafariRule safari -> isElement(safari.rule());
            case CosmeticRule _ -> true;
            case OpaqueRule opaque -> opaque.type() == RuleType.ADBLOCK
                    && AdblockSyntax.cosmeticOperator(opaque.payload()) != null;
            default -> false;
        };
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
