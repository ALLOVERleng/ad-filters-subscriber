package dev.fordes.adfs.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.validation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties("adfs.config.rules")
public final class RuleProperties {

    @Min(1)
    @Max(1_048_576)
    private int minLength = 1;

    @Min(1)
    @Max(1_048_576)
    private int maxLength = 65_536;

    @NotNull
    private List<String> whitelist = List.of();

    /**
     * 同一目标同时出现放行（{@code @@}）与阻断规则时，是否保留放行规则并剔除阻断规则。
     *
     * <p>放行规则在客户端里优先级更高，同目标的阻断规则实际不会生效，属于“无效规则”，
     * 保留它只会让产物虚高。<b>默认开启</b>，即默认保留白名单规则。</p>
     *
     * <p>目标按域名归一：adblock 的 {@code ||x^}、DNS 的 {@code |x|}、hosts 的
     * {@code 0.0.0.0 x}、dnsmasq/smartdns 的地址赋值、smartdns 的 {@code address /x/-}
     * 都算同一个目标，因此黑白名单可以来自不同格式的不同上游。不做父域覆盖，父域级别的
     * 放行由 {@code rules.whitelist} 负责。</p>
     */
    private boolean allowOverridesBlock = true;

}
