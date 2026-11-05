package dev.fordes.adfs.config;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dev.fordes.adfs.rule.filter.SourceExclusion;

/**
 * 单个输入源的声明。
 *
 * @param exclude  源级排除：命中的规则在进入处理管线之前丢弃，用于隔离上游误报。
 *                 支持 {@code example.com}（含子域）、{@code *.example.com}（仅子域）
 *                 与关键词三种写法，见 {@link SourceExclusion}。
 * @param optional 可选输入：所有地址都读不到时跳过该来源，不终止整次构建。
 * @param fallback 备用地址：主地址打不开时按声明顺序尝试。用于上游换仓库、镜像抖动，
 *                 或 CI 能访问而本地被阻断的场景（例如 raw 域名在部分网络下不可达）。
 */
public record InputSpec(String name, SourceLocation source, RuleType type, RuleDialect dialect,
        List<String> exclude, boolean optional, List<SourceLocation> fallback) {

    public InputSpec {
        exclude = List.copyOf(exclude);
        fallback = List.copyOf(fallback);
    }

    public InputSpec(String name, SourceLocation source, RuleType type, RuleDialect dialect) {
        this(name, source, type, dialect, List.of(), false, List.of());
    }

    public InputSpec(String name, SourceLocation source, RuleType type, RuleDialect dialect,
            List<String> exclude) {
        this(name, source, type, dialect, exclude, false, List.of());
    }

    public InputSpec(String name, SourceLocation source, RuleType type, RuleDialect dialect,
            List<String> exclude, boolean optional) {
        this(name, source, type, dialect, exclude, optional, List.of());
    }

    /** 按尝试顺序返回主地址与备用地址；没有备用地址时只有主地址一项。 */
    public List<SourceLocation> candidates() {
        if (fallback.isEmpty()) {
            return List.of(source);
        }
        List<SourceLocation> locations = new ArrayList<>(fallback.size() + 1);
        locations.add(source);
        locations.addAll(fallback);
        return List.copyOf(locations);
    }

    /** 同一声明换用另一个地址，用于主地址失败后尝试备用源。 */
    public InputSpec withSource(SourceLocation location) {
        return new InputSpec(name, location, type, dialect, exclude, optional, fallback);
    }

    public sealed interface SourceLocation permits HttpSource, LocalSource {
    }

    public record LocalSource(Path path) implements SourceLocation {
    }

    public record HttpSource(URI uri) implements SourceLocation {
    }
}
