package dev.fordes.adfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

final class AdfsCommandTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void publishesLocalRulesWithExplicitConfigurationAndDefaultOptions() throws IOException {
        Path input = temporaryDirectory.resolve("rules.txt");
        Path output = temporaryDirectory.resolve("output");
        Path config = temporaryDirectory.resolve("custom.yml");
        Files.writeString(input, "||ads.example^\n");
        Files.writeString(config, """
                adfs:
                  input:
                    - name: local
                      path: '%s'
                      type: adblock
                  output:
                    - name: rules.txt
                      type: adblock
                  config:
                    output-dir: '%s'
                """.formatted(input, output));

        assertEquals(0, AdfsCommand.execute(new String[]{"-c", config.toString()}));
        // adblock 文本输出默认拆分: URL 规则与元素规则各一个文件。
        assertTrue(Files.readString(output.resolve("rules.url.txt")).contains("||ads.example^"));
        assertTrue(Files.exists(output.resolve("rules.element.txt")));
        assertFalse(Files.exists(output.resolve("rules.txt")));
    }

    @Test
    void handlesHelpAndVersionBeforeLoadingConfiguration() {
        String missing = temporaryDirectory.resolve("missing.yml").toString();
        assertEquals(0, AdfsCommand.execute(new String[]{"--help", "-c", missing}));
        assertEquals(0, AdfsCommand.execute(new String[]{"--version", "-c", missing}));
        assertEquals(2, AdfsCommand.execute(new String[]{"-c", missing}));
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void mapsConfigurationBindingFailureToConfigurationExitCode() {
        String key = "adfs.config.dns.concurrency";
        String previous = System.setProperty(key, "0");
        try {
            assertEquals(2, AdfsCommand.execute(new String[0]));
        } finally {
            if (previous == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, previous);
            }
        }
    }
}
