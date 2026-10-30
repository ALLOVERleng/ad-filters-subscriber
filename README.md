# Ad Filters Subscriber · 规则订阅

![Last Update](https://img.shields.io/github/last-commit/ALLOVERleng/ad-filters-subscriber?style=flat-square&branch=release)
![Build Status](https://img.shields.io/github/actions/workflow/status/ALLOVERleng/ad-filters-subscriber/auto-update.yml?branch=main&style=flat-square)
![Stars](https://img.shields.io/github/stars/ALLOVERleng/ad-filters-subscriber?style=flat-square)
![Forks](https://img.shields.io/github/forks/ALLOVERleng/ad-filters-subscriber?style=flat-square)

本仓库是 [fordes123/ad-filters-subscriber](https://github.com/fordes123/ad-filters-subscriber) 的一个**规则订阅实例**：
GitHub Actions 定时拉取 36 个外部上游列表，经归一化、去重、格式转换与发布前语法校验后，
产出 12 个可直接订阅的规则文件，覆盖 AdGuard、uBlock Origin、Adblock Plus、AdGuard DNS、hosts、
dnsmasq、SmartDNS、Mihomo（Clash）与 sing-box。

> [!IMPORTANT]
> ### AI 参与声明
>
> 本仓库自 **2026-10-30** 起的规则管线改动，以及本 README 的重新编写，均由 **AI 编程助手在人工指令下生成、经人工复核后提交**。
>
> 规则**内容**不来自 AI：所有拦截条目仍完全由下方列出的上游列表提供，AI 只参与「如何解析、如何转换、如何校验、如何发布」这一层，
> 不会新增、臆造或改写任何拦截域名。如对改动存疑，请以 git 提交历史为准；使用本仓库规则的风险由使用者自行承担。

> [!TIP]
> 本仓库已合并多个上游并统一去重、转换，推荐**单独订阅**本仓库规则，
> 并**禁用拦截器内置的规则列表**，以获得更一致的覆盖与更少的重复规则。

> [!WARNING]
> Chrome、Edge 等浏览器已放弃 MV2，官方扩展对新规则的兼容性持续下降，
> 请确认当前拦截器是否正常工作；如遇异常，推荐改用 Firefox、Brave，或在 Firefox 上使用 uBlock Origin。

## 订阅地址

| 文件 | 说明 | GitHub | ghproxy | jsDelivr |
|------|:-----|:------:|:-------:|:--------:|
| `easylist.url.txt`     | AdGuard 规则（URL / 网络请求） | [link][easylist-url-raw] | [link][easylist-url-ghproxy] | [link][easylist-url-jsdelivr] |
| `easylist.element.txt` | AdGuard 规则（元素隐藏 / 样式）  | [link][easylist-element-raw] | [link][easylist-element-ghproxy] | [link][easylist-element-jsdelivr] |
| `ubo.url.txt`          | uBlock Origin 规则（URL / 网络请求） | [link][ubo-url-raw] | [link][ubo-url-ghproxy] | [link][ubo-url-jsdelivr] |
| `ubo.element.txt`      | uBlock Origin 规则（元素隐藏） | [link][ubo-element-raw] | [link][ubo-element-ghproxy] | [link][ubo-element-jsdelivr] |
| `abp.url.txt`          | Adblock Plus 规则（URL / 网络请求） | [link][abp-url-raw] | [link][abp-url-ghproxy] | [link][abp-url-jsdelivr] |
| `abp.element.txt`      | Adblock Plus 规则（元素隐藏） | [link][abp-element-raw] | [link][abp-element-ghproxy] | [link][abp-element-jsdelivr] |
| `dns.txt`              | AdGuard DNS 过滤规则 | [link][dns-raw] | [link][dns-ghproxy] | [link][dns-jsdelivr] |
| `hosts.txt`            | hosts 格式规则 | [link][hosts-raw] | [link][hosts-ghproxy] | [link][hosts-jsdelivr] |
| `dnsmasq.txt`          | dnsmasq 格式规则 | [link][dnsmasq-raw] | [link][dnsmasq-ghproxy] | [link][dnsmasq-jsdelivr] |
| `smartdns.txt`         | SmartDNS 格式规则 | [link][smartdns-raw] | [link][smartdns-ghproxy] | [link][smartdns-jsdelivr] |
| `clash.yaml`           | Mihomo 域名规则集（YAML） | [link][clash-raw] | [link][clash-ghproxy] | [link][clash-jsdelivr] |
| `sing-box.json`        | sing-box 规则集（JSON） | [link][sing-box-raw] | [link][sing-box-ghproxy] | [link][sing-box-jsdelivr] |

### 选择哪一个

AdGuard 客户端（Windows / macOS / Android / iOS 应用与浏览器扩展）本身支持完整的 Adblock 语法，
包括 URL 路径级拦截、元素隐藏与 `@@` 放行，因此**优先订阅 `easylist` 两份**，拦截粒度最细；
只有跑不了客户端、只能做域名级拦截的场景（AdGuard Home、路由器插件等）才退而使用 `dns.txt` / `hosts.txt`。

| 使用场景 | 推荐文件 | 说明 |
|:---------|:---------|:-----|
| **AdGuard 客户端**（桌面 / 移动端 / 浏览器扩展） | `easylist.url.txt` + `easylist.element.txt` | 客户端原生支持 URL 级匹配与元素隐藏，拦截最精确 |
| **uBlock Origin**（Firefox / Brave） | `ubo.url.txt` + `ubo.element.txt` | uBO 原生语法 |
| **Adblock Plus** | `abp.url.txt` + `abp.element.txt` | ABP 原生语法 |
| AdGuard Home（域名级拦截） | `dns.txt` | 只有域名维度，粒度比 `easylist` 粗 |
| Pi-hole / 路由器 hosts 方案 | `hosts.txt` | 域名级，兼容性最好 |
| dnsmasq | `dnsmasq.txt` | 原生 `address=/domain/` 语法 |
| SmartDNS | `smartdns.txt` | 原生 `address /domain/#` 语法 |
| Mihomo / Clash.Meta | `clash.yaml` | 域名规则集（YAML） |
| sing-box | `sing-box.json` | 规则集（JSON） |

> 同一台设备上装了 AdGuard / uBO 时，只需订阅对应的浏览器列表即可，**不必再叠加 `dns.txt`**：
> 域名级列表能拦的 URL 级列表也能拦，反过来则不成立，叠加只会增加重复规则。

## 本实例相对上游项目的差异

本仓库的过滤逻辑并不只是「拉取 + 拼接」，在转换层做了一批针对实际使用问题的修补：

| 改动 | 说明 |
|:-----|:-----|
| **黑白名单冲突** | 同一域名同时出现在黑名单与白名单时，**保留白名单、剔除阻断规则**（`allow-overrides-block`）。目标按域名归一，`\|\|x^`、`\|x\|`、`0.0.0.0 x`、`address /x/...` 视为同一目标，黑白名单来自不同格式的不同上游也能互相抵消。 |
| **白名单写出前减法** | 白名单域名不再「先写阻断再补 `@@`」，而是在渲染前删除阻断规则。hosts / dnsmasq / Mihomo / sing-box 本身无法表达放行，只有做减法才能与 AdGuard 侧结果一致。 |
| **源级排除 `exclude`** | 可为单个上游声明排除项（域名、`*.域名` 或关键词），用于隔离上游误报。当前用于 `1Hosts Lite` 对 GitHub 加速网关（`gh-proxy.com` / `gh-proxy.org`）的整域误杀。 |
| **发布前语法校验** | 产物落盘后再按目标格式复检一遍：AdGuard / DNS 严格校验并剔除非法行，hosts / dnsmasq / SmartDNS / Mihomo 只做空行、控制字符、超长行等通用检查，避免误杀合法的透传行。剔除条数计入运行报告。 |
| **元素 / URL 规则拆分** | adblock 文本输出默认拆成 `.element.txt` 与 `.url.txt` 两个文件：元素文件只放 cosmetic 规则，`@@` 放行只写入 URL 文件，订阅方可按需取用。 |
| **站点作用域收敛** | `$domain=` 这类「只在指定站点生效」的规则在 hosts / DNS / dnsmasq / SmartDNS / Mihomo / sing-box 上无法保真，**直接丢弃**。放大成整域规则会让阻断误伤、让放行变成整域放行（例如只对某站点生效的 `@@\|\|raw.githubusercontent.com^$domain=...` 曾被放大成整域放行，抵消了对该域名上万条恶意 URL 的拦截）。 |
| **浏览器产物裁剪** | `easylist` / `ubo` / `abp` 三个输出排除纯域名阻断（`exclude: [dns-domain]`），既避免和 DNS 产物重复，也避免超出 jsDelivr 的单文件体积上限。 |

## 生成方式

规则由 [ad-filters-subscriber](https://github.com/fordes123/ad-filters-subscriber) 生成，配置见 [`config/application.yaml`](config/application.yaml)：

```bash
./gradlew --no-daemon shadowJar
java -jar build/libs/adfs-*-all.jar -c config/application.yaml
```

GitHub Actions（`auto-update.yml`）按计划执行同样的流程，把产物提交到 `release` 分支；
`verify.yml` 用于校验产物完整性。仓库只保留生成器与配置，**不手工编辑产物**。

## 上游规则

<details>
<summary>点击查看 36 个外部上游列表</summary>

**AdGuard 官方过滤器**

- [AdGuard 恼人广告过滤器](https://raw.githubusercontent.com/AdguardTeam/FiltersRegistry/master/filters/filter_14_Annoyances/filter.txt)
- [AdGuard Cookie 通知过滤器](https://filters.adtidy.org/android/filters/18_optimized.txt)
- [AdGuard 弹窗过滤器](https://filters.adtidy.org/android/filters/19_optimized.txt)
- [AdGuard 移动端程序内横幅过滤器](https://filters.adtidy.org/android/filters/20_optimized.txt)
- [AdGuard 其他恼人广告过滤器](https://filters.adtidy.org/android/filters/21_optimized.txt)
- [AdGuard 小工具过滤器](https://filters.adtidy.org/android/filters/22_optimized.txt)
- [AdGuard 防跟踪保护过滤器](https://filters.adtidy.org/android/filters/3_optimized.txt)
- [ABP 反广告过滤（anti-cv）](https://easylist-downloads.adblockplus.org/abp-filters-anti-cv.txt)

**恶意域名 / 遥测 / 隐私**

- [Online Malicious URL Blocklist](https://urlhaus-filter.pages.dev/urlhaus-filter-ag-online.txt)
- [HyperADRules · ads](https://github.com/Lynricsy/HyperADRules/releases/latest/download/hyper_adrules_ads_adguard.txt)
- [HyperADRules · malware](https://github.com/Lynricsy/HyperADRules/releases/latest/download/hyper_adrules_malware_adguard.txt)
- [HyperADRules · allow](https://github.com/Lynricsy/HyperADRules/releases/latest/download/hyper_adrules_allow_adguard.txt)
- [Telemetry Lists - Aggregated](https://raw.githubusercontent.com/SystemJargon/filters/refs/heads/main/telemetry.txt)
- [Bypass Paywalls Clean filter](https://gitflic.ru/project/magnolia1234/bypass-paywalls-clean-filters/blob/raw?file=bpc-paywall-filter.txt)

**DNS / hosts 类**

- [1Hosts Lite](https://raw.githubusercontent.com/badmojr/1Hosts/master/Lite/adblock.txt)（已源级排除 `gh-proxy.com` / `gh-proxy.org`）
- [HaGeZi's Pro DNS Blocklist](https://raw.githubusercontent.com/hagezi/dns-blocklists/refs/heads/main/adblock/pro.txt)
- [DNS-Kuner 黑名单](https://raw.githubusercontent.com/Kuner-mw/DNS-Kuner/main/FilterRules/blacklist.txt)
- [DNS-Kuner 白名单](https://raw.githubusercontent.com/Kuner-mw/DNS-Kuner/main/FilterRules/allowlist.txt)
- [JiuYue_Rules](https://rules.vlo.cc/dns/dns.txt)
- [SM 广告丟那吗 hosts](https://raw.githubusercontent.com/2Gardon/SM-Ad-FuckU-hosts/refs/heads/master/SMAdHosts)（hosts 格式）
- [NEO DEV HOST](https://raw.githubusercontent.com/neodevpro/neodevhost/master/adblocker)

**中文社区规则**

- [晴雅广告拦截规则](https://raw.githubusercontent.com/rssvcn/qy-Ads-Rule/main/black.txt)
- [xndeye/web-ad-rule](https://raw.githubusercontent.com/xndeye/web-ad-rule/master/easylist.txt)
- [cjxlist · annoyance](https://raw.githubusercontent.com/cjx82630/cjxlist/master/cjx-annoyance.txt)
- [TT 拒绝列表](https://raw.githubusercontent.com/A2O-A1/Cat/refs/heads/main/TT%E6%8B%92%E7%BB%9D%E5%88%97%E8%A1%A8%E6%B8%85%E5%8D%95%20(%E6%9B%B4%E6%96%B0%E4%B8%AD).txt)
- [DD-AD](https://raw.githubusercontent.com/afwfv/DD-AD/refs/heads/release/easylist.txt)
- [去广告规则（酷安反馈）](https://raw.githubusercontent.com/qq5460168/666/refs/heads/master/rules.txt)
- [去广告规则（反馈群）](https://raw.githubusercontent.com/qq5460168/dangchu/main/black.txt)
- [AdRules 橘猫](https://adrules.5iclub.us.kg/adblock.txt)
- [jiekouAD](https://raw.githubusercontent.com/damengzhu/banad/main/jiekouAD.txt)
- [浮风拦截规则](https://raw.githubusercontent.com/Thelongdarkorg/ad-rules-merged/main/merged.txt)
- [AdguardFilter 标准](https://raw.githubusercontent.com/miaoermua/AdguardFilter/main/rule.txt)
- [AdguardFilter 黑名单](https://raw.githubusercontent.com/miaoermua/AdguardFilter/main/bad_apple.txt)
- [AdguardFilter 白名单](https://raw.githubusercontent.com/miaoermua/AdguardFilter/main/whitelist.txt)
- [liu50341759/killad](https://github.com/liu50341759/killad/raw/refs/heads/main/rule/all.txt)

**聚合源**

- [xndeye/adblock_list（easylist 产物）](https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/easylist.txt)

</details>

## 说明与免责

- 产物文件由程序生成，请不要直接编辑 `release` 分支上的文件；如需调整，请修改 `config/application.yaml` 后重新生成。
- 规则会随上游更新而变动，个别站点可能出现误杀。遇到误杀请先确认来源：可在运行日志中查看规则所属上游，再用 `adfs.input[].exclude` 做源级排除。
- 本项目仅用于个人学习与自建订阅，不对规则的完整性、时效性与适用性作任何担保。

[easylist-url-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/easylist.url.txt
[easylist-url-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/easylist.url.txt
[easylist-url-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/easylist.url.txt
[easylist-element-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/easylist.element.txt
[easylist-element-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/easylist.element.txt
[easylist-element-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/easylist.element.txt
[ubo-url-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/ubo.url.txt
[ubo-url-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/ubo.url.txt
[ubo-url-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/ubo.url.txt
[ubo-element-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/ubo.element.txt
[ubo-element-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/ubo.element.txt
[ubo-element-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/ubo.element.txt
[abp-url-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/abp.url.txt
[abp-url-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/abp.url.txt
[abp-url-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/abp.url.txt
[abp-element-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/abp.element.txt
[abp-element-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/abp.element.txt
[abp-element-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/abp.element.txt
[dns-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/dns.txt
[dns-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/dns.txt
[dns-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/dns.txt
[hosts-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/hosts.txt
[hosts-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/hosts.txt
[hosts-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/hosts.txt
[dnsmasq-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/dnsmasq.txt
[dnsmasq-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/dnsmasq.txt
[dnsmasq-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/dnsmasq.txt
[smartdns-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/smartdns.txt
[smartdns-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/smartdns.txt
[smartdns-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/smartdns.txt
[clash-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/clash.yaml
[clash-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/clash.yaml
[clash-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/clash.yaml
[sing-box-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/sing-box.json
[sing-box-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/sing-box.json
[sing-box-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/sing-box.json
