package dev.fordes.adfs.config;

import java.util.List;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.validation.Validated;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@EachProperty(value = "adfs.input", list = true)
public final class InputProperties {

    private final int index;

    @NotBlank
    private String name;

    @NotBlank
    private String path;

    @NotBlank
    private String type;

    private String dialect;

    /**
     * 源级排除：命中的规则在进入处理管线前丢弃，用于隔离上游误报（例如某个列表把
     * 公共基础设施整域拦截）。支持域名（含子域）、{@code *.域名}（仅子域）与关键词。
     */
    private List<String> exclude = List.of();

    /**
     * 可选输入: 读取失败时记录告警并跳过该来源, 不终止整次构建。
     *
     * <p>上游偶发 404/5xx 时, 若把整批产物绑定在单个来源上会导致一次抖动就让所有产物停止更新,
     * 对这类"锦上添花"的列表开启本开关更稳妥。默认关闭, 保持严格失败语义。</p>
     */
    private boolean optional = false;

    public InputProperties(@Parameter int index) {
        this.index = index;
    }

}
