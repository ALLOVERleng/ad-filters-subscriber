package dev.fordes.adfs.format.validate;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import dev.fordes.adfs.config.OutputSpec;
import dev.fordes.adfs.error.RuleProcessingException;
import dev.fordes.adfs.format.SyntaxValidator;
import dev.fordes.adfs.format.adblock.AdblockSyntax;
import dev.fordes.adfs.rule.model.IpAddress;

/**
 * 按输出格式提供的行级语法校验。
 *
 * <p>校验目标不是“完整解析”，而是拦住写错格式或明显残缺的行：例如把 hosts 行写进 adblock 文件、
 * 域名段为空、选项列表残缺、正则未闭合等。所有检查都以本仓库 Writer 的实际渲染形态为准，
 * 避免误杀合法规则。</p>
 */
public final class OutputSyntax {

    private static final Pattern CONTROL = Pattern.compile("\\p{Cntrl}");
    private static final Pattern HOST = Pattern.compile("(?i)[a-z0-9_*]([a-z0-9_*-]*[a-z0-9_*])?"
            + "(\\.[a-z0-9_*]([a-z0-9_*-]*[a-z0-9_*])?)*");
    private static final Pattern CIDR = Pattern.compile("(?i)[0-9a-f:.]+/\\d{1,3}");
    private static final Pattern MIHOMO_CLASSICAL = Pattern.compile("[A-Z][A-Z0-9-]*,\\S.*");
    private static final Pattern MIHOMO_DOMAIN = Pattern.compile("[+.]?[A-Za-z0-9*._-]+");
    private static final Pattern DNS_RESPONSE = Pattern.compile("#[46]?|-[46]?");

    private OutputSyntax() {
    }

    public static SyntaxValidator of(OutputSpec spec) {
        return switch (spec.type()) {
            case ADBLOCK -> adblock();
            case DNS -> OutputSyntax::dns;
            case HOSTS -> OutputSyntax::hosts;
            case DNSMASQ -> OutputSyntax::dnsmasq;
            case SMARTDNS -> OutputSyntax::smartdns;
            case MIHOMO -> switch (spec.dialect()) {
                case CLASSICAL -> OutputSyntax::mihomoClassical;
                case IPCIDR -> OutputSyntax::mihomoIpCidr;
                default -> OutputSyntax::mihomoDomain;
            };
            // JSON 由 JsonGenerator 结构化生成，逐行校验没有意义。
            case SING_BOX -> SyntaxValidator.acceptAll();
        };
    }

    private static SyntaxValidator adblock() {
        return new SyntaxValidator() {
            @Override
            public boolean isValid(String line) {
                return adblockLine(line);
            }

            @Override
            public boolean validatesPassthrough() {
                return true;
            }
        };
    }

    static boolean adblockLine(String line) {
        if (blankOrControl(line)) {
            return false;
        }
        AdblockSyntax.CosmeticLocation cosmetic = AdblockSyntax.cosmeticOperator(line);
        if (cosmetic != null) {
            String body = line.substring(cosmetic.index() + cosmetic.operator().length()).strip();
            return !body.isEmpty() && domainsValid(line.substring(0, cosmetic.index()));
        }
        int separator = AdblockSyntax.optionSeparator(line);
        if (separator >= 0 && line.substring(separator + 1).isBlank()) {
            return false;
        }
        String head = separator < 0 ? line : line.substring(0, separator);
        String pattern = (head.startsWith("@@") ? head.substring(2) : head).strip();
        if (pattern.isEmpty()) {
            return false;
        }
        // 其他格式的行（hosts / dnsmasq / smartdns）不应混进 adblock 产物。
        if (pattern.startsWith("address=/") || pattern.startsWith("server=/") || pattern.startsWith("address /")) {
            return false;
        }
        String[] mapping = pattern.split("\\s+");
        if (mapping.length == 2 && ip(mapping[0]) && host(mapping[1])) {
            return false;
        }
        if (pattern.startsWith("/")) {
            if (pattern.length() < 3 || !pattern.endsWith("/")) {
                return false;
            }
            try {
                Pattern.compile(pattern.substring(1, pattern.length() - 1));
            } catch (PatternSyntaxException exception) {
                return false;
            }
        } else if (pattern.startsWith("||")) {
            String host = pattern.substring(2);
            if (host.isEmpty() || host.charAt(0) == '^' || host.charAt(0) == '/') {
                return false;
            }
        }
        if (separator >= 0) {
            try {
                AdblockSyntax.options(line.substring(separator + 1));
            } catch (RuleProcessingException exception) {
                return false;
            }
        }
        return true;
    }

