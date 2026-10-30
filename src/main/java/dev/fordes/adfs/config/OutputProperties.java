package dev.fordes.adfs.config;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.validation.Validated;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Validated
@EachProperty(value = "adfs.output", list = true)
public final class OutputProperties {

    private final int index;

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    private String dialect;
    private String container;
    private String fileHeader;

    /** 需要从该输出中排除的规则类别，例如 {@code [dns-domain]}。缺省为空表示全部保留。 */
    private List<String> exclude = List.of();

    /**
     * 是否把该输出拆成“元素规则”和“URL 规则”两个文件。
     *
     * <p>未设置时按格式决定：adblock 文本输出<b>默认拆分</b>，文件名分别在扩展名前插入
     * {@code .element} 与 {@code .url}，例如 {@code easylist.txt} 会生成
     * {@code easylist.element.txt} 与 {@code easylist.url.txt}；其它格式保持单一文件。
     * 显式 {@code split: false} 可退回单文件（仅 adblock 文本输出的拆分有意义，其它格式
     * 显式开启会报错）。</p>
     */
    private Boolean split;

    public OutputProperties(@Parameter int index) {
        this.index = index;
    }

    public void setFileHeader(@Nullable String fileHeader) {
        this.fileHeader = fileHeader;
    }

}
