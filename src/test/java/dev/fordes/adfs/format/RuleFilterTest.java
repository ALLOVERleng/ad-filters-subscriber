package dev.fordes.adfs.format;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.fordes.adfs.config.ContainerFormat;
import dev.fordes.adfs.config.OutputSpec;
import dev.fordes.adfs.config.RuleCategory;
import dev.fordes.adfs.config.RuleDialect;
import dev.fordes.adfs.config.RuleType;
import dev.fordes.adfs.rule.model.AdblockNetworkRule;
import dev.fordes.adfs.rule.model.AdblockPattern;
import dev.fordes.adfs.rule.model.DnsAddressRule;
import dev.fordes.adfs.rule.model.DnsResponse;
import dev.fordes.adfs.rule.model.DomainName;
import dev.fordes.adfs.rule.model.DomainRule;
import dev.fordes.adfs.rule.model.HostMappingRule;
import dev.fordes.adfs.rule.model.IpAddress;
import dev.fordes.adfs.rule.model.IpFamily;
import dev.fordes.adfs.rule.model.PartyConstraint;
import dev.fordes.adfs.rule.model.RuleAction;
import dev.fordes.adfs.rule.model.SuffixDomain;

final class RuleFilterTest {

    private static final OutputSpec EXCLUDING = new OutputSpec(
            Path.of("browser.txt"), RuleType.ADBLOCK, RuleDialect.ADGUARD, ContainerFormat.TEXT, "",
            Set.of(RuleCategory.DNS_DOMAIN));

    private static final OutputSpec KEEPING = new OutputSpec(
            Path.of("all.txt"), RuleType.ADBLOCK, RuleDialect.ADGUARD, ContainerFormat.TEXT, "");

    @Test
    void excludesDomainBlocksProducedByDnsInputs() {
        assertTrue(RuleFilter.isExcluded(EXCLUDING, domainRule(RuleAction.BLOCK)));
        assertTrue(RuleFilter.isExcluded(EXCLUDING, new HostMappingRule(
                IpAddress.parse("0.0.0.0"), new DomainName("blocked.example"))));
        assertTrue(RuleFilter.isExcluded(EXCLUDING, new DnsAddressRule(RuleType.SMARTDNS,
                new SuffixDomain(new DomainName("blocked.example")), DnsResponse.SOA,
                Set.of(IpFamily.IPV4), List.of())));
    }

    @Test
    void keepsRulesThatStillMakeSenseInBrowsers() {
        assertFalse(RuleFilter.isExcluded(EXCLUDING, domainRule(RuleAction.ALLOW)));
        assertFalse(RuleFilter.isExcluded(EXCLUDING, new AdblockNetworkRule(
                new AdblockPattern(AdblockPattern.Kind.DOMAIN_ANCHOR, "doubleclick.net"), RuleAction.BLOCK,
                Set.of(), Set.of(), List.of(), PartyConstraint.ANY, false, false, List.of(), RuleDialect.CORE)));
        assertFalse(RuleFilter.isExcluded(EXCLUDING, new DnsAddressRule(RuleType.SMARTDNS,
                new SuffixDomain(new DomainName("ignored.example")), DnsResponse.IGNORE,
                Set.of(IpFamily.IPV4), List.of())));
    }

    @Test
    void keepsEverythingWhenNoCategoryIsExcluded() {
        assertFalse(RuleFilter.isExcluded(KEEPING, domainRule(RuleAction.BLOCK)));
        assertFalse(RuleFilter.isExcluded(KEEPING, new HostMappingRule(
                IpAddress.parse("0.0.0.0"), new DomainName("blocked.example"))));
    }

    private static DomainRule domainRule(RuleAction action) {
        return new DomainRule(new SuffixDomain(new DomainName("ads.example")), action);
    }
}
