package dev.fordes.adfs.format;

public enum WriteResult {
    WRITTEN,
    PASSTHROUGH,
    DUPLICATE,
    WHITELIST_REMOVED,
    UNSUPPORTED,
    /** 按输出声明的 {@code exclude} 类别或 {@code split} 形状主动排除，非转换失败。 */
    FILTERED,
    /** 渲染结果不符合目标格式的语法，已被丢弃。 */
    INVALID_SYNTAX
}
