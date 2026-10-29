package dev.fordes.adfs.format;

public enum WriteResult {
    WRITTEN,
    PASSTHROUGH,
    DUPLICATE,
    WHITELIST_REMOVED,
    UNSUPPORTED,
    /** 按输出声明的 {@code exclude} 类别主动排除，非转换失败。 */
    FILTERED
}
