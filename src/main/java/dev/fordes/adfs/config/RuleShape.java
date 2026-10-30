package dev.fordes.adfs.config;

import java.util.Locale;

/**
 * 输出文件的规则形状。
 *
 * <p>元素/外观类规则（{@code ##}、{@code #@#}、{@code #$#} 等）与 URL/网络类规则混在一个文件里时，
 * 订阅方无法按需取用，也不便于单独做体积与语法校验。声明 {@code split: true} 的输出会按形状拆成
 * 两个文件，分别对应本枚举的 {@link #ELEMENT} 与 {@link #URL}。</p>
 */
public enum RuleShape {

    /** 不限制形状，元素规则与 URL 规则写在同一文件。 */
    ALL("all"),

    /** 只保留元素隐藏、样式注入等外观类规则。 */
    ELEMENT("element"),

    /** 只保留 URL/网络类规则（含域名、IP、路由规则）。 */
    URL("url");

    private final String value;

    RuleShape(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static RuleShape parse(String value) {
        String normalized = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        for (RuleShape shape : values()) {
            if (shape.value.equals(normalized)) {
                return shape;
            }
        }
        throw new IllegalArgumentException("未知的输出形状: " + value);
    }
}
