# AbBlock List

![Last Update](https://img.shields.io/github/last-commit/xndeye/adblock_list?style=flat-square&branch=release)
![Build Status](https://img.shields.io/github/actions/workflow/status/xndeye/adblock_list/auto-update.yml?branch=main&style=flat-square)
![Stars](https://img.shields.io/github/stars/xndeye/adblock_list?style=flat-square)
![Forks](https://img.shields.io/github/forks/xndeye/adblock_list?style=flat-square)


💪 强大而克制的广告过滤规则，可拦截 99%[^1] 的 Web 广告！

> [!TIP]
> 本项目定时合并多个优质上游规则，并进行去重和转换，  
> 因此推荐单独订阅本仓库规则，**禁用广告拦截器内置的规则列表**以获得最佳的性能和体验。

> [!WARNING]
> Chrome、Edge 等浏览器已放弃 MV2，原则上也不支持本仓库规则，  
> 请及时检查广告拦截器是否正常工作，推荐改用 Firefox、Brave 浏览器！

| 文件            | 说明                                    | github               | ghproxy                  | jsdelivr                  |
|-----------------|:----------------------------------------|:--------------------:|:------------------------:|:-------------------------:|
| `easylist.url.txt`     | AdGuard 广告过滤规则（URL/网络）      | [link][easylist-url-raw]     | [link][easylist-url-ghproxy]     | [link][easylist-url-jsdelivr]     |
| `easylist.element.txt` | AdGuard 广告过滤规则（元素隐藏/样式）  | [link][easylist-element-raw] | [link][easylist-element-ghproxy] | [link][easylist-element-jsdelivr] |
| `ubo.url.txt`          | uBlock Origin 广告过滤规则（URL/网络） | [link][ubo-url-raw]          | [link][ubo-url-ghproxy]          | [link][ubo-url-jsdelivr]          |
| `ubo.element.txt`      | uBlock Origin 广告过滤规则（元素隐藏） | [link][ubo-element-raw]      | [link][ubo-element-ghproxy]      | [link][ubo-element-jsdelivr]      |
| `abp.url.txt`          | Adblock Plus 广告过滤规则（URL/网络）  | [link][abp-url-raw]          | [link][abp-url-ghproxy]          | [link][abp-url-jsdelivr]          |
| `abp.element.txt`      | Adblock Plus 广告过滤规则（元素隐藏）  | [link][abp-element-raw]      | [link][abp-element-ghproxy]      | [link][abp-element-jsdelivr]      |
| `dns.txt`        | AdGuard DNS 过滤规则                     | [link][dns-raw]       | [link][dns-ghproxy]       | [link][dns-jsdelivr]       |
| `hosts.txt`      | Hosts 格式规则                          | [link][hosts-raw]     | [link][hosts-ghproxy]     | [link][hosts-jsdelivr]     |
| `dnsmasq.txt`    | dnsmasq 格式规则                         | [link][dnsmasq-raw]   | [link][dnsmasq-ghproxy]   | [link][dnsmasq-jsdelivr]   |
| `smartdns.txt`   | SmartDNS 格式规则                        | [link][smartdns-raw]  | [link][smartdns-ghproxy]  | [link][smartdns-jsdelivr]  |
| `clash.yaml`     | Mihomo 域名规则集（YAML）                 | [link][clash-raw]     | [link][clash-ghproxy]     | [link][clash-jsdelivr]     |
| `sing-box.json`  | sing-box 规则集（JSON）                  | [link][sing-box-raw]  | [link][sing-box-ghproxy]  | [link][sing-box-jsdelivr]  |
| `private.txt`    | 本仓库维护的私有规则，以 easylist 形式提供 | [link][private-raw]   | [link][private-ghproxy]   | [link][private-jsdelivr]   |

[easylist-url-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/easylist.url.txt

[easylist-url-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/easylist.url.txt

[easylist-url-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/easylist.url.txt

[easylist-element-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/easylist.element.txt

[easylist-element-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/easylist.element.txt

[easylist-element-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/easylist.element.txt

[ubo-url-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/ubo.url.txt

[ubo-url-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/ubo.url.txt

[ubo-url-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/ubo.url.txt

[ubo-element-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/ubo.element.txt

[ubo-element-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/ubo.element.txt

[ubo-element-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/ubo.element.txt

[abp-url-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/abp.url.txt

[abp-url-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/abp.url.txt

[abp-url-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/abp.url.txt

[abp-element-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/abp.element.txt

[abp-element-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/abp.element.txt

[abp-element-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/abp.element.txt

[dns-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/dns.txt

[dns-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/dns.txt

[dns-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/dns.txt

[hosts-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/hosts.txt

[hosts-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/hosts.txt

[hosts-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/hosts.txt

[dnsmasq-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/dnsmasq.txt

[dnsmasq-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/dnsmasq.txt

[dnsmasq-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/dnsmasq.txt

[smartdns-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/smartdns.txt

[smartdns-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/smartdns.txt

[smartdns-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/smartdns.txt

[clash-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/clash.yaml

[clash-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/clash.yaml

[clash-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/clash.yaml

[sing-box-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/sing-box.json

[sing-box-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/sing-box.json

[sing-box-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/sing-box.json

[private-raw]: https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/private.txt

[private-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/xndeye/adblock_list/refs/heads/release/private.txt

[private-jsdelivr]: https://gcore.jsdelivr.net/gh/xndeye/adblock_list@refs/heads/release/private.txt

<details>
<summary>点击查看上游规则</summary>
<ul>
    <li><a href="https://filters.adtidy.org/extension/ublock/filters/2_optimized.txt"><del>AdGuard 基础过滤器</del></a></li>
    <li><a href="https://filters.adtidy.org/extension/ublock/filters/11_optimized.txt">AdGuard 移动广告过滤器</a></li>
    <li><a href="https://filters.adtidy.org/extension/ublock/filters/3_optimized.txt">AdGuard 防跟踪保护过滤器</a></li>
    <li><a href="https://filters.adtidy.org/extension/ublock/filters/17_optimized.txt">AdGuard URL跟踪过滤器</a></li>
    <li><a href="https://filters.adtidy.org/extension/ublock/filters/14_optimized.txt">AdGuard 恼人广告过滤器</a></li>
    <li><a href="https://filters.adtidy.org/extension/ublock/filters/10_optimized.txt">AdGuard 解除搜索广告和自我推销过滤器</a></li>
    <li><a href="https://filters.adtidy.org/extension/ublock/filters/224_optimized.txt">AdGuard 中文过滤器</a></li>
    <li><a href="https://github.com/TG-Twilight/AWAvenue-Adblock-Rule">AWAvenue-Adblock-Rule</a></li>
    <li><a href="https://raw.githubusercontent.com/Noyllopa/NoAppDownload/master/NoAppDownload.txt">NoAppDownload</a></li>
    <li><a href="https://github.com/xndeye/web-ad-rule"><del>xndeye/web-ad-rule</del></a></li>
    <li><a href="https://github.com/xinggsf/Adblock-Plus-Rule">xinggsf/Adblock-Plus-Rule</a></li>
    <li><a href="https://github.com/damengzhu/banad"><del>damengzhu/banad</del></a></li>
    <li><a href="https://github.com/cjx82630/cjxlist">cjx82630/cjxlist</a></li>
    <li><a href="https://easylist-downloads.adblockplus.org/easylistchina+easylist.txt">ABP EasyList China+EasyList</a></li>
    <li><a href="https://easylist-downloads.adblockplus.org/abp-filters-anti-cv.txt">ABP filters</a></li>
    <li><a href="https://raw.githubusercontent.com/uBlockOrigin/uAssets/refs/heads/master/filters/privacy.txt">uBlockOrigin privacy</a></li>
    <li><a href="https://github.com/SystemJargon/filters">Telemetry Lists</a></li>
    <li><a href="https://github.com/badmojr/1Hosts">1Hosts Lite</a></li>
</ul>
</details>

[^1]: 数据在 `Chrome 130.0` 上使用 `AdGuard 浏览器扩展` 订阅本仓库 `easylist.txt` 规则，
通过 [d3ward/toolz](https://d3ward.github.io/toolz/adblock.html) 测得，[结果](https://github.com/user-attachments/assets/76ccfcac-9ffd-4bed-89d7-08cdfe6cc33d)仅供参考
