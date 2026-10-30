package dev.fordes.adfs.format.validate;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.fordes.adfs.config.ContainerFormat;
import dev.fordes.adfs.config.OutputSpec;
import dev.fordes.adfs.config.RuleDialect;
import dev.fordes.adfs.config.RuleType;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ArtifactValidatorTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void dropsLinesThatViolateAdblockSyntax() throws Exception {
        Path file = temporaryDirectory.resolve("easylist.txt");
        Files.writeString(file, "||ads.example^\naddress=/ads.example/#\n\n@@||safe.example^\n");

        ArtifactValidator.Result result = ArtifactValidator.validate(file, adblockSpec("easylist.txt"));

        assertEquals(4, result.lines());
        assertEquals(2, result.dropped());
        assertEquals("||ads.example^\n@@||safe.example^\n", Files.readString(file));
    }

    @Test
    void keepsPassthroughLinesForFormatsWithoutStrictGrammar() throws Exception {
        Path file = temporaryDirectory.resolve("smartdns.conf");
        Files.writeString(file, "address /ads.example/#\ndomain-rules /ads.example/ -address #\n");

        ArtifactValidator.Result result = ArtifactValidator.validate(file, new OutputSpec(
                Path.of("smartdns.conf"), RuleType.SMARTDNS, RuleDialect.NONE, ContainerFormat.TEXT, ""));

        assertEquals(0, result.dropped());
        assertEquals("address /ads.example/#\ndomain-rules /ads.example/ -address #\n",
                Files.readString(file));
    }

    @Test
    void leavesValidFileUntouched() throws Exception {
        Path file = temporaryDirectory.resolve("dns.txt");
        Files.writeString(file, "||ads.example^\n@@||safe.example^\n");

        ArtifactValidator.Result result = ArtifactValidator.validate(file, new OutputSpec(
                Path.of("dns.txt"), RuleType.DNS, RuleDialect.ADGUARD, ContainerFormat.TEXT, ""));

        assertEquals(2, result.lines());
        assertEquals(0, result.dropped());
    }

    private static OutputSpec adblockSpec(String name) {
        return new OutputSpec(Path.of(name), RuleType.ADBLOCK, RuleDialect.ADGUARD, ContainerFormat.TEXT, "");
    }
}
