package dev.fordes.adfs.report;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import dev.fordes.adfs.application.ProcessingResult;
import dev.fordes.adfs.config.OutputSpec;
import dev.fordes.adfs.format.WriteResult;
import dev.fordes.adfs.rule.model.OpaqueRule;
import dev.fordes.adfs.rule.model.Rule;
import dev.fordes.adfs.rule.model.RuleEntry;
import dev.fordes.adfs.rule.model.SafariRule;

public final class ProcessingMetrics {

    private final long startedNanos = System.nanoTime();
    private final long inputs;
    private final Map<ProcessingStage, Long> stages = new java.util.EnumMap<>(ProcessingStage.class);
    private final Map<String, InputMetrics> sources = new LinkedHashMap<>();
    private final Map<Path, MutableOutputMetrics> outputs = new LinkedHashMap<>();
    private long semanticRules;
    private long opaqueRules;
    private long uniqueRules;
    private long duplicateRules;
    private long allowOverridden;
    private long dnsChecked;
    private long dnsValid;
    private long dnsInvalid;
    private long dnsFailed;
    private long dnsSkipped;
    private long dnsMerged;
    private int dnsCacheEntries;
    private int hashEntries;
    private int hashCapacity;
    private int hashCollisions;

    public ProcessingMetrics(long inputs) {
        this.inputs = inputs;
    }

    public void register(OutputSpec output) {
        outputs.put(output.path(), new MutableOutputMetrics());
    }

    public void parsed(RuleEntry entry) {
        switch (entry) {
            case SafariRule safari -> parsed(safari.rule());
            case Rule _ -> semanticRules++;
            case OpaqueRule _ -> opaqueRules++;
        }
    }

    public void stageFinished(ProcessingStage stage, long started) {
        stages.put(stage, (System.nanoTime() - started) / 1_000_000);
    }

    public void outputBytes(Path path, long bytes) {
        outputs.get(path).bytes(bytes);
    }

    public long parsedCount() {
        return semanticRules + opaqueRules;
    }

    public void inputFinished(String name, InputMetrics metrics) {
        sources.put(name, metrics);
    }

    public void finished(OutputSpec output, dev.fordes.adfs.format.FinishResult result) {
        outputs.get(output.path()).finish(result);
    }

    public void unique() {
        uniqueRules++;
    }

    public void duplicate() {
        duplicateRules++;
    }

    /** 同目标已有放行规则、因而被剔除的阻断规则条数。 */
    public void allowOverridden() {
        allowOverridden++;
    }

    public void finishHashTable(int entries, int capacity, int collisions) {
        hashEntries = entries;
        hashCapacity = capacity;
        hashCollisions = collisions;
    }

    public void wrote(OutputSpec output, WriteResult result) {
        outputs.get(output.path()).add(result);
    }

    /** 产物级校验在发布前剔除的行数, 与写出阶段的 {@code INVALID_SYNTAX} 合并统计。 */
    public void invalidLines(OutputSpec output, long count) {
        if (count > 0) {
            outputs.get(output.path()).invalidLines(count);
        }
    }

    public void dnsChecked() {
        dnsChecked++;
    }

    public void dnsValid() {
        dnsValid++;
    }

    public void dnsInvalid() {
        dnsInvalid++;
    }

    public void dnsFailed() {
        dnsFailed++;
    }

    public void dnsSkipped() {
        dnsSkipped++;
    }

    public void dnsMerged() {
        dnsMerged++;
    }

    public void finishDns(int cacheEntries) {
        dnsCacheEntries = cacheEntries;
    }

    public ProcessingResult snapshot() {
        Map<Path, OutputMetrics> snapshots = new LinkedHashMap<>();
        outputs.forEach((path, metrics) -> snapshots.put(path, metrics.snapshot()));
        long elapsedMillis = (System.nanoTime() - startedNanos) / 1_000_000;
        DnsMetrics dns = new DnsMetrics(dnsChecked, dnsValid, dnsInvalid, dnsFailed, dnsSkipped, dnsMerged,
                dnsCacheEntries);
        return new ProcessingResult(inputs, semanticRules, opaqueRules, uniqueRules, duplicateRules, allowOverridden,
                hashEntries, hashCapacity, hashCollisions, dns,
                snapshots, elapsedMillis, sources, stages);
    }
}

final class MutableOutputMetrics {

    private long written;
    private long passthrough;
    private long duplicates;
    private long whitelistRemoved;
    private long unsupported;
    private long filtered;
    private long invalidSyntax;
    private long whitelistAdded;
    private long bytes;

    void bytes(long count) {
        bytes = count;
    }

    void finish(dev.fordes.adfs.format.FinishResult result) {
        whitelistAdded += result.whitelistAdded();
        duplicates += result.duplicates();
    }

    void invalidLines(long count) {
        invalidSyntax += count;
    }

    void add(WriteResult result) {
        switch (result) {
            case WRITTEN -> written++;
            case PASSTHROUGH -> passthrough++;
            case DUPLICATE -> duplicates++;
            case WHITELIST_REMOVED -> whitelistRemoved++;
            case UNSUPPORTED -> unsupported++;
            case FILTERED -> filtered++;
            case INVALID_SYNTAX -> invalidSyntax++;
        }
    }

    OutputMetrics snapshot() {
        return new OutputMetrics(written, passthrough, duplicates, whitelistRemoved, unsupported, filtered,
                invalidSyntax, whitelistAdded, bytes);
    }
}
