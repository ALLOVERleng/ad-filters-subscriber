package dev.fordes.adfs.rule.conversion;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.fordes.adfs.config.RuleDialect;
import dev.fordes.adfs.config.RuleType;
import dev.fordes.adfs.rule.model.AdblockModifier;
import dev.fordes.adfs.rule.model.AdblockNetworkRule;
import dev.fordes.adfs.rule.model.AdblockPattern;
import dev.fordes.adfs.rule.model.DnsAddressRule;
import dev.fordes.adfs.rule.model.DnsResponse;
import dev.fordes.adfs.rule.model.DomainName;
import dev.fordes.adfs.rule.model.DomainRule;
import dev.fordes.adfs.rule.model.ExactDomain;
import dev.fordes.adfs.rule.model.HostMappingRule;
import dev.fordes.adfs.rule.model.IpAddress;
import dev.fordes.adfs.rule.model.IpFamily;
import dev.fordes.adfs.rule.model.PartyConstraint;
import dev.fordes.adfs.rule.model.RuleAction;
import dev.fordes.adfs.rule.model.SuffixDomain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AllowIndexTest {

    @Test
    void allowFromAnotherFormatCancelsDomainBlock() {
        AllowIndex index = new AllowIndex();
        index.accept(adblock("ads.example", RuleAction.ALLOW));

        // hosts 的 0.0.0.0、DNS 的 |x|、adblock 的 ||x^ 归一成同一个目标。
        assertTrue(index.overrides(new HostMappingRule(IpAddress.parse("0.0.0.0"),
                new DomainName("ads.example"))));
        assertTrue(index.overrides(new DomainRule(new ExactDomain(new DomainName("ads.example")),
                RuleAction.BLOCK)));
        assertTrue(index.overrides(adblock("ads.example", RuleAction.BLOCK)));
        assertFalse(index.overrides(adblock("other.example", RuleAction.BLOCK)));
    }

    @Test
    void smartDnsIgnoreAllowCancelsAddressBlock() {
        AllowIndex index = new AllowIndex();
        index.accept(new DnsAddressRule(RuleType.SMARTDNS, new SuffixDomain(new DomainName("ads.example")),
                DnsResponse.IGNORE, Set.of(IpFamily.IPV4, IpFamily.IPV6), List.of()));

        assertTrue(index.overrides(new DnsAddressRule(RuleType.DNSMASQ,
                new SuffixDomain(new DomainName("ads.example")), DnsResponse.NULL_ADDRESS,
                Set.of(IpFamily.IPV4), List.of())));
    }

    @Test
    void doesNotCancelLookalikeDomain() {
        AllowIndex index = new AllowIndex();
        index.accept(adblock("ads.example", RuleAction.ALLOW));

        assertFalse(index.overrides(adblock("sub.ads.example", RuleAction.BLOCK)));
        assertFalse(index.overrides(adblock("notads.example", RuleAction.BLOCK)));
    }

    /**
     * 上游会把域名列表直接混进 adblock 文件（HyperADRules ads 实测 17132 条裸行）。
     * 这类裸行解析为 {@link AdblockPattern.Kind#URL}，写出阶段才收紧成 {@code ||domain^}；
     * 冲突消解必须用同一个目标键，否则 {@code ||ads.example^} 与 {@code @@||ads.example^}
     * 会同时进入产物。
     */
    @Test
    void bareDomainUrlBlockCancelsAgainstDomainAllow() {
        AllowIndex index = new AllowIndex();
        index.accept(adblock("ads.example", RuleAction.ALLOW));

        assertTrue(index.overrides(bare("ads.example", RuleAction.BLOCK)),
                "裸域名阻断应被同域名放行规则抵消");
        assertFalse(index.overrides(bare("sub.ads.example", RuleAction.BLOCK)),
                "裸域名不覆盖子域, 不应抵消");
        assertFalse(index.overrides(bare("ads.example/path", RuleAction.BLOCK)),
                "含路径的 URL 规则不是域名规则, 不应抵消");
        assertFalse(index.overrides(bare("*.ads.example", RuleAction.BLOCK)),
                "通配符规则不是域名规则, 不应抵消");
    }

    /** 带请求级修饰符的裸行不是域名规则，收紧会改变语义，不应参与域名抵消。 */
    @Test
    void bareDomainWithModifierIsNotTightened() {
        AllowIndex index = new AllowIndex();
        index.accept(adblock("ads.example", RuleAction.ALLOW));

        AdblockNetworkRule withModifier = new AdblockNetworkRule(
                new AdblockPattern(AdblockPattern.Kind.URL, "ads.example"), RuleAction.BLOCK,
                Set.of(), Set.of(), List.of(), PartyConstraint.ANY, false, false,
                List.of(new AdblockModifier(AdblockModifier.Type.REMOVEPARAM, "")),
                RuleDialect.ADGUARD);

        assertFalse(index.overrides(withModifier));
    }

    /**
     * {@code $important} 的阻断规则在客户端会压过例外规则，不能被 {@code @@} 剔除，
     * 否则上游显式声明的高优先级拦截会被静默丢弃。
     */
    @Test
    void importantBlockIsNotOverriddenByAllow() {
        AllowIndex index = new AllowIndex();
        index.accept(adblock("ads.example", RuleAction.ALLOW));

        assertFalse(index.overrides(important("ads.example", RuleAction.BLOCK)),
                "$important 阻断规则不应被同域名放行规则抵消");
        assertTrue(index.overrides(adblock("ads.example", RuleAction.BLOCK)),
                "普通阻断规则仍应被抵消");
    }

    private static AdblockNetworkRule important(String domain, RuleAction action) {
        return new AdblockNetworkRule(
                new AdblockPattern(AdblockPattern.Kind.DOMAIN_ANCHOR, domain), action,
                Set.of(), Set.of(), List.of(), PartyConstraint.ANY, false, true, List.of(),
                RuleDialect.ADGUARD);
    }

    private static AdblockNetworkRule bare(String value, RuleAction action) {
        return new AdblockNetworkRule(
                new AdblockPattern(AdblockPattern.Kind.URL, value), action,
                Set.of(), Set.of(), List.of(), PartyConstraint.ANY, false, false, List.of(),
                RuleDialect.ADGUARD);
    }

    private static AdblockNetworkRule adblock(String domain, RuleAction action) {
        return new AdblockNetworkRule(
                new AdblockPattern(AdblockPattern.Kind.DOMAIN_ANCHOR, domain), action,
                Set.of(), Set.of(), List.of(), PartyConstraint.ANY, false, false, List.of(),
                RuleDialect.ADGUARD);
    }
}