    static boolean domainsValid(String prefix) {
        String value = prefix.strip();
        if (value.isEmpty() || value.startsWith("[")) {
            return true;
        }
        for (String token : value.split(",", -1)) {
            String domain = token.strip();
            if (domain.startsWith("~")) {
                domain = domain.substring(1);
            }
            if (domain.isEmpty() || !HOST.matcher(domain).matches()) {
                return false;
            }
        }
        return true;
    }

    static boolean dns(String line) {
        if (blankOrControl(line)) {
            return false;
        }
        String rule = line.startsWith("@@") ? line.substring(2) : line;
        int separator = rule.indexOf('$');
        if (separator >= 0) {
            if (!rule.substring(separator + 1).equals("important")) {
                return false;
            }
            rule = rule.substring(0, separator);
        }
        rule = rule.strip();
        if (rule.startsWith("||") && rule.endsWith("^")) {
            return host(rule.substring(2, rule.length() - 1));
        }
        return hosts(rule);
    }

    static boolean hosts(String line) {
        if (blankOrControl(line)) {
            return false;
        }
        String[] parts = line.strip().split("\\s+");
        return parts.length == 2 && ip(parts[0]) && host(parts[1]);
    }

    static boolean dnsmasq(String line) {
        if (blankOrControl(line)) {
            return false;
        }
        String prefix = line.startsWith("address=/") ? "address=/"
                : line.startsWith("server=/") ? "server=/" : "";
        if (prefix.isEmpty()) {
            return false;
        }
        int last = line.lastIndexOf('/');
        if (last <= prefix.length()) {
            return false;
        }
        return host(line.substring(prefix.length(), last)) && dnsValue(line.substring(last + 1));
    }

    static boolean smartdns(String line) {
        if (blankOrControl(line)) {
            return false;
        }
        String prefix = "address /";
        if (!line.startsWith(prefix)) {
            return false;
        }
        int last = line.lastIndexOf('/');
        if (last <= prefix.length()) {
            return false;
        }
        return selector(line.substring(prefix.length(), last)) && dnsValue(line.substring(last + 1));
    }

    static boolean mihomoClassical(String line) {
        return !blankOrControl(line) && MIHOMO_CLASSICAL.matcher(line).matches();
    }

    static boolean mihomoDomain(String line) {
        return !blankOrControl(line) && MIHOMO_DOMAIN.matcher(line).matches();
    }

    static boolean mihomoIpCidr(String line) {
        return !blankOrControl(line) && CIDR.matcher(line).matches();
    }

    private static boolean dnsValue(String value) {
        if (value.isEmpty()) {
            return false;
        }
        if (DNS_RESPONSE.matcher(value).matches()) {
            return true;
        }
        for (String part : value.split(",", -1)) {
            if (!ip(part)) {
                return false;
            }
        }
        return true;
    }

    private static boolean selector(String value) {
        String candidate = value.startsWith("-.") || value.startsWith("*.") ? value.substring(2) : value;
        return host(candidate);
    }

    private static boolean host(String value) {
        return !value.isEmpty() && HOST.matcher(value).matches();
    }

    private static boolean ip(String value) {
        try {
            IpAddress.parse(value);
            return true;
        } catch (RuleProcessingException exception) {
            return false;
        }
    }

    private static boolean blankOrControl(String line) {
        return line == null || line.isBlank() || CONTROL.matcher(line).find();
    }
}
