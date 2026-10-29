package dev.fordes.adfs.rule.conversion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.fordes.adfs.config.RuleDialect;
import dev.fordes.adfs.rule.model.AdblockNetworkRule;
import dev.fordes.adfs.rule.model.AdblockPattern;
import dev.fordes.adfs.rule.model.AdblockResourceType;
import dev.fordes.adfs.rule.model.DomainName;
import dev.fordes.adfs.rule.model.DomainRule;
import dev.fordes.adfs.rule.model.PartyConstraint;
import dev.fordes.adfs.rule.model.RuleAction;
import dev.fordes.adfs.rule.model.SuffixDomain;

final class WhitelistMatcherTest {

    private static final List<DomainName> ALLOWED = List.of(new DomainName("allow.example"));

    @Test
    void matchesWhitelistedDomainAndItsSubdomains() {
        assertTrue(WhitelistMatcher.matches(
                new DomainRule(new SuffixDomain(new DomainName("allow.example")), RuleAction.BLOCK), ALLOWED));
        assertTrue(WhitelistMatcher.matches(
                new DomainRule(new SuffixDomain(new DomainName("sub.allow.example")), RuleAction.BLOCK), ALLOWED));
    }

    @Test
    void ignoresLookalikeDomainsAndAllowRules() {
        assertFalse(WhitelistMatcher.matches(
                new DomainRule(new SuffixDomain(new DomainName("notallow.example")), RuleAction.BLOCK), ALLOWED));
        assertFalse(WhitelistMatcher.matches(
                new DomainRule(new SuffixDomain(new DomainName("allow.example.net")), RuleAction.BLOCK), ALLOWED));
        assertFalse(WhitelistMatcher.matches(
                new DomainRule(new SuffixDomain(new DomainName("allow.example")), RuleAction.ALLOW), ALLOWED));
    }

    @Test
    void ignoresUnexpressibleAdblockPatternInsteadOfThrowing() {
        AdblockNetworkRule rule = new AdblockNetworkRule(
                new AdblockPattern(AdblockPattern.Kind.DOMAIN_ANCHOR, "bad_label.example"), RuleAction.BLOCK,
                Set.of(AdblockResourceType.SCRIPT), Set.of(), List.of(), PartyConstraint.ANY,
                false, true, List.of(), RuleDialect.ADGUARD);

        assertFalse(WhitelistMatcher.matches(rule, ALLOWED));
    }
}
