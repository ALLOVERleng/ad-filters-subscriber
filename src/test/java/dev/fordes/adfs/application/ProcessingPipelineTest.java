package dev.fordes.adfs.application;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.fordes.adfs.config.ContainerFormat;
import dev.fordes.adfs.config.EffectiveConfig.ConversionConfig;
import dev.fordes.adfs.config.EffectiveConfig.RuleConfig;
import dev.fordes.adfs.config.EffectiveConfig;
import dev.fordes.adfs.config.InputSpec.LocalSource;
import dev.fordes.adfs.config.InputSpec;
import dev.fordes.adfs.config.OutputSpec;
import dev.fordes.adfs.config.RuleCategory;
import dev.fordes.adfs.config.RuleDialect;
import dev.fordes.adfs.config.RuleType;
import dev.fordes.adfs.format.FormatRegistry;
import dev.fordes.adfs.publish.OutputPublisher;
import dev.fordes.adfs.publish.StagingWorkspace;
import dev.fordes.adfs.source.LocalSourceReader;
import dev.fordes.adfs.testing.TestConfigs;
import dev.fordes.adfs.validation.dns.DnsValidator;

final class ProcessingPipelineTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void publishesDeterministicMultiFormatOutputAndAppliesBadfilter() throws IOException {
        Path input = temporaryDirectory.resolve("rules.txt");
        Files.writeString(input, """
                ||ads.example^
                ||disabled.example^$script
                ||disabled.example^$script,badfilter
                example.com##.advert
                ||opaque.example^$redirect=noopjs
                """, StandardCharsets.UTF_8);
        Path outputDir = temporaryDirectory.resolve("output");
        List<OutputSpec> outputs = List.of(
                new OutputSpec(Path.of("adguard.txt"), RuleType.ADBLOCK, RuleDialect.ADGUARD, ContainerFormat.TEXT, ""),
                new OutputSpec(Path.of("hosts"), RuleType.HOSTS, RuleDialect.NONE, ContainerFormat.TEXT, ""),
                new OutputSpec(Path.of("mihomo.yaml"), RuleType.MIHOMO, RuleDialect.CLASSICAL, ContainerFormat.YAML, ""),
                new OutputSpec(Path.of("sing-box.json"), RuleType.SING_BOX, RuleDialect.NONE, ContainerFormat.JSON, ""),
                new OutputSpec(Path.of("smartdns.conf"), RuleType.SMARTDNS, RuleDialect.NONE, ContainerFormat.TEXT, ""));
        EffectiveConfig base = TestConfigs.create(
                input, RuleType.ADBLOCK, RuleDialect.ADGUARD, outputDir, outputs, false);
        EffectiveConfig config = new EffectiveConfig(
                base.outputDir(), base.inputLimits(), base.http(), base.rules(),
                new ConversionConfig(true, true), base.dns(), base.inputs(), base.outputs());
        ProcessingPipeline pipeline = pipeline();

        ProcessingResult first = pipeline.process(config);
        Map<Path, byte[]> firstOutput = readOutput(outputDir);
        Files.writeString(outputDir.resolve("stale.txt"), "stale", StandardCharsets.UTF_8);
        ProcessingResult second = pipeline.process(config);
        Map<Path, byte[]> secondOutput = readOutput(outputDir);

