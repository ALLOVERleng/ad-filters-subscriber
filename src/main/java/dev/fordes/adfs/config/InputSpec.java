package dev.fordes.adfs.config;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import dev.fordes.adfs.rule.filter.SourceExclusion;

/**
 * 单个输入源的声明。
 *
 * @param exclude 源级排除：命中的规则在进入处理管线之前丢弃，用于隔离上游误报。
 *                支持 {@code example.com}（含子域）、{@code *.example.com}（仅子域）
 *                与关键词三种写法，见 {@link SourceExclusion}。
 */
public record InputSpec(String name, SourceLocation source, RuleType type, RuleDialect dialect,
        List<String> exclude) {

    public InputSpec {
        exclude = List.copyOf(exclude);
    }

    public InputSpec(String name, SourceLocation source, RuleType type, RuleDialect dialect) {
        this(name, source, type, dialect, List.of());
    }

    public sealed interface SourceLocation permits HttpSource, LocalSource {
    }

    public record LocalSource(Path path) implements SourceLocation {
    }

    public record HttpSource(URI uri) implements SourceLocation {
    }
}
