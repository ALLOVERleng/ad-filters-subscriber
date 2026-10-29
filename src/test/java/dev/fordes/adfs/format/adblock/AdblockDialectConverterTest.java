package dev.fordes.adfs.format.adblock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.fordes.adfs.config.RuleDialect;
import dev.fordes.adfs.rule.model.CosmeticOperator;
import dev.fordes.adfs.rule.model.CosmeticRule;
import dev.fordes.adfs.rule.model.DomainConstraint;
import dev.fordes.adfs.rule.model.DomainName;

final class AdblockDialectConverterTest {

    private static final List<DomainConstraint> DOMAINS = List.of(
            new DomainConstraint(new DomainName("a.example"), false));

    @Test
    void rejectsUboScriptletWrittenAsElementRule() {
        CosmeticRule adguard = new CosmeticRule(DOMAINS, false,
                CosmeticOperator.ELEMENT_HIDE, "+js(cookie-remover, x)", RuleDialect.ADGUARD);
        assertTrue(AdblockDialectConverter.convert(adguard, RuleDialect.ADGUARD).isEmpty());

        CosmeticRule ubo = new CosmeticRule(DOMAINS, false,
                CosmeticOperator.ELEMENT_HIDE, "+js(cookie-remover, x)", RuleDialect.UBO);
        assertTrue(AdblockDialectConverter.convert(ubo, RuleDialect.UBO).isPresent());
    }

    @Test
    void rejectsStyleOperatorInsideElementRuleForNonUboTargets() {
        CosmeticRule rule = new CosmeticRule(DOMAINS, false,
                CosmeticOperator.ELEMENT_HIDE, ".ad:style(display: none)", RuleDialect.ADGUARD);
        assertTrue(AdblockDialectConverter.convert(rule, RuleDialect.ADGUARD).isEmpty());
        assertTrue(AdblockDialectConverter.convert(rule, RuleDialect.UBO).isPresent());
    }

    @Test
    void rejectsCssInjectionWithoutDeclarationBlock() {
        CosmeticRule invalid = new CosmeticRule(DOMAINS, false,
                CosmeticOperator.CSS_INJECTION, "abort-on-property-read popit", RuleDialect.ADGUARD);
        assertTrue(AdblockDialectConverter.convert(invalid, RuleDialect.ADGUARD).isEmpty());

        CosmeticRule valid = new CosmeticRule(DOMAINS, false,
                CosmeticOperator.CSS_INJECTION, ".ad { display: none !important; }", RuleDialect.ADGUARD);
        assertTrue(AdblockDialectConverter.convert(valid, RuleDialect.ADGUARD).isPresent());
    }

    @Test
    void rejectsAdguardCssInjectionInUboOutput() {
        CosmeticRule rule = new CosmeticRule(DOMAINS, false,
                CosmeticOperator.CSS_INJECTION, ".ad { display: none !important; }", RuleDialect.ADGUARD);
        assertTrue(AdblockDialectConverter.convert(rule, RuleDialect.UBO).isEmpty());
        assertFalse(AdblockDialectConverter.convert(rule, RuleDialect.ADGUARD).isEmpty());
    }
}
