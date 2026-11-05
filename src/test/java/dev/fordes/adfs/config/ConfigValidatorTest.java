package dev.fordes.adfs.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.fordes.adfs.error.ConfigurationException;

final class ConfigValidatorTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void resolvesDefaultsAndNormalizesConfiguredDomains() throws Exception {
        Path input = temporaryDirectory.resolve("rules.txt");
        Files.writeString(input, "0.0.0.0 ads.example\n");
        RuleProperties rules = new RuleProperties();
        rules.setWhitelist(List.of("BÜCHER.Example."));
        PreprocessorProperties preprocessor = new PreprocessorProperties();
        preprocessor.setTrueTokens(List.of("desktop"));

        EffectiveConfig config = validator(
                temporaryDirectory.resolve("output"), input, "hosts", null,
                "result.txt", "hosts", null, null, rules, preprocessor).validate();

        assertEquals(67_108_864L, config.inputLimits().maxSize());
        assertEquals(Duration.ofSeconds(10), config.http().connectTimeout());
        assertEquals(RuleDialect.NONE, config.inputs().getFirst().dialect());
        assertEquals(ContainerFormat.TEXT, config.outputs().getFirst().container());
        assertEquals(List.of("desktop"), config.rules().preprocessor().trueTokens().stream().toList());
        assertEquals(List.of("xn--bcher-kva.example"), config.rules().whitelist().stream().toList());
        assertFalse(config.dns().enabled());
    }

    @Test
    void rejectsIncompatibleDialect() throws Exception {
        Path input = temporaryDirectory.resolve("input.txt");
        Files.writeString(input, "example.com\n");
        RuleProperties rules = new RuleProperties();
        PreprocessorProperties preprocessor = new PreprocessorProperties();

        ConfigValidator incompatible = validator(
                temporaryDirectory.resolve("output"), input, "hosts", "abp",
                "result.txt", "hosts", null, null, rules, preprocessor);
        assertThrows(ConfigurationException.class, incompatible::validate);
    }

    @Test
    void allowsInputUnderOutputDirectoryAndRejectsOutputInputConflict() throws Exception {
        Path input = temporaryDirectory.resolve("input.txt");
        Files.writeString(input, "example.com\n");
        RuleProperties rules = new RuleProperties();
        PreprocessorProperties preprocessor = new PreprocessorProperties();

        ConfigValidator separatePaths = validator(
                temporaryDirectory, input, "hosts", null,
                "result.txt", "hosts", null, null, rules, preprocessor);
        assertDoesNotThrow(separatePaths::validate);

        ConfigValidator conflictingPaths = validator(
                temporaryDirectory, input, "hosts", null,
                "input.txt", "hosts", null, null, rules, preprocessor);
        assertThrows(ConfigurationException.class, conflictingPaths::validate);
    }

    @Test
    void rejectsCrossFieldLimitsAndInvalidDnsLiteral() throws Exception {
        Path input = temporaryDirectory.resolve("rules.txt");
        Files.writeString(input, "example.com\n");
        RuleProperties rules = new RuleProperties();
        rules.setMinLength(20);
        rules.setMaxLength(10);
        PreprocessorProperties preprocessor = new PreprocessorProperties();
        ConfigValidator limits = validator(
                temporaryDirectory.resolve("output"), input, "hosts", null,
                "result.txt", "hosts", null, null, rules, preprocessor);
        assertThrows(ConfigurationException.class, limits::validate);

        rules.setMinLength(1);
        rules.setMaxLength(10);
        ConfigValidator dns = validator(
                temporaryDirectory.resolve("output"), input, "hosts", null,
                "result.txt", "hosts", null, "resolver.example", rules, preprocessor);
        assertThrows(ConfigurationException.class, dns::validate);
    }

    @Test
    void resolvesExcludeCategoriesAndRejectsUnknownOnes() throws Exception {
        Path input = temporaryDirectory.resolve("rules.txt");
        Files.writeString(input, "0.0.0.0 ads.example\n");
        RuleProperties rules = new RuleProperties();
        PreprocessorProperties preprocessor = new PreprocessorProperties();

        EffectiveConfig config = validator(
                temporaryDirectory.resolve("output"), input, "hosts", null,
                "easylist.txt", "adblock", "adguard", null, rules, preprocessor, List.of("dns-domain")).validate();
        assertEquals(Set.of(RuleCategory.DNS_DOMAIN), config.outputs().getFirst().exclude());

        EffectiveConfig plain = validator(
                temporaryDirectory.resolve("output"), input, "hosts", null,
                "easylist.txt", "adblock", "adguard", null, rules, preprocessor).validate();
        assertTrue(plain.outputs().getFirst().exclude().isEmpty());

        ConfigValidator unknown = validator(
                temporaryDirectory.resolve("output"), input, "hosts", null,
                "easylist.txt", "adblock", "adguard", null, rules, preprocessor,
                List.of("dns-domain", "not-a-category"));
        assertThrows(ConfigurationException.class, unknown::validate);
    }

    private static ConfigValidator validator(
            Path outputDir,
            Path inputPath,
            String inputType,
            String inputDialect,
            String outputName,
            String outputType,
            String outputDialect,
            String dnsServer,
            RuleProperties rules,
            PreprocessorProperties preprocessor) {
        return validator(outputDir, inputPath, inputType, inputDialect, outputName, outputType, outputDialect,
                dnsServer, rules, preprocessor, List.of());
    }

    @Test
    void splitsAdblockOutputIntoElementAndUrlFiles() throws Exception {
        Path input = temporaryDirectory.resolve("rules.txt");
        Files.writeString(input, "0.0.0.0 ads.example\n");
        RuleProperties rules = new RuleProperties();
        PreprocessorProperties preprocessor = new PreprocessorProperties();

        EffectiveConfig config = validator(
                temporaryDirectory.resolve("output"), input, "hosts", null,
                "browser.txt", "adblock", "adguard", null, rules, preprocessor, List.of(), true).validate();

        assertEquals(2, config.outputs().size());
        assertEquals(Path.of("browser.element.txt"), config.outputs().get(0).path());
        assertEquals(RuleShape.ELEMENT, config.outputs().get(0).shape());
        assertEquals(Path.of("browser.url.txt"), config.outputs().get(1).path());
        assertEquals(RuleShape.URL, config.outputs().get(1).shape());
    }

    @Test
    void rejectsSplitForNonAdblockOutput() throws Exception {
        Path input = temporaryDirectory.resolve("rules.txt");
        Files.writeString(input, "0.0.0.0 ads.example\n");

        ConfigValidator split = validator(
                temporaryDirectory.resolve("output"), input, "hosts", null,
                "hosts.txt", "hosts", null, null, new RuleProperties(), new PreprocessorProperties(),
                List.of(), true);

        assertThrows(ConfigurationException.class, split::validate);
    }

    @Test
    void parsesFallbackSourcesAndDropsDuplicates() {
        AdfsProperties adfs = new AdfsProperties();
        adfs.setOutputDir(temporaryDirectory.resolve("output"));
        InputProperties input = new InputProperties(0);
        input.setName("remote");
        input.setPath("https://raw.githubusercontent.com/owner/repo/main/list.txt");
        input.setType("adblock");
        input.setFallback(List.of(
                "https://gh-proxy.com/https://raw.githubusercontent.com/owner/repo/main/list.txt",
                "https://raw.githubusercontent.com/owner/repo/main/list.txt",
                "https://gh-proxy.com/https://raw.githubusercontent.com/owner/repo/main/list.txt",
                " "));
        OutputProperties output = new OutputProperties(0);
        output.setName("result.txt");
        output.setType("adblock");

        ConfigValidator validator = new ConfigValidator(adfs, new InputLimitProperties(), new HttpProperties(),
                new RuleProperties(), new PreprocessorProperties(), new ConversionProperties(), new DnsProperties(),
                new DnsCacheProperties(), List.of(input), List.of(output));

        InputSpec spec = validator.validate().inputs().getFirst();

        // 与主地址相同、重复以及空白的备用地址都会被丢掉, 只剩一条镜像地址。
        assertEquals(1, spec.fallback().size());
        assertEquals(2, spec.candidates().size());
        assertEquals("https://gh-proxy.com/https://raw.githubusercontent.com/owner/repo/main/list.txt",
                ((InputSpec.HttpSource) spec.fallback().getFirst()).uri().toString());
    }

    @Test
    void rejectsFallbackOfDifferentKind() throws Exception {
        Path inputPath = temporaryDirectory.resolve("local.txt");
        Files.writeString(inputPath, "example.com\n");
        AdfsProperties adfs = new AdfsProperties();
        adfs.setOutputDir(temporaryDirectory.resolve("output"));
        InputProperties input = new InputProperties(0);
        input.setName("local");
        input.setPath(inputPath.toString());
        input.setType("hosts");
        input.setFallback(List.of("https://example.com/list.txt"));
        OutputProperties output = new OutputProperties(0);
        output.setName("result.txt");
        output.setType("hosts");

        ConfigValidator validator = new ConfigValidator(adfs, new InputLimitProperties(), new HttpProperties(),
                new RuleProperties(), new PreprocessorProperties(), new ConversionProperties(), new DnsProperties(),
                new DnsCacheProperties(), List.of(input), List.of(output));

        assertThrows(ConfigurationException.class, validator::validate);
    }

    private static ConfigValidator validator(
            Path outputDir,
            Path inputPath,
            String inputType,
            String inputDialect,
            String outputName,
            String outputType,
            String outputDialect,
            String dnsServer,
            RuleProperties rules,
            PreprocessorProperties preprocessor,
            List<String> exclude) {
        return validator(outputDir, inputPath, inputType, inputDialect, outputName, outputType, outputDialect,
                dnsServer, rules, preprocessor, exclude, false);
    }

    private static ConfigValidator validator(
            Path outputDir,
            Path inputPath,
            String inputType,
            String inputDialect,
            String outputName,
            String outputType,
            String outputDialect,
            String dnsServer,
            RuleProperties rules,
            PreprocessorProperties preprocessor,
            List<String> exclude,
            boolean split) {
        AdfsProperties adfs = new AdfsProperties();
        adfs.setOutputDir(outputDir);
        InputProperties input = new InputProperties(0);
        input.setName("input");
        input.setPath(inputPath.toString());
        input.setType(inputType);
        input.setDialect(inputDialect);
        OutputProperties output = new OutputProperties(0);
        output.setName(outputName);
        output.setType(outputType);
        output.setDialect(outputDialect);
        output.setExclude(exclude);
        output.setSplit(split);
        DnsProperties dns = new DnsProperties();
        dns.setServers(dnsServer == null ? List.of() : List.of(dnsServer));
        return new ConfigValidator(
                adfs,
                new InputLimitProperties(),
                new HttpProperties(),
                rules,
                preprocessor,
                new ConversionProperties(),
                dns,
                new DnsCacheProperties(),
                List.of(input),
                List.of(output));
    }
}
