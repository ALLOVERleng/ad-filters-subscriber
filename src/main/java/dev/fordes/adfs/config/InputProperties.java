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

    public InputProperties(@Parameter int index) {
        this.index = index;
    }

}
