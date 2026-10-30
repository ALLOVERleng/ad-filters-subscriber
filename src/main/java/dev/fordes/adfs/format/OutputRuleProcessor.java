package dev.fordes.adfs.format;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Predicate;

import dev.fordes.adfs.format.conversion.ProjectedRule;
import dev.fordes.adfs.rule.conversion.ConversionDecision;
import dev.fordes.adfs.rule.conversion.ConversionPolicy;
import dev.fordes.adfs.rule.dedup.OutputDeduplicator;
import dev.fordes.adfs.rule.model.ReservedHosts;
import dev.fordes.adfs.rule.model.RuleEntry;

/** 统一执行转换策略、白名单、语法校验、目标去重和写出顺序。 */
public final class OutputRuleProcessor<T> {

    /**
     * 单行渲染长度上限。
     *
     * <p>上游常把成百上千个域名合并进同一条规则（{@code $domain=a|b|c...}、逗号分隔的
     * {@code #$#} 域名前缀等），超长行在多数客户端上要么解析缓慢、要么被截断，且一旦失效
     * 会同时丢掉整批域名的保护。超过上限的规则按“无法安全表达”丢弃并计数。</p>
     */
    static final int MAX_RENDERED_LENGTH = 32768;

    private final ConversionPolicy policy;
    private final Predicate<RuleEntry> removeForWhitelist;
    private final SyntaxValidator syntax;
    private final OutputDeduplicator deduplicator;
    private final Consumer<T> sink;

    public OutputRuleProcessor(
            ConversionPolicy policy,
            OutputDeduplicator deduplicator,
            Consumer<T> sink) {
        this(policy, _ -> false, SyntaxValidator.acceptAll(), deduplicator, sink);
    }

    public OutputRuleProcessor(
            ConversionPolicy policy,
            Predicate<RuleEntry> removeForWhitelist,
            OutputDeduplicator deduplicator,
            Consumer<T> sink) {
        this(policy, removeForWhitelist, SyntaxValidator.acceptAll(), deduplicator, sink);
    }

    public OutputRuleProcessor(
            ConversionPolicy policy,
            Predicate<RuleEntry> removeForWhitelist,
            SyntaxValidator syntax,
            OutputDeduplicator deduplicator,
            Consumer<T> sink) {
        this.policy = policy;
        this.removeForWhitelist = removeForWhitelist;
        this.syntax = syntax;
        this.deduplicator = deduplicator;
        this.sink = sink;
    }

    public WriteResult process(ProjectedRule<T> projected) {
        ConversionDecision decision = projected.decision();
        if (!policy.allows(decision.scope())) {
            return WriteResult.UNSUPPORTED;
        }
        if (ReservedHosts.isReserved(projected.effectiveRule())) {
            return WriteResult.UNSUPPORTED;
        }
        if (removeForWhitelist.test(projected.effectiveRule())) {
            return WriteResult.WHITELIST_REMOVED;
        }
        if (projected.encoded() instanceof String text && text.length() > MAX_RENDERED_LENGTH) {
            return WriteResult.UNSUPPORTED;
        }
        // 语法校验放在去重之前: 非法行不应该占用去重键, 否则同内容的合法行会被误判为重复。
        if (projected.encoded() instanceof String text && !syntaxApplies(projected) && !syntax.isValid(text)) {
            return WriteResult.INVALID_SYNTAX;
        }
        // 去重键取“最终渲染文本”: 两条不同的规范化规则可能渲染成同一行(白名单补写、精确域名与
        // 后缀域名落到同一 dnsmasq/smartdns 语法等)。顺序敏感规则也只有在渲染文本逐字相同时才
        // 会被合并, 而逐字相同即语义相同, 不会破坏先后关系。
        if (!deduplicator.add(dedupKey(projected))) {
            return WriteResult.DUPLICATE;
        }
        sink.accept(projected.encoded());
        return projected.passthrough() ? WriteResult.PASSTHROUGH : WriteResult.WRITTEN;
    }

    private boolean syntaxApplies(ProjectedRule<T> projected) {
        return projected.passthrough() && !syntax.validatesPassthrough();
    }

    private static byte[] dedupKey(ProjectedRule<?> projected) {
        return projected.encoded() instanceof String text
                ? text.getBytes(StandardCharsets.UTF_8)
                : projected.dedupKey();
    }

}
