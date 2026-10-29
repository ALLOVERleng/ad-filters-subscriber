package dev.fordes.adfs.rule.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ReservedHostsTest {

    @Test
    void matchesReservedNames() {
        assertTrue(ReservedHosts.isReservedName("localhost"));
        assertTrue(ReservedHosts.isReservedName("LOCALHOST.localdomain"));
        assertTrue(ReservedHosts.isReservedName("||ip6-localhost^"));
        assertTrue(ReservedHosts.isReservedName(".printer.local"));
        assertTrue(ReservedHosts.isReservedName("broadcasthost"));
        assertFalse(ReservedHosts.isReservedName("notlocalhost.example"));
        assertFalse(ReservedHosts.isReservedName("local.example.com"));
        assertFalse(ReservedHosts.isReservedName(""));
    }

    @Test
    void filtersRuleEntries() {
        assertTrue(ReservedHosts.isReserved(
                new DomainRule(new ExactDomain(new DomainName("localhost")), RuleAction.BLOCK)));
        assertTrue(ReservedHosts.isReserved(
                new HostMappingRule(IpAddress.parse("0.0.0.0"), new DomainName("ip6-loopback"))));
        assertFalse(ReservedHosts.isReserved(
                new DomainRule(new ExactDomain(new DomainName("ads.example.com")), RuleAction.BLOCK)));
    }
}
