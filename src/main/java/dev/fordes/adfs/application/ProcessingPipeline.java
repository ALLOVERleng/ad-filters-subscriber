package dev.fordes.adfs.application;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import dev.fordes.adfs.format.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import jakarta.inject.Singleton;

import dev.fordes.adfs.config.EffectiveConfig;
import dev.fordes.adfs.config.InputSpec;
import dev.fordes.adfs.config.InputSpec.HttpSource;
import dev.fordes.adfs.config.InputSpec.LocalSource;
import dev.fordes.adfs.config.InputSpec.SourceLocation;
import dev.fordes.adfs.config.OutputSpec;
import dev.fordes.adfs.config.RuleDialect;
import dev.fordes.adfs.error.InputException;
import dev.fordes.adfs.format.adblock.DisableIndex;
import dev.fordes.adfs.format.validate.ArtifactValidator;
import dev.fordes.adfs.publish.OutputPublisher;
import dev.fordes.adfs.publish.OutputHeader;
import dev.fordes.adfs.publish.PublishManifest;
import dev.fordes.adfs.publish.StagingWorkspace.Workspace;
import dev.fordes.adfs.publish.StagingWorkspace;
import dev.fordes.adfs.report.ProcessingMetrics;
import dev.fordes.adfs.report.ProcessingStage;
import dev.fordes.adfs.rule.conversion.AllowIndex;
import dev.fordes.adfs.rule.dedup.CanonicalStore;
import dev.fordes.adfs.rule.dedup.RuleDeduplicator;
import dev.fordes.adfs.rule.filter.SourceExclusion;
import dev.fordes.adfs.rule.model.RuleEntry;
import dev.fordes.adfs.rule.normalize.RuleNormalizer;
import dev.fordes.adfs.rule.spool.RuleSpool;
import dev.fordes.adfs.source.SourceReader;
import dev.fordes.adfs.source.SourceSession;
import dev.fordes.adfs.validation.dns.DnsValidator;

@Singleton
@RequiredArgsConstructor
@Slf4j
public final class ProcessingPipeline {

    private static final String SPOOL_NAME = "rules.bin";
    private static final String CANONICAL_NAME = "canonical.bin";
    private static final String DISABLE_NAME = "disabled.bin";
    private final FormatRegistry formats;
    private final List<SourceReader> sourceReaders;
    private final StagingWorkspace stagingWorkspace;
    private final OutputPublisher publisher;
    private final DnsValidator dnsValidator;

    public ProcessingResult process(EffectiveConfig config) {
        OffsetDateTime generatedAt = OffsetDateTime.now();
        formats.validateSupported(config);
        log.info("开始处理, 输入源 {} 个, 输出目标 {} 个, DNS 检测{}",
                config.inputs().size(), config.outputs().size(), config.dns().enabled() ? "已启用" : "未启用");
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        ProcessingMetrics metrics = new ProcessingMetrics(config.inputs().size());
        config.outputs().forEach(metrics::register);
        try (Workspace workspace = stagingWorkspace.open(config.outputDir())) {
            Path spoolPath = workspace.runDir().resolve(SPOOL_NAME);
            try (RuleSpool spool = new RuleSpool(spoolPath, config.rules().maxLength())) {
                long inputStarted = System.nanoTime();
                parseInputs(config, workspace.runDir(), spool, metrics);
                spool.finishWriting();
                metrics.stageFinished(ProcessingStage.INPUT, inputStarted);
                log.info("输入解析完成, 开始去重、{}规则转换", config.dns().enabled() ? "DNS 检测及" : "");
                long processingStarted = System.nanoTime();
                replay(config, workspace, spool, metrics, generatedAt);
                metrics.stageFinished(ProcessingStage.PROCESSING, processingStarted);
            }
            log.info("规则处理完成, 开始校验并发布产物");
            long manifestStarted = System.nanoTime();
            PublishManifest manifest = PublishManifest.create(workspace, config.outputs());
            manifest.entries().forEach(entry -> metrics.outputBytes(entry.path(), entry.size()));
            metrics.stageFinished(ProcessingStage.MANIFEST, manifestStarted);
            long publishStarted = System.nanoTime();
            publisher.publish(workspace, manifest);
            metrics.stageFinished(ProcessingStage.PUBLISH, publishStarted);
            log.info("产物发布完成, 输出目录为「{}」", config.outputDir());
        } finally {
            if (previousContext == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(previousContext);
            }
        }
        return metrics.snapshot();
    }

