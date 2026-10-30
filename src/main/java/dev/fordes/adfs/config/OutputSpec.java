package dev.fordes.adfs.config;

import java.nio.file.Path;
import java.util.Set;

public record OutputSpec(
        Path path,
        RuleType type,
        RuleDialect dialect,
        ContainerFormat container,
        String fileHeader,
        Set<RuleCategory> exclude,
        RuleShape shape) {

    public OutputSpec {
        exclude = Set.copyOf(exclude);
    }

    public OutputSpec(Path path, RuleType type, RuleDialect dialect, ContainerFormat container, String fileHeader,
            Set<RuleCategory> exclude) {
        this(path, type, dialect, container, fileHeader, exclude, RuleShape.ALL);
    }

    public OutputSpec(Path path, RuleType type, RuleDialect dialect, ContainerFormat container, String fileHeader) {
        this(path, type, dialect, container, fileHeader, Set.of(), RuleShape.ALL);
    }
}
