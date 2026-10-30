package dev.fordes.adfs.rule.conversion;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.fordes.adfs.config.RuleDialect;
import dev.fordes.adfs.config.RuleType;
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

    private static AdblockNetworkRule adblock(String domain, RuleAction action) {
        return new AdblockNetworkRule(
                new AdblockPattern(AdblockPattern.Kind.DOMAIN_ANCHOR, domain), action,
                Set.of(), Set.of(), List.of(), PartyConstraint.ANY, false, false, List.of(),
                RuleDialect.ADGUARD);
    }
}
