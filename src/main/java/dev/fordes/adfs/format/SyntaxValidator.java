package dev.fordes.adfs.format;

/** 输出行语法校验：返回 {@code false} 的行在落盘前被丢弃，不计入去重结果。 */
@FunctionalInterface
public interface SyntaxValidator {

    boolean isValid(String line);

    /**
     * 是否也校验透传规则。
     *
     * <p>透传规则来自同格式输入，可能包含我们并不解析的指令（例如 SmartDNS 的其他配置项），
     * 用通用检查器判断容易误杀，因此默认只校验自己渲染出的行；Adblock 语法边界清晰，
     * 透传行同样参与校验。</p>
     */
    default boolean validatesPassthrough() {
        return false;
    }

    static SyntaxValidator acceptAll() {
        return _ -> true;
    }
}
