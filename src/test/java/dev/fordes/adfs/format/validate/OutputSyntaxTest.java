package dev.fordes.adfs.format.validate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import dev.fordes.adfs.config.ContainerFormat;
import dev.fordes.adfs.config.OutputSpec;
import dev.fordes.adfs.config.RuleDialect;
import dev.fordes.adfs.config.RuleType;
import dev.fordes.adfs.format.SyntaxValidator;

final class OutputSyntaxTest {

    @Test
    void adblockAcceptsWriterOutputAndRejectsMalformedLines() {
        SyntaxValidator syntax = validator(RuleType.ADBLOCK, RuleDialect.ADGUARD, ContainerFormat.TEXT);

        assertTrue(syntax.isValid("||ads.example^"));
        assertTrue(syntax.isValid("@@||white.example^"));
        assertTrue(syntax.isValid("||ads.example^$important"));
        assertTrue(syntax.isValid("||ads.example^$domain=~internal.example"));
        assertTrue(syntax.isValid("||ads.example^$redirect=noopjs"));
        assertTrue(syntax.isValid("/ads[0-9]+/"));
        assertTrue(syntax.isValid("example.com##.advert"));
        assertTrue(syntax.isValid("example.com#@#.advert"));

        assertFalse(syntax.isValid(""));
        assertFalse(syntax.isValid("   "));
        assertFalse(syntax.isValid("||^"));
        assertFalse(syntax.isValid("||ads.example^$"));
        assertFalse(syntax.isValid("||ads.example^$script,,badfilter"));
        assertFalse(syntax.isValid("example.com##"));
        assertFalse(syntax.isValid("/unclosed"));
        assertFalse(syntax.isValid("address=/ads.example/#"));
        assertFalse(syntax.isValid("address /ads.example/#"));
        assertFalse(syntax.isValid("0.0.0.0 ads.example"));
    }

    @Test
    void dnsAcceptsDomainAndMappingLines() {
        SyntaxValidator syntax = validator(RuleType.DNS, RuleDialect.ADGUARD, ContainerFormat.TEXT);

        assertTrue(syntax.isValid("||ads.example^"));
        assertTrue(syntax.isValid("@@||white.example^"));
        assertTrue(syntax.isValid("||ads.example^$important"));
        assertTrue(syntax.isValid("0.0.0.0 ads.example"));

        assertFalse(syntax.isValid(""));
        assertFalse(syntax.isValid("||^"));
        assertFalse(syntax.isValid("||ads.example^$script"));
        assertFalse(syntax.isValid("address=/ads.example/#"));
    }

    @Test
    void hostsAcceptsOnlyAddressMappingLines() {
        SyntaxValidator syntax = validator(RuleType.HOSTS, RuleDialect.NONE, ContainerFormat.TEXT);

        assertTrue(syntax.isValid("0.0.0.0 ads.example"));
        assertTrue(syntax.isValid("127.0.0.1 local.example"));

        assertFalse(syntax.isValid("||ads.example^"));
        assertFalse(syntax.isValid("0.0.0.0"));
        assertFalse(syntax.isValid("0.0.0.0 two hosts"));
        assertFalse(syntax.isValid("0.0.0.0 bad_host!"));
    }

    @Test
    void dnsmasqAndSmartDnsAcceptAddressDirectives() {
        SyntaxValidator dnsmasq = validator(RuleType.DNSMASQ, RuleDialect.NONE, ContainerFormat.TEXT);
        assertTrue(dnsmasq.isValid("address=/ads.example/#"));
        assertTrue(dnsmasq.isValid("address=/ads.example/0.0.0.0"));
        assertTrue(dnsmasq.isValid("address=/ads.example/#4"));
        assertFalse(dnsmasq.isValid("address=/ads.example/"));
        assertFalse(dnsmasq.isValid("||ads.example^"));

        SyntaxValidator smartDns = validator(RuleType.SMARTDNS, RuleDialect.NONE, ContainerFormat.TEXT);
        assertTrue(smartDns.isValid("address /ads.example/#"));
        assertTrue(smartDns.isValid("address /-.ads.example/0.0.0.0"));
        assertTrue(smartDns.isValid("address /white.example/-"));
        assertFalse(smartDns.isValid("address=//#"));
        assertFalse(smartDns.isValid("||ads.example^"));
    }

    @Test
    void mihomoValidatesPerDialect() {
        SyntaxValidator classical = validator(RuleType.MIHOMO, RuleDialect.CLASSICAL, ContainerFormat.YAML);
        assertTrue(classical.isValid("DOMAIN-SUFFIX,ads.example"));
        assertTrue(classical.isValid("IP-CIDR,10.0.0.0/8"));
        assertFalse(classical.isValid("ads.example"));

        SyntaxValidator domain = validator(RuleType.MIHOMO, RuleDialect.DOMAIN, ContainerFormat.TEXT);
        assertTrue(domain.isValid("+.ads.example"));
        assertTrue(domain.isValid("ads.example"));
        assertFalse(domain.isValid("DOMAIN,ads.example"));

        SyntaxValidator ipCidr = validator(RuleType.MIHOMO, RuleDialect.IPCIDR, ContainerFormat.TEXT);
        assertTrue(ipCidr.isValid("10.0.0.0/8"));
        assertFalse(ipCidr.isValid("ads.example"));
    }

    @Test
    void singBoxSkipsLineLevelValidation() {
        SyntaxValidator syntax = validator(RuleType.SING_BOX, RuleDialect.NONE, ContainerFormat.JSON);
        assertTrue(syntax.isValid("{\"domain_suffix\":[\"ads.example\"]}"));
        assertTrue(syntax.isValid(""));
    }

    private static SyntaxValidator validator(RuleType type, RuleDialect dialect, ContainerFormat container) {
        return OutputSyntax.of(new OutputSpec(Path.of("out.txt"), type, dialect, container, ""));
    }
}
