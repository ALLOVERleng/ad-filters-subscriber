package dev.fordes.adfs.rule.filter;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.fordes.adfs.config.RuleDialect;
import dev.fordes.adfs.config.RuleType;
import dev.fordes.adfs.rule.model.AdblockNetworkRule;
import dev.fordes.adfs.rule.model.AdblockPattern;
import dev.fordes.adfs.rule.model.DomainName;
import dev.fordes.adfs.rule.model.DomainRule;
import dev.fordes.adfs.rule.model.ExactDomain;
import dev.fordes.adfs.rule.model.OpaqueRule;
import dev.fordes.adfs.rule.model.PartyConstraint;
import dev.fordes.adfs.rule.model.RuleAction;
import dev.fordes.adfs.rule.model.SuffixDomain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SourceExclusionTest {

    @Test
    void dropsDomainAndItsSubdomains() {
        SourceExclusion exclusion = SourceExclusion.of(List.of("gh-proxy.com"));

        assertTrue(exclusion.excludes(block("gh-proxy.com")));
        assertTrue(exclusion.excludes(block("sub.gh-proxy.com")));
        assertFalse(exclusion.excludes(block("notgh-proxy.com")));
        assertFalse(exclusion.excludes(block("gh-proxy.com.example")));
    }

    @Test
    void wildcardOnlyMatchesSubdomains() {
        SourceExclusion exclusion = SourceExclusion.of(List.of("*.example.com"));

        assertTrue(exclusion.excludes(block("ads.example.com")));
        assertFalse(exclusion.excludes(block("example.com")));
    }

    @Test
    void acceptsAdblockSyntaxAndKeywords() {
        SourceExclusion exclusion = SourceExclusion.of(List.of("||ads.example^", "adserver"));

        assertTrue(exclusion.excludes(adblock("ads.example")));
        assertTrue(exclusion.excludes(new OpaqueRule(RuleType.ADBLOCK, RuleDialect.ADGUARD, null,
                "||other.example^$redirect=noopjs,adserver=1")));
        assertFalse(exclusion.excludes(adblock("other.example")));
    }

    @Test
    void keepsAllowRules() {
        SourceExclusion exclusion = SourceExclusion.of(List.of("ads.example"));

        assertFalse(exclusion.excludes(
                new DomainRule(new SuffixDomain(new DomainName("ads.example")), RuleAction.ALLOW)));
    }

    private static DomainRule block(String domain) {
        return new DomainRule(new ExactDomain(new DomainName(domain)), RuleAction.BLOCK);
    }

    private static AdblockNetworkRule adblock(String domain) {
        return new AdblockNetworkRule(
                new AdblockPattern(AdblockPattern.Kind.DOMAIN_ANCHOR, domain), RuleAction.BLOCK,
                Set.of(), Set.of(), List.of(), PartyConstraint.ANY, false, false, List.of(),
                RuleDialect.ADGUARD);
    }
}
