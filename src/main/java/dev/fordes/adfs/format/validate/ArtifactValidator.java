package dev.fordes.adfs.format.validate;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import dev.fordes.adfs.config.ContainerFormat;
import dev.fordes.adfs.config.OutputSpec;
import dev.fordes.adfs.error.OutputException;
import dev.fordes.adfs.format.SyntaxValidator;

/**
 * 产物级语法校验: 在写出完成、注入文件头之前, 对暂存目录里的最终文件再扫一遍。
 *
 * <p>行级校验已经发生在 {@code OutputRuleProcessor} 里, 但它看不到两类内容: ① {@code finish()}
 * 追加的白名单规则; ② Writer 自身拼接出的行（例如某个格式前缀写错）。这里按“最终文件”复核,
 * 语法边界明确的格式（adblock / DNS, 同为 adblock 语法家族）直接剔除不合规行;
 * hosts / dnsmasq / smartdns / mihomo 会透传上游原文, 无法据格式语法判定真伪,
 * 只做通用检查（空行、控制字符、超长行），避免把合法的透传行误删。</p>
 *
 * <p>实现按流式两遍扫描: 先数, 有剔除才重写, 避免把 90 万行的产物整体读进内存。</p>
 */
public final class ArtifactValidator {

    /** 与 {@code OutputRuleProcessor} 保持一致的渲染长度上限。 */
    static final int MAX_LINE_LENGTH = 32_768;

    private static final String SEPARATOR = "\n";

    public record Result(long lines, long dropped) {

        public static final Result EMPTY = new Result(0, 0);
    }

    private ArtifactValidator() {
    }

    public static Result validate(Path file, OutputSpec spec) {
        if (spec.container() != ContainerFormat.TEXT || !Files.isRegularFile(file)) {
            return Result.EMPTY;
        }
        SyntaxValidator syntax = OutputSyntax.of(spec);
        boolean strict = strict(spec);
        Predicate<String> keep = line -> intact(line) && (!strict || syntax.isValid(line));
        long[] counts = count(file, keep);
        if (counts[1] == 0) {
            return new Result(counts[0], 0);
        }
        rewrite(file, keep);
        return new Result(counts[0], counts[1]);
    }

    private static long[] count(Path file, Predicate<String> keep) {
        long lines = 0;
        long dropped = 0;
        try (Stream<String> stream = Files.lines(file, StandardCharsets.UTF_8)) {
            for (Iterator<String> iterator = stream.iterator(); iterator.hasNext(); ) {
                lines++;
                if (!keep.test(iterator.next())) {
                    dropped++;
                }
            }
        } catch (IOException | UncheckedIOException exception) {
            throw new OutputException("校验产物语法失败: " + file, exception);
        }
        return new long[] {lines, dropped};
    }

    private static void rewrite(Path file, Predicate<String> keep) {
        Path temporary = null;
        try {
            temporary = Files.createTempFile(file.getParent(), ".adfs-validate-", ".tmp");
            try (Stream<String> stream = Files.lines(file, StandardCharsets.UTF_8);
                    var writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                for (Iterator<String> iterator = stream.iterator(); iterator.hasNext(); ) {
                    String line = iterator.next();
                    if (keep.test(line)) {
                        writer.write(line);
                        writer.write(SEPARATOR);
                    }
                }
            }
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            temporary = null;
        } catch (IOException | UncheckedIOException exception) {
            throw new OutputException("重写产物失败: " + file, exception);
        } finally {
            deleteQuietly(temporary);
        }
    }

    private static void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // 临时文件清理失败不影响校验结果, 暂存目录整体会被丢弃。
        }
    }

    private static boolean strict(OutputSpec spec) {
        return switch (spec.type()) {
            case ADBLOCK, DNS -> true;
            case HOSTS, DNSMASQ, SMARTDNS, MIHOMO, SING_BOX -> false;
        };
    }

    private static boolean intact(String line) {
        return !line.isBlank() && line.length() <= MAX_LINE_LENGTH
                && line.chars().noneMatch(Character::isISOControl);
    }
}
