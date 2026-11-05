# Ad Filters Subscriber · 规则订阅

![Last Update](https://img.shields.io/github/last-commit/ALLOVERleng/ad-filters-subscriber?style=flat-square&branch=release)
![Build Status](https://img.shields.io/github/actions/workflow/status/ALLOVERleng/ad-filters-subscriber/auto-update.yml?branch=main&style=flat-square)
![Stars](https://img.shields.io/github/stars/ALLOVERleng/ad-filters-subscriber?style=flat-square)
![Forks](https://img.shields.io/github/forks/ALLOVERleng/ad-filters-subscriber?style=flat-square)

本仓库是 [fordes123/ad-filters-subscriber](https://github.com/fordes123/ad-filters-subscriber) 的一个**规则订阅实例**：
GitHub Actions 每 8 小时拉取 **36 个远程上游 + 1 个本地补充列表**，经解析、归一化、去重、格式转换与发布前语法校验，
产出 **14 个可直接订阅的规则文件**，覆盖 AdGuard、uBlock Origin、Adblock Plus、AdGuard DNS、hosts、dnsmasq、
星号通配、SmartDNS、Mihomo（Clash）、sing-box 与 Quantumult X 分流。

产物只发布到 `release` 分支，`main` 分支只保留生成器与配置，**不手工编辑产物**。

> [!IMPORTANT]
> ### AI 参与声明
>
> 本仓库自 **2026-10-30** 起的规则管线改动，以及本 README 的编写与重写，均由 **AI 编程助手在人工指令下生成、经人工复核后提交**。
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

全部文件位于 `release` 分支根目录，同一文件提供 GitHub / ghproxy / jsDelivr 三种取用方式。

| 文件 | 格式 | 说明 | GitHub | ghproxy | jsDelivr |
|------|:-----|:-----|:------:|:-------:|:--------:|
| `easylist.url.txt`     | AdGuard | URL / 网络请求规则 | [link][easylist-url-raw] | [link][easylist-url-ghproxy] | [link][easylist-url-jsdelivr] |
| `easylist.element.txt` | AdGuard | 元素隐藏 / 样式规则 | [link][easylist-element-raw] | [link][easylist-element-ghproxy] | [link][easylist-element-jsdelivr] |
| `ubo.url.txt`          | uBO | URL / 网络请求规则 | [link][ubo-url-raw] | [link][ubo-url-ghproxy] | [link][ubo-url-jsdelivr] |
| `ubo.element.txt`      | uBO | 元素隐藏规则 | [link][ubo-element-raw] | [link][ubo-element-ghproxy] | [link][ubo-element-jsdelivr] |
| `abp.url.txt`          | ABP | URL / 网络请求规则 | [link][abp-url-raw] | [link][abp-url-ghproxy] | [link][abp-url-jsdelivr] |
| `abp.element.txt`      | ABP | 元素隐藏规则 | [link][abp-element-raw] | [link][abp-element-ghproxy] | [link][abp-element-jsdelivr] |
| `dns.txt`              | AdGuard DNS | 纯域名阻断（`\|\|domain^`） | [link][dns-raw] | [link][dns-ghproxy] | [link][dns-jsdelivr] |
| `hosts.txt`            | hosts | `0.0.0.0 domain` | [link][hosts-raw] | [link][hosts-ghproxy] | [link][hosts-jsdelivr] |
| `dnsmasq.txt`          | dnsmasq | `address=/domain/` | [link][dnsmasq-raw] | [link][dnsmasq-ghproxy] | [link][dnsmasq-jsdelivr] |
| `wildcard.txt`         | 星号通配 | 每行 `*.domain` | [link][wildcard-raw] | [link][wildcard-ghproxy] | [link][wildcard-jsdelivr] |
| `smartdns.txt`         | SmartDNS | `address /domain/#` | [link][smartdns-raw] | [link][smartdns-ghproxy] | [link][smartdns-jsdelivr] |
| `clash.yaml`           | Mihomo | 域名规则集（YAML `payload` 列表） | [link][clash-raw] | [link][clash-ghproxy] | [link][clash-jsdelivr] |
| `sing-box.json`        | sing-box | 规则集（JSON） | [link][sing-box-raw] | [link][sing-box-ghproxy] | —（超 20 MB） |
| `quantumult-x.conf`    | Quantumult X | 分流规则 `host\|host-suffix\|host-keyword, 值, reject` | [link][quantumultx-raw] | [link][quantumultx-ghproxy] | —（超 20 MB） |

> [!NOTE]
> `sing-box.json` 与 `quantumult-x.conf` 当前约 **30 MB**，超过 jsDelivr 单文件 20 MB 上限，因此不提供 jsDelivr 链接，
> 请使用 GitHub 直链或 ghproxy。其余文件均在 20 MB 以内，但体积随上游变化，可到 `release` 分支查看实际大小。

### 选择哪一份

浏览器类产物（`easylist` / `ubo` / `abp`）**只保留浏览器语义的规则**：来自 hosts / DNS 类输入的纯域名阻断
（`exclude: [dns-domain]`）不会写进去——它们在浏览器里既无法覆盖全部子域，又会把文件撑到超出 jsDelivr 的 20 MB 上限。
因此**浏览器列表 + 一份域名列表**才是完整组合。

| 使用场景 | 推荐订阅 | 说明 |
|:---------|:---------|:-----|
| **AdGuard 客户端**（桌面 / 移动端 / 浏览器扩展） | `easylist.url.txt` + `easylist.element.txt` + `dns.txt` | 前两份负责 URL 级拦截与元素隐藏，`dns.txt` 补齐纯域名阻断 |
| **uBlock Origin**（Firefox / Brave） | `ubo.url.txt` + `ubo.element.txt` + `dns.txt` | uBO 原生语法；`dns.txt` 同为 AdGuard 语法，可直接作为过滤器导入 |
| **Adblock Plus** | `abp.url.txt` + `abp.element.txt` + `dns.txt` | ABP 原生语法 |
| AdGuard Home / 路由器插件（域名级） | `dns.txt` | 只有域名维度，粒度比其他产物粗，但体积最小 |
| Pi-hole / 路由器 hosts 方案 | `hosts.txt` | 域名级，兼容性最好 |
| dnsmasq | `dnsmasq.txt` | 原生 `address=/domain/` 语法 |
| Blocky / Technitium 等星号通配方案 | `wildcard.txt` | 每行 `*.domain`，**只覆盖子域**，根域需由 `dns.txt` / `hosts.txt` 补 |
| SmartDNS | `smartdns.txt` | 原生 `address /domain/#` 语法 |
| Mihomo / Clash.Meta | `clash.yaml` | 域名规则集（YAML） |
| sing-box | `sing-box.json` | 规则集（JSON） |
| **Quantumult X** | `quantumult-x.conf` | 分流规则，每行 `类型, 匹配值, reject`；只有「拒绝 / 直连 / 代理」，没有放行写法 |

> [!NOTE]
> 已经是域名级方案（AdGuard Home、Pi-hole、dnsmasq、SmartDNS、Mihomo、sing-box、Quantumult X）时，**只订一份对应的域名列表**即可，
> 再叠加其它域名列表只会增加重复规则。浏览器方案同理，`ubo.*` 与 `abp.*` 不要和 `easylist.*` 同时订阅。

### 关于体积与粒度

| 取舍 | 表现 | 原因 |
|:-----|:-----|:-----|
| 浏览器产物不含纯域名阻断 | `easylist` / `ubo` / `abp` 体积可控 | 纯域名阻断在 DNS 类产物里已完整覆盖，浏览器侧再写一遍只是翻倍 |
| `wildcard.txt` 不覆盖根域 | 每个域名只需一行，体积最小 | 后缀规则统一写成 `*.domain`，`*` 只匹配子域，根域需由 `dns.txt` / `hosts.txt` 补齐；精确主机仍以裸域名行保留 |
| 站点限定规则被丢弃 | hosts / DNS 产物条数少于浏览器产物 | `$domain=` 无法在域名格式里保真，放大成整域会误伤 |
| 超限产物不提供 jsDelivr | `sing-box.json`、`quantumult-x.conf` 只有 GitHub / ghproxy 两种取用方式 | 两个文件约 30 MB，超出 jsDelivr 单文件 20 MB 上限 |

## 产物是怎么来的

```
HTTP(S) 拉取 → 解析 → 归一化 → 源级 exclude → 去重 / 白名单优先
            → 按目标格式转换写出 → 发布前语法校验 → 目录原子发布
```

1. **拉取**：仅接受 `http` / `https`，重定向上限 5 次；`408`、`429`、`5xx` 与网络异常按 `adfs.config.http.retries`
   （默认 2 次）重试，等待从 250 ms 指数退避至 2 s 上限，并遵循 `Retry-After`；`401` / `403` 立即失败。
   远程 `@include` 只接受同源、且不越过根来源基址的相对 URI。
2. **解析**：按 `input[].type` / `dialect` 选择解析器（adblock、dns、hosts、dnsmasq、smartdns、mihomo、sing-box），
   逐行归一化为统一的规则模型，超长行、非法域名、注释等计入报告。
3. **源级排除**：`input[].exclude` 在落盘前丢弃规则（域名、`*.域名` 或关键词），被排除的条目不再参与去重和产出。
4. **去重与白名单优先**：全局按目标去重；同目标的放行规则优先，阻断规则直接剔除（`allow-overrides-block`）。
5. **转换写出**：按输出 `type` / `dialect` 逐条转换。语义无法保真的规则依据 `allow-expansion` / `allow-reduction`
   决定保留（记 EXPANDED / REDUCED）还是丢弃，逐条写入日志。
6. **发布前校验**：产物落盘后再按目标格式复检——`adblock` / `dns` / `wildcard` 严格校验并剔除非法行，
   `hosts` / `dnsmasq` / `smartdns` / `mihomo` 只做空行、控制字符、超长行等通用检查，`sing-box` 由 JSON 生成器结构化写出。
   剔除条数计入日志与运行报告。
7. **发布**：先写入暂存目录并生成清单（路径、文件集合、大小与 SHA-256 校验），再以**整目录交换**的方式替换 `dist/`，
   任意一步失败都不会留下新旧混杂的产物。

### 关键行为与取舍

本实例的转换层并不只是「拉取 + 拼接」，针对实际使用中遇到的问题做了一批处理：

| 行为 | 说明 |
|:-----|:-----|
| **黑白名单冲突** | 同一域名同时出现在黑名单与白名单时**保留白名单、剔除阻断规则**（`allow-overrides-block`，默认开启）。目标按域名归一，`\|\|x^`、`\|x\|`、`0.0.0.0 x`、`address /x/...` 视为同一目标，黑白名单来自不同格式的不同上游也能互相抵消。 |
| **白名单写出前减法** | 白名单域名不再「先写阻断再补 `@@`」，而是在渲染前删除阻断规则。hosts / dnsmasq / Mihomo / sing-box 本身无法表达放行，只有做减法才能与 AdGuard 侧结果一致。`dns.txt` 例外：末尾会补写 `@@\|\|domain^`，用于与订阅方本地的其它规则源共存时显式放行。 |
| **源级排除 `exclude`** | 可为单个上游声明排除项，用于隔离上游误报。当前用于 `1Hosts Lite` 对 GitHub 加速网关（`gh-proxy.com` / `gh-proxy.org`）的整域误杀。 |
| **发布前语法校验** | 见上一步：严格校验的格式剔除非法行，其余只做通用检查，避免误杀合法的透传行。 |
| **元素 / URL 规则拆分** | adblock 文本输出默认拆成 `.element.txt` 与 `.url.txt`：元素文件只放 cosmetic 规则，`@@` 放行只写入 URL 文件，订阅方可按需取用；`split: false` 可退回单文件。 |
| **站点作用域收敛** | `$domain=` 这类「只在指定站点生效」的规则在 hosts / DNS / dnsmasq / SmartDNS / Mihomo / sing-box 上无法保真，**直接丢弃**。放大成整域规则会让阻断误伤、让放行变成整域放行（例如只对某站点生效的 `@@\|\|raw.githubusercontent.com^$domain=...` 曾被放大成整域放行，抵消了对该域名上万条恶意 URL 的拦截）。 |
| **分流规则的策略语义** | Quantumult X 分流的 `direct` 表示**直连**，不是「不拦截」：写成 `direct` 会把对应流量从代理里摘出去，改变订阅方的分流结果。因此 `@@` 放行、白名单域名只做减法（剔除阻断行），绝不写成 `direct`；`$domain=` 站点作用域与正则域名没有对应写法，同样直接丢弃。`hosts` 里的地址映射（`1.2.3.4 x`）也无法用分流表达，一并丢弃。 |
| **备用地址 `fallback`** | 主地址打不开时按声明顺序尝试备用地址，全部失败才走 `optional`。只覆盖「打不开」这一阶段（HTTP 4xx/5xx、连接失败、本地文件缺失），**解析阶段的语法错误不会重试**——同一份坏数据重读一遍不会变好。适用于 raw 域名被阻断、镜像抖动、上游换地址；备用地址必须与主地址同为 HTTP 或同为本地路径，重复项与空白项会被忽略。 |
| **可选输入 `optional`** | 给单个上游声明 `optional: true` 后，读取失败只记录「跳过可选来源」并继续处理其余源，不再让一个源的抖动导致整批产物停止更新。默认关闭以保持严格失败语义，当前用于 Gitflic 单点源 `Bypass Paywalls Clean filter` 与已删除的上游 `xndeye web-ad-rule`。 |
| **浏览器产物裁剪** | `easylist` / `ubo` / `abp` 排除纯域名阻断（`exclude: [dns-domain]`），既避免和 DNS 产物重复，也避免超出 jsDelivr 的单文件体积上限。 |
| **裸域名规则收紧** | 上游（实测 HyperADRules ads 有 17132 条）会把域名列表直接混进 adblock 文件。`ad.qq.com` 这种裸行在客户端是 **URL 子串匹配**，会连 `weread.qq.com`（结尾恰好是 `ad.qq.com`）一起拦掉。产物统一收紧为 `\|\|ad.qq.com^`，只覆盖该域及其子域；单标签（`ads`）、含通配符 / 路径、以及 `match-case` 规则保持原样，避免锚定后彻底失效。该转换记为 REDUCED，需要 `allow-reduction: true` 才会保留。识别逻辑对所有域名类产物（`dns` / `wildcard` / `hosts` / `dnsmasq` / `mihomo` 等）同样生效。 |
| **星号通配语义** | `wildcard.txt` 每行 `*.domain`，`*` 只匹配子域、**不匹配根域名本身**：`\|\|ad.qq.com^` 会写成 `*.ad.qq.com`，而精确主机（`\|domain\|`）写成裸域名行 `domain`。关键词与正则规则无法用单行星号表达，直接丢弃并计入报告。 |
| **整目录原子发布** | 产物先落在暂存目录并通过清单校验，再整体替换 `dist/`；中途失败不会产生新旧混杂，配置里删掉的输出文件也不会残留。 |

## 本地构建与运行

需要 **JDK 25**（Gradle Wrapper 已随仓库提供）。

```bash
# 1) 打包可执行 jar
./gradlew --no-daemon shadowJar

# 2) 按配置生成规则，产物写入 dist/
java -jar build/libs/adfs-*-all.jar -c config/application.yaml
```

常用参数：

| 参数 | 说明 |
|:-----|:-----|
| `-c, --config <文件>` | 指定 YAML 配置文件 |
| `-h, --help` | 查看帮助 |
| `-V, --version` | 打印版本（当前 `2.0.0`） |

运行目录结构：

| 路径 | 内容 |
|:-----|:-----|
| `dist/` | 本轮产物（14 个规则文件，带文件头） |
| `logs/adfs.log` | 运行日志，按天滚动保留 30 天 |
| `.dist.adfs.lock` | 运行锁，防止并发写同一输出目录 |
| `rules-src/private.txt` | 本地补充规则，作为输入参与合并 |
| `config/application.yaml` | 本订阅实例的实际配置 |
| `config/application-example.yaml` | 配置示例，可作为自建订阅的起点 |
| `docs/adblock.md` | AdGuard / uBO / ABP 语法对照，转换取舍的依据 |

退出码：

| 退出码 | 含义 |
|:------:|:-----|
| `0` | 成功 |
| `2` | 配置错误 |
| `3` | 输入读取失败 |
| `4` | 规则处理失败 |
| `5` | DNS 校验失败 |
| `6` | 产物写出 / 发布失败 |

> DNS 校验（`adfs.config.dns.enabled`）在本实例中**关闭**：产物面向纯规则订阅，不在生成阶段做解析验证。
> 需要时可在配置中开启，并使用 `strict-mode` 收紧可转换的规则形态。

## 自动化流程

| 工作流 | 触发 | 作用 |
|:-------|:-----|:-----|
| `auto-update.yml` | 每 8 小时定时；`main` 分支代码变更；手动触发 | 构建 → 生成 → 以强制提交方式把产物发布到 `release` 分支（提交信息 `chore: update filters`），并上传运行日志 |
| `verify.yml` | 任意 push / PR / 手动触发 | `gradlew check shadowJar` 编译与测试，校验 `--version` 可执行 |
| `release.yml` | 推送 `v*` 标签 | 打包 JVM 发行版 zip（`shadowDistZip`）与 GraalVM 原生可执行文件（Linux / Windows / macOS），生成 `SHA256SUMS` 并发布 GitHub Release |

`auto-update.yml` 里 `README.md`、`.github/**`、`.devcontainer/**`、`LICENSE` 的改动不会触发产物更新。

## 上游规则

<details>
<summary>点击查看 36 个远程上游 + 1 个本地补充</summary>

**AdGuard 官方过滤器（8）**

- [AdGuard 恼人广告过滤器](https://raw.githubusercontent.com/AdguardTeam/FiltersRegistry/master/filters/filter_14_Annoyances/filter.txt)
- [AdGuard Cookie 通知过滤器](https://filters.adtidy.org/android/filters/18_optimized.txt)
- [AdGuard 弹窗过滤器](https://filters.adtidy.org/android/filters/19_optimized.txt)
- [AdGuard 移动端程序内横幅过滤器](https://filters.adtidy.org/android/filters/20_optimized.txt)
- [AdGuard 其他恼人广告过滤器](https://filters.adtidy.org/android/filters/21_optimized.txt)
- [AdGuard 小工具过滤器](https://filters.adtidy.org/android/filters/22_optimized.txt)
- [AdGuard 防跟踪保护过滤器](https://filters.adtidy.org/android/filters/3_optimized.txt)
- [ABP 反广告过滤（anti-cv）](https://easylist-downloads.adblockplus.org/abp-filters-anti-cv.txt)

**恶意域名 / 遥测 / 隐私（6）**

- [Online Malicious URL Blocklist](https://urlhaus-filter.pages.dev/urlhaus-filter-ag-online.txt)
- [HyperADRules · ads](https://github.com/Lynricsy/HyperADRules/releases/latest/download/hyper_adrules_ads_adguard.txt)
- [HyperADRules · malware](https://github.com/Lynricsy/HyperADRules/releases/latest/download/hyper_adrules_malware_adguard.txt)
- [HyperADRules · allow](https://github.com/Lynricsy/HyperADRules/releases/latest/download/hyper_adrules_allow_adguard.txt)
- [Telemetry Lists - Aggregated](https://raw.githubusercontent.com/SystemJargon/filters/refs/heads/main/telemetry.txt)
- [Bypass Paywalls Clean filter](https://gitflic.ru/project/magnolia1234/bypass-paywalls-clean-filters/blob/raw?file=bpc-paywall-filter.txt)（`optional: true`，Gitflic 单点源不可用时不阻断整批产出）

**DNS / hosts 类（7）**

- [1Hosts Lite](https://raw.githubusercontent.com/badmojr/1Hosts/master/Lite/adblock.txt)（源级排除 `gh-proxy.com` / `gh-proxy.org`）
- [HaGeZi's Pro DNS Blocklist](https://raw.githubusercontent.com/hagezi/dns-blocklists/refs/heads/main/adblock/pro.txt)
- [DNS-Kuner 黑名单](https://raw.githubusercontent.com/Kuner-mw/DNS-Kuner/main/FilterRules/blacklist.txt)
- [DNS-Kuner 白名单](https://raw.githubusercontent.com/Kuner-mw/DNS-Kuner/main/FilterRules/allowlist.txt)
- [JiuYue_Rules](https://rules.vlo.cc/dns/dns.txt)
- [SM 广告丟那吗 hosts](https://raw.githubusercontent.com/2Gardon/SM-Ad-FuckU-hosts/refs/heads/master/SMAdHosts)（hosts 格式）
- [NEO DEV HOST](https://raw.githubusercontent.com/neodevpro/neodevhost/master/adblocker)

**中文社区规则（14）**

- [晴雅广告拦截规则](https://raw.githubusercontent.com/rssvcn/qy-Ads-Rule/main/black.txt)
- `xndeye/web-ad-rule`（上游仓库已删除，`raw` 与 API 均 404，自 2026-11 起以 `optional: true` 跳过；内容由下方同作者的合并列表提供）
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

**聚合源（1）**

- [xndeye/adblock_list（easylist 产物）](https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/easylist.txt)

**本地补充（1）**

- [`rules-src/private.txt`](rules-src/private.txt)：对上游规则的补充与修正，随仓库维护

</details>

## 常见问题

<details>
<summary>某个站点被误杀了，怎么办？</summary>

先在 `logs/adfs.log` 里按域名搜索，确认规则来自哪个上游，再选择处理方式：

- 上游误报、且只影响个别域名：在对应 `input[].exclude` 中声明（支持域名、`*.域名` 与关键词）；
- 需要长期放行的通用域名：加入 `adfs.config.rules.whitelist`，所有格式的产物都会在写出前减掉；
- 只是某个源暂时不可用：把该源设为 `optional: true`，避免它拖停整批产物。

以上都解决不了时，按[反馈](#反馈)里的模板提 issue，记得附上命中的规则行。

</details>

<details>
<summary>为什么根域名没有被 `wildcard.txt` 覆盖？</summary>

`*` 在多数通配实现里只匹配子域。为保持单行语义清晰，`wildcard.txt` 只负责子域，根域（精确主机）由 `dns.txt` / `hosts.txt` 覆盖。
如果使用方只订一份文件，请优先选 `dns.txt`。

</details>

<details>
<summary>产物为什么分成 `.element.txt` 和 `.url.txt`？</summary>

元素隐藏规则（cosmetic）与网络请求规则（URL）在不同客户端里的支持度、更新频率都不同，拆分后订阅方可以只取需要的一类；
同时 `@@` 放行只写入 URL 文件，元素文件保持纯 cosmetic，避免客户端把放行规则应用范围扩大。

</details>

## 反馈

误杀、漏拦截、上游失效、管线异常都可以提 issue，四种模板分别对应：

| 类型 | 适用场景 | 需要提供 |
|:-----|:---------|:---------|
| [误杀反馈][issue-new] | 正常站点或功能被拦 | 被拦域名、订阅的产物、客户端与版本 |
| [漏拦截反馈][issue-new] | 广告或跟踪没被拦住 | 广告形态、页面地址、请求 URL |
| [上游失效][issue-new] | 某上游 404 / 超时 / 停更 | 上游名称与地址、日志片段、替代地址 |
| [管线问题][issue-new] | 构建、生成、校验、发布出错 | 出问题的环节、复现命令、退出码与日志 |

提交前建议先自查三步，很多问题可以就地解决：

1. **确认来源**：在订阅的产物里搜被拦域名，确认规则确实来自本仓库，而不是客户端内置列表；
2. **定位上游**：在 `logs/adfs.log` 里按域名搜索，日志会带上命中的上游名称；
3. **临时放行**：个别域名加入 `adfs.config.rules.whitelist`；某个上游误报过多，用 `input[].exclude` 做源级排除。

几条说明：

- 产物每 8 小时重建一次。修复合并后请等下一次构建完成，再在客户端里手动刷新订阅；
- **上游规则本身的增删**请优先反馈给对应上游（清单见[上游规则](#上游规则)），改本仓库的配置只会影响聚合结果；
- 不接受政治敏感、商业推广类请求，也不处理「删除与广告无关域名」这类要求。

## 说明与免责

- 产物文件由程序生成，请不要直接编辑 `release` 分支上的文件；如需调整，请修改 `config/application.yaml` 后重新生成。
- 规则会随上游更新而变动，个别站点可能出现误杀。遇到误杀请先确认来源：可在运行日志中查看规则所属上游，再用 `adfs.input[].exclude` 做源级排除。
- 本项目仅用于个人学习与自建订阅，不对规则的完整性、时效性与适用性作任何担保。

> 本文档依据 `main` 分支当前实现编写，最近一次核对：2026-11-05。

[issue-new]: https://github.com/ALLOVERleng/ad-filters-subscriber/issues/new/choose
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
[wildcard-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/wildcard.txt
[wildcard-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/wildcard.txt
[wildcard-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/wildcard.txt
[smartdns-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/smartdns.txt
[smartdns-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/smartdns.txt
[smartdns-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/smartdns.txt
[clash-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/clash.yaml
[clash-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/clash.yaml
[clash-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@release/clash.yaml
[sing-box-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/sing-box.json
[sing-box-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/sing-box.json
[quantumultx-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/quantumult-x.conf
[quantumultx-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/quantumult-x.conf

