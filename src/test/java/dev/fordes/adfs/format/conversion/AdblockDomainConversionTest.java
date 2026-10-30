package dev.fordes.adfs.format.conversion;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.fordes.adfs.config.RuleDialect;
import dev.fordes.adfs.rule.model.AdblockNetworkRule;
import dev.fordes.adfs.rule.model.AdblockPattern;
import dev.fordes.adfs.rule.model.AdblockResourceType;
import dev.fordes.adfs.rule.model.DomainConstraint;
import dev.fordes.adfs.rule.model.DomainName;
import dev.fordes.adfs.rule.model.PartyConstraint;
import dev.fordes.adfs.rule.model.RuleAction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AdblockDomainConversionTest {

    @Test
    void plainDomainRuleIsExpressible() {
        assertTrue(AdblockDomainConversion.supportsSubject(rule(RuleAction.BLOCK,
                List.of(), Set.of(), PartyConstraint.ANY)));
    }

    @Test
    void siteScopedRuleIsNotExpressible() {
        // @@||raw.githubusercontent.com^$domain=viewscreen.githubusercontent.com
        // 放大成整域放行会抵消其它来源对同域名的拦截, 两个方向都必须丢弃。
        List<DomainConstraint> constraints = List.of(
                new DomainConstraint(new DomainName("viewscreen.githubusercontent.com"), false));

        AdblockNetworkRule allow = rule(RuleAction.ALLOW, constraints, Set.of(), PartyConstraint.ANY);
        assertFalse(AdblockDomainConversion.supportsRequestScope(allow));
        assertFalse(AdblockDomainConversion.supportsSubject(allow));

        AdblockNetworkRule block = rule(RuleAction.BLOCK, constraints, Set.of(), PartyConstraint.ANY);
        assertFalse(AdblockDomainConversion.supportsSubject(block));
    }

    @Test
    void typeAndPartyConstraintsStillAllowWidening() {
        // $script / $third-party 在 DNS 语义里本就按整域处理, 保留并计为 EXPANDED。
        assertTrue(AdblockDomainConversion.supportsSubject(rule(RuleAction.BLOCK, List.of(),
                Set.of(AdblockResourceType.SCRIPT), PartyConstraint.THIRD_PARTY)));
    }

    private static AdblockNetworkRule rule(
            RuleAction action,
            List<DomainConstraint> constraints,
            Set<AdblockResourceType> types,
            PartyConstraint party) {
        return new AdblockNetworkRule(
                new AdblockPattern(AdblockPattern.Kind.DOMAIN_ANCHOR, "ads.example"), action,
                types, Set.of(), constraints, party, false, false, List.of(), RuleDialect.ADGUARD);
    }
}