    private void parseInputs(EffectiveConfig config, Path runDir, RuleSpool spool, ProcessingMetrics metrics) {
        int inputIndex = 0;
        for (InputSpec input : config.inputs()) {
            long started = System.nanoTime();
            log.debug("开始解析输入源「{}」", input.name());
            String format = input.type().value();
            if (input.dialect() != RuleDialect.NONE) {
                format += "/" + input.dialect().value();
            }
            MDC.put(RuleSpool.INPUT, input.name() + " (" + format + ")");
            MDC.remove(RuleSpool.INPUT_RULE);
            SourceReader sourceReader = sourceReaders.stream()
                    .filter(reader -> reader.supports(input))
                    .findFirst()
                    .orElseThrow(() -> new InputException("没有可用的来源读取器: " + input.name()));
            RuleParser parser = formats.createParser(input, config);
            SourceExclusion exclusion = SourceExclusion.of(input.exclude());
            long[] excluded = new long[1];
            long parsedBefore = metrics.parsedCount();
            Path inputSpoolPath = runDir.resolve("input-" + inputIndex + ".bin");
            try (RuleSpool inputSpool = new RuleSpool(inputSpoolPath, config.rules().maxLength());
                    SourceSession session = open(sourceReader, input, config)) {
                ParseResult result = parser.parse(session, entry -> {
                    RuleEntry normalized = RuleNormalizer.normalize(entry);
                    // 源级排除在落盘之前执行: 被排除的规则不参与去重、不进入任何产物,
                    // 也不需要下游 Writer 再判断一次。
                    if (exclusion.excludes(normalized)) {
                        excluded[0]++;
                        return;
                    }
                    inputSpool.accept(normalized);
                });
                inputSpool.finishWriting();
                if (result == ParseResult.COMPLETE) {
                    inputSpool.replay(entry -> {
                        metrics.parsed(entry);
                        spool.accept(entry);
                    });
                }
                metrics.inputFinished(input.name(), session.metrics(metrics.parsedCount() - parsedBefore));
            } catch (InputException exception) {
                if (!input.optional()) {
                    throw exception;
                }
                // 可选来源: 上游偶发 404/5xx 时跳过该源, 其余来源照常产出, 避免整批产物停止更新。
                metrics.inputSkipped(input.name(), exception.getMessage());
                log.warn("可选输入源「{}」读取失败, 已跳过: {}", input.name(), exception.getMessage());
                inputIndex++;
                continue;
            }
            if (excluded[0] > 0) {
                log.info("输入源「{}」按 exclude 丢弃 {} 条规则", input.name(), excluded[0]);
            }
            log.info("输入源「{}」解析完成, 耗时 {} ms", input.name(), (System.nanoTime() - started) / 1_000_000);
            inputIndex++;
        }
    }

    /**
     * 打开输入源：主地址不可用时按声明顺序尝试备用地址。
     *
     * <p>只对“打不开”这一阶段做切换。HTTP 请求与状态码判定都在 {@code open} 内完成，
     * 而解析阶段的语法错误不会触发备用源——同一份坏数据重读一遍不会变好，只会让日志翻倍。</p>
     */
    private static SourceSession open(SourceReader reader, InputSpec input, EffectiveConfig config) {
        List<SourceLocation> candidates = input.candidates();
        InputException lastFailure = null;
        for (int index = 0; index < candidates.size(); index++) {
            SourceLocation location = candidates.get(index);
            try {
                SourceSession session = reader.open(input.withSource(location), config);
                if (index > 0) {
                    log.warn("输入源「{}」已切换到备用地址: {}", input.name(), describe(location));
                }
                return session;
            } catch (InputException exception) {
                lastFailure = exception;
                if (index + 1 < candidates.size()) {
                    log.warn("输入源「{}」地址不可用, 改试备用地址 {}: {}", input.name(),
                            describe(candidates.get(index + 1)), exception.getMessage());
                }
            }
        }
        if (lastFailure == null) {
            throw new InputException("输入源没有可用地址: " + input.name());
        }
        if (candidates.size() == 1) {
            throw lastFailure;
        }
        throw new InputException("输入源「" + input.name() + "」的 " + candidates.size()
                + " 个地址均不可用: " + lastFailure.getMessage(), lastFailure);
    }

    private static String describe(SourceLocation location) {
        return switch (location) {
            case HttpSource http -> http.uri().toString();
            case LocalSource local -> local.path().toString();
        };
    }