        assertEquals(5, firstOutput.size());
        assertEquals(firstOutput.size(), secondOutput.size());
        assertTrue(secondOutput.keySet().containsAll(firstOutput.keySet()));
        firstOutput.forEach((path, bytes) -> assertArrayEquals(bytes, secondOutput.get(path), path.toString()));
        // 发布采用整目录原子替换: 目录内未声明的旧文件不会保留。
        assertFalse(Files.exists(outputDir.resolve("stale.txt")));
        assertTrue(Files.readString(outputDir.resolve("adguard.txt")).contains("||ads.example^"));
        assertTrue(Files.readString(outputDir.resolve("adguard.txt")).contains("||opaque.example^$redirect=noopjs"));
        assertFalse(Files.readString(outputDir.resolve("adguard.txt")).contains("disabled.example"));
        assertEquals(5, first.semanticRules() + first.opaqueRules());
        assertEquals(3, first.uniqueRules());
        assertEquals(first.uniqueRules(), second.uniqueRules());
    }

    @Test
    void guaranteesWhitelistAcrossExceptionAndRemovalTargets() throws IOException {
        Path input = temporaryDirectory.resolve("hosts.txt");
        Files.writeString(input, "0.0.0.0 white.example\n0.0.0.0 blocked.example\n", StandardCharsets.UTF_8);
        Path outputDir = temporaryDirectory.resolve("whitelist-output");
        List<OutputSpec> outputs = List.of(
                new OutputSpec(Path.of("adguard.txt"), RuleType.ADBLOCK, RuleDialect.ADGUARD, ContainerFormat.TEXT, ""),
                new OutputSpec(Path.of("smartdns.conf"), RuleType.SMARTDNS, RuleDialect.NONE, ContainerFormat.TEXT, ""),
                new OutputSpec(Path.of("hosts"), RuleType.HOSTS, RuleDialect.NONE, ContainerFormat.TEXT, ""),
                new OutputSpec(Path.of("sing-box.json"), RuleType.SING_BOX, RuleDialect.NONE, ContainerFormat.JSON, ""));
        EffectiveConfig base = TestConfigs.create(
                input, RuleType.HOSTS, RuleDialect.NONE, outputDir, outputs, false);
        RuleConfig rules = new RuleConfig(
                base.rules().minLength(), base.rules().maxLength(), Set.of("white.example"),
                base.rules().preprocessor());
        EffectiveConfig config = new EffectiveConfig(
                base.outputDir(), base.inputLimits(), base.http(), rules,
                new ConversionConfig(true, true), base.dns(), base.inputs(), base.outputs());

        pipeline().process(config);

        String adguard = Files.readString(outputDir.resolve("adguard.txt"));
        String smartDns = Files.readString(outputDir.resolve("smartdns.conf"));
        String hosts = Files.readString(outputDir.resolve("hosts"));
        String singBox = Files.readString(outputDir.resolve("sing-box.json"));
        assertTrue(adguard.contains("@@||white.example^"));
        assertTrue(smartDns.contains("address /white.example/-"));
        assertFalse(hosts.contains("white.example"));
        assertTrue(hosts.contains("blocked.example"));
        assertFalse(singBox.contains("white.example"));
        assertTrue(singBox.contains("blocked.example"));
    }

    @Test
    void excludesDeclaredRuleCategoryFromSelectedOutputs() throws IOException {
        Path dns = temporaryDirectory.resolve("exclude.dns");
        Files.writeString(dns, "exact.example\n||suffix.example^\n@@|safe.example|\n", StandardCharsets.UTF_8);
        Path adblock = temporaryDirectory.resolve("exclude.txt");
        Files.writeString(adblock, "||tracker.example^\n", StandardCharsets.UTF_8);
        Path outputDir = temporaryDirectory.resolve("exclude-output");
        EffectiveConfig base = TestConfigs.create(dns, RuleType.DNS, RuleDialect.ADGUARD, outputDir,
                List.of(TestConfigs.textOutput("browser.txt", RuleType.ADBLOCK, RuleDialect.ADGUARD,
                                Set.of(RuleCategory.DNS_DOMAIN)),
                        TestConfigs.textOutput("all.txt", RuleType.ADBLOCK, RuleDialect.ADGUARD)), false);
        EffectiveConfig config = new EffectiveConfig(base.outputDir(), base.inputLimits(), base.http(), base.rules(),
                new ConversionConfig(true, true), base.dns(), List.of(
                        new InputSpec("dns", new LocalSource(dns), RuleType.DNS, RuleDialect.ADGUARD),
                        new InputSpec("adblock", new LocalSource(adblock), RuleType.ADBLOCK, RuleDialect.ADGUARD)),
                base.outputs());

        ProcessingResult result = pipeline().process(config);

        String browser = Files.readString(outputDir.resolve("browser.txt"));
        assertTrue(browser.contains("||tracker.example^"));
        assertTrue(browser.contains("@@||safe.example^"));
        assertFalse(browser.contains("exact.example"));
        assertFalse(browser.contains("suffix.example"));
        String all = Files.readString(outputDir.resolve("all.txt"));
        assertTrue(all.contains("||tracker.example^"));
        assertTrue(all.contains("@@||safe.example^"));
        assertTrue(all.contains("||exact.example^"));
        assertTrue(all.contains("||suffix.example^"));
        assertEquals(2, result.outputs().get(Path.of("browser.txt")).filtered());
        assertEquals(0, result.outputs().get(Path.of("all.txt")).filtered());
    }

    private static ProcessingPipeline pipeline() {
        return new ProcessingPipeline(
                new FormatRegistry(),
                List.of(new LocalSourceReader()),
                new StagingWorkspace(),
                new OutputPublisher(),
                new DnsValidator());
    }

    @Test
    void rendersExactDnsRulesAsSubdomainBlocksWhenExpansionAllowed() throws IOException {
        Path input = temporaryDirectory.resolve("dns.txt");
        Files.writeString(input, "ads.example\n@@|safe.example|\n||suffix.example^\n");
        Path outputDir = temporaryDirectory.resolve("dns-output");
        EffectiveConfig base = TestConfigs.create(input, RuleType.DNS, RuleDialect.ADGUARD, outputDir,
                List.of(TestConfigs.textOutput("dns.txt", RuleType.DNS, RuleDialect.ADGUARD)), false);
        EffectiveConfig config = new EffectiveConfig(base.outputDir(), base.inputLimits(), base.http(),
                base.rules(), new ConversionConfig(true, true), base.dns(), base.inputs(), base.outputs());

        pipeline().process(config);

        assertEquals("||ads.example^\n@@||safe.example^\n||suffix.example^\n",
                Files.readString(outputDir.resolve("dns.txt")));
    }

    @Test
    void dropsExactDnsRulesWhenExpansionDisallowed() throws IOException {
        Path input = temporaryDirectory.resolve("dns-3.txt");
        Files.writeString(input, "ads.example\n@@|safe.example|\n||suffix.example^\n");
        Path outputDir = temporaryDirectory.resolve("dns-output-3");
        EffectiveConfig config = TestConfigs.create(input, RuleType.DNS, RuleDialect.ADGUARD, outputDir,
                List.of(TestConfigs.textOutput("dns.txt", RuleType.DNS, RuleDialect.ADGUARD)), false);

        pipeline().process(config);

        assertEquals("||suffix.example^\n", Files.readString(outputDir.resolve("dns.txt")));
    }

    @Test
    void collapsesIdenticalRenderedOrderedRules() throws IOException {
        Path input = temporaryDirectory.resolve("dup.conf");
        Files.writeString(input, "address=/dup.example/#\naddress=/dup.example/#\n");
        Path outputDir = temporaryDirectory.resolve("dup-output");
        EffectiveConfig config = TestConfigs.create(input, RuleType.DNSMASQ, RuleDialect.NONE, outputDir,
                List.of(TestConfigs.textOutput("dnsmasq.txt", RuleType.DNSMASQ, RuleDialect.NONE)), false);

        pipeline().process(config);

        assertEquals("address=/dup.example/#\n", Files.readString(outputDir.resolve("dnsmasq.txt")));
    }

    @Test
    void dropsRulesLongerThanRenderedLineLimit() throws IOException {
        Path input = temporaryDirectory.resolve("long.txt");
        Files.writeString(input, "a.example##." + "x".repeat(40_000) + "\n||short.example^\n");
        Path outputDir = temporaryDirectory.resolve("long-output");
        EffectiveConfig config = TestConfigs.create(input, RuleType.ADBLOCK, RuleDialect.ADGUARD, outputDir,
                List.of(TestConfigs.textOutput("easylist.txt", RuleType.ADBLOCK, RuleDialect.ADGUARD)), false);

        pipeline().process(config);

        assertEquals("||short.example^\n", Files.readString(outputDir.resolve("easylist.txt")));
    }

    @Test
    void removesBlocksCoveredByWhitelistedParentDomain() throws IOException {
        Path input = temporaryDirectory.resolve("whitelist.hosts");
        Files.writeString(input, "0.0.0.0 sub.allow.example\n0.0.0.0 other.example\n");
        Path outputDir = temporaryDirectory.resolve("whitelist-output");
        EffectiveConfig base = TestConfigs.create(input, RuleType.HOSTS, RuleDialect.NONE, outputDir,
                List.of(TestConfigs.textOutput("hosts.txt", RuleType.HOSTS, RuleDialect.NONE)), false);
        EffectiveConfig config = new EffectiveConfig(base.outputDir(), base.inputLimits(), base.http(),
                new RuleConfig(1, 65_536, Set.of("allow.example"), base.rules().preprocessor()),
                base.conversion(), base.dns(), base.inputs(), base.outputs());

        pipeline().process(config);

        assertEquals("0.0.0.0 other.example\n", Files.readString(outputDir.resolve("hosts.txt")));
    }

    @Test
    void dropsSafariAffinityMetadataFromAdblockOutput() throws IOException {
        Path input = temporaryDirectory.resolve("safari.txt");
        Files.writeString(input, "!#safari_cb_affinity(general)\n||ads.example^\n!#safari_cb_affinity\n");
        Path outputDir = temporaryDirectory.resolve("safari-output");
        EffectiveConfig base = TestConfigs.create(input, RuleType.ADBLOCK, RuleDialect.ADGUARD, outputDir,
                List.of(TestConfigs.textOutput("easylist.txt", RuleType.ADBLOCK, RuleDialect.ADGUARD)), false);
        EffectiveConfig config = new EffectiveConfig(base.outputDir(), base.inputLimits(), base.http(),
                base.rules(), new ConversionConfig(true, true), base.dns(), base.inputs(), base.outputs());

        pipeline().process(config);

        assertEquals("||ads.example^\n", Files.readString(outputDir.resolve("easylist.txt")));
    }

    @Test
    void preservesUnrelatedSingBoxRulesWithWhitelist() throws IOException {
        Path input = temporaryDirectory.resolve("sing-box.json");
        Files.writeString(input, """
                {"version":3,"rules":[
                  {"domain":"ads.example"},
                  {"domain_suffix":"safe.example"},
                  {"domain":"another.example","port":443}
                ]}
                """);
        Path outputDir = temporaryDirectory.resolve("sing-output");
        EffectiveConfig base = TestConfigs.create(input, RuleType.SING_BOX, RuleDialect.NONE, outputDir,
                List.of(new OutputSpec(Path.of("rules.json"), RuleType.SING_BOX, RuleDialect.NONE, ContainerFormat.JSON, "")), false);
        EffectiveConfig config = new EffectiveConfig(base.outputDir(), base.inputLimits(), base.http(),
                new RuleConfig(1, 65_536, Set.of("safe.example"), base.rules().preprocessor()),
                base.conversion(), base.dns(), base.inputs(), base.outputs());

        pipeline().process(config);

        String output = Files.readString(outputDir.resolve("rules.json"));
        assertTrue(output.contains("ads.example"));
        assertTrue(output.contains("another.example"));
        assertFalse(output.contains("safe.example"));
    }

    @Test
    void disablesOpaqueFiltersAndKeepsOtherDialects() throws IOException {
        Path adguard = temporaryDirectory.resolve("adguard.txt");
        Path ubo = temporaryDirectory.resolve("ubo.txt");
        Files.writeString(adguard, "||ads.example^$script\n||opaque.example^$redirect=noopjs\n"
                + "||opaque.example^$badfilter,redirect=noopjs\n");
        Files.writeString(ubo, "||ads.example^$script,badfilter\n");
        Path outputDir = temporaryDirectory.resolve("dialect-output");
        EffectiveConfig base = TestConfigs.create(adguard, RuleType.ADBLOCK, RuleDialect.ADGUARD, outputDir,
                List.of(TestConfigs.textOutput("adguard.txt", RuleType.ADBLOCK, RuleDialect.ADGUARD)), false);
        EffectiveConfig config = new EffectiveConfig(base.outputDir(), base.inputLimits(), base.http(), base.rules(),
                base.conversion(), base.dns(), List.of(
                        new InputSpec("adguard", new LocalSource(adguard), RuleType.ADBLOCK, RuleDialect.ADGUARD),
                        new InputSpec("ubo", new LocalSource(ubo), RuleType.ADBLOCK, RuleDialect.UBO)), base.outputs());

        pipeline().process(config);

        assertEquals("||ads.example^$script\n", Files.readString(outputDir.resolve("adguard.txt")));
    }

    @Test
    void skipsInvalidTextRulesAndPublishesEmptyOutput() throws IOException {
        Path input = temporaryDirectory.resolve("broken.txt");
        Path outputDir = Files.createDirectory(temporaryDirectory.resolve("existing-output"));
        Files.writeString(outputDir.resolve("hosts"), "previous\n");
        Files.writeString(input, "not-an-address example.com\n");
        EffectiveConfig config = TestConfigs.create(input, RuleType.HOSTS, RuleDialect.NONE, outputDir,
                List.of(TestConfigs.textOutput("hosts", RuleType.HOSTS, RuleDialect.NONE)), false);

        ProcessingResult result = pipeline().process(config);

        assertEquals("", Files.readString(outputDir.resolve("hosts")));
        assertEquals(1, result.sources().get("test-input").invalidRules());
        try (var paths = Files.list(temporaryDirectory)) {
            assertFalse(paths.anyMatch(path -> path.getFileName().toString().startsWith(".adfs-")));
        }
    }

    @Test
    void preservesComplexAdblockRulesAndAddsWhitelistException() throws IOException {
        Path input = temporaryDirectory.resolve("important.txt");
        Files.writeString(input, "||white.example^$important\n||white.example^$redirect=noopjs\n"
                + "white.example##.advert\n||blocked.example^\n");
        Path outputDir = temporaryDirectory.resolve("important-output");
        EffectiveConfig base = TestConfigs.create(input, RuleType.ADBLOCK, RuleDialect.ADGUARD, outputDir,
                List.of(TestConfigs.textOutput("adguard.txt", RuleType.ADBLOCK, RuleDialect.ADGUARD)), false);
        EffectiveConfig config = new EffectiveConfig(base.outputDir(), base.inputLimits(), base.http(),
                new RuleConfig(1, 65_536, Set.of("white.example"), base.rules().preprocessor()),
                base.conversion(), base.dns(), base.inputs(), base.outputs());

        pipeline().process(config);

        assertEquals("||white.example^$important\n||white.example^$redirect=noopjs\n"
                + "white.example##.advert\n||blocked.example^\n@@||white.example^\n",
                Files.readString(outputDir.resolve("adguard.txt")));
    }

    @Test
    void preservesNonEquivalentSmartDnsRulesAndAddsWhitelistRule() throws IOException {
        Path input = temporaryDirectory.resolve("smartdns.conf");
        Files.writeString(input, "address /child.white.example/#\naddress /-.white.example/0.0.0.0\n"
                + "domain-rules /white.example/ -address #\naddress /blocked.example/#\n");
        Path outputDir = temporaryDirectory.resolve("smartdns-output");
        EffectiveConfig base = TestConfigs.create(input, RuleType.SMARTDNS, RuleDialect.NONE, outputDir,
                List.of(TestConfigs.textOutput("smartdns.conf", RuleType.SMARTDNS, RuleDialect.NONE)), false);
        EffectiveConfig config = new EffectiveConfig(base.outputDir(), base.inputLimits(), base.http(),
                new RuleConfig(1, 65_536, Set.of("white.example"), base.rules().preprocessor()),
                base.conversion(), base.dns(), base.inputs(), base.outputs());

        pipeline().process(config);

        assertEquals("address /child.white.example/#\naddress /-.white.example/0.0.0.0\n"
                + "domain-rules /white.example/ -address #\naddress /blocked.example/#\n"
                + "address /white.example/-\n",
                Files.readString(outputDir.resolve("smartdns.conf")));
    }

    private static Map<Path, byte[]> readOutput(Path outputDir) throws IOException {
        Map<Path, byte[]> result = new LinkedHashMap<>();
        try (var paths = Files.walk(outputDir)) {
            for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
                result.put(outputDir.relativize(path), Files.readAllBytes(path));
            }
        }
        return result;
    }
}