    private void replay(
            EffectiveConfig config,
            Workspace workspace,
            RuleSpool spool,
            ProcessingMetrics metrics,
            OffsetDateTime generatedAt) {
        try (CanonicalStore store = new CanonicalStore(workspace.runDir().resolve(CANONICAL_NAME));
                DisableIndex disabled = createDisableIndex(workspace, spool);
                OutputSet outputs = createOutputs(config, workspace)) {
            RuleDeduplicator deduplicator = new RuleDeduplicator(store);
            AllowIndex allows = createAllowIndex(spool, config.rules().allowOverridesBlock());
            if (config.dns().enabled()) {
                dnsValidator.validate(spool, config.dns(), deduplicator,
                        entry -> enabled(entry, disabled, allows, metrics),
                        entry -> distributeUnique(entry, outputs, metrics), metrics);
            } else {
                spool.replay(entry -> {
                    if (enabled(entry, disabled, allows, metrics)) {
                        distribute(entry, deduplicator, outputs, metrics);
                    }
                });
            }
            metrics.finishHashTable(deduplicator.size(), deduplicator.capacity(), deduplicator.collisions());
            outputs.targets().forEach(output -> metrics.finished(output.spec(), output.finish()));
            for (OutputTarget output : outputs.targets()) {
                output.close();
                ArtifactValidator.Result validated = ArtifactValidator.validate(
                        workspace.nextDir().resolve(output.spec().path()), output.spec());
                metrics.invalidLines(output.spec(), validated.dropped());
                if (validated.dropped() > 0) {
                    log.warn("产物「{}」有 {} 行不符合 {} 语法, 已在发布前剔除",
                            output.spec().path(), validated.dropped(), output.spec().type().value());
                }
                OutputHeader.prepend(workspace, output.spec(), generatedAt,
                        Math.max(0, output.ruleCount() - (int) validated.dropped()));
            }
        }
    }

    private static DisableIndex createDisableIndex(Workspace workspace, RuleSpool spool) {
        DisableIndex disabled = new DisableIndex(workspace.runDir().resolve(DISABLE_NAME));
        try {
            spool.replay(entry -> {
                if (disabled.isControl(entry)) {
                    disabled.disable(entry);
                }
            });
            return disabled;
        } catch (RuntimeException exception) {
            try {
                disabled.close();
            } catch (RuntimeException closeFailure) {
                exception.addSuppressed(closeFailure);
            }
            throw exception;
        }
    }

    private static AllowIndex createAllowIndex(RuleSpool spool, boolean allowOverridesBlock) {
        AllowIndex index = new AllowIndex();
        if (allowOverridesBlock) {
            spool.replay(index::accept);
        }
        return index;
    }

    private static boolean enabled(
            RuleEntry entry, DisableIndex disabled, AllowIndex allows, ProcessingMetrics metrics) {
        if (disabled.isControl(entry) || disabled.isDisabled(entry)) {
            return false;
        }
        // 放行规则优先级更高, 同目标的阻断规则不会生效, 直接剔除以免产物虚高。
        if (allows.overrides(entry)) {
            metrics.allowOverridden();
            log.debug("阻断规则被同目标白名单覆盖, 已跳过:  {} --> {}",
                    MDC.get(RuleSpool.INPUT), MDC.get(RuleSpool.INPUT_RULE));
            return false;
        }
        return true;
    }

    private OutputSet createOutputs(EffectiveConfig config, Workspace workspace) {
        OutputSet outputs = new OutputSet();
        int index = 0;
        try {
            for (OutputSpec spec : config.outputs()) {
                outputs.add(formats.createWriter(spec, config, workspace.nextDir(), workspace.runDir(), index));
                index++;
            }
            return outputs;
        } catch (RuntimeException exception) {
            try {
                outputs.close();
            } catch (RuntimeException closeFailure) {
                exception.addSuppressed(closeFailure);
            }
            throw exception;
        }
    }

    private static void distribute(
            RuleEntry entry,
            RuleDeduplicator deduplicator,
            OutputSet outputs,
            ProcessingMetrics metrics) {
        if (!deduplicator.add(entry)) {
            metrics.duplicate();
            log.debug("重复规则, 已跳过:  {} --> 规则去重 | {}",
                    MDC.get(RuleSpool.INPUT), MDC.get(RuleSpool.INPUT_RULE));
            return;
        }
        metrics.unique();
        distributeUnique(entry, outputs, metrics);
    }

    private static void distributeUnique(RuleEntry entry, OutputSet outputs, ProcessingMetrics metrics) {
        for (OutputTarget output : outputs.targets()) {
            WriteResult result = output.write(entry);
            metrics.wrote(output.spec(), result);
        }
    }
}
