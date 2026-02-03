<div align="center">
<h1>AD Filter Subscriber</h1>
  <p>
    广告过滤规则订阅器，整合不同来源的规则，帮助你快速构建属于自己的规则集~
  </p>
<!-- Badges -->
<p>
  <img src="https://img.shields.io/github/last-commit/fordes123/ad-filters-subscriber?style=flat-square" alt="last update" />
  <img src="https://img.shields.io/github/forks/fordes123/ad-filters-subscriber?style=flat-square" alt="forks" />
  <img src="https://img.shields.io/github/stars/fordes123/ad-filters-subscriber?style=flat-square" alt="stars" />
  <img src="https://img.shields.io/github/issues/fordes123/ad-filters-subscriber?style=flat-square" alt="open issues" />
  <img src="https://img.shields.io/github/license/fordes123/ad-filters-subscriber?style=flat-square" alt="license" />
</p>


<h4>
    <a href="#a">项目说明</a>
  <span> · </span>
    <a href="#b">快速开始</a>
  <span> · </span>
    <a href="#c">规则订阅</a>
  <span> · </span>
    <a href="#d">问题反馈</a>
  </h4>

</div>

[English](./README_en.md) | 中文

<h2 id="a">📔 项目说明</h2>

本项目旨在聚合不同来源、不同格式的广告过滤规则，自由的进行转换和整合。

> ⚠️ 新版不再兼容原配置格式，迁移前务必注意

#### 支持的规则格式

- [x] easylist
- [x] dns (AdGuardHome)
- [x] dnsmasq
- [x] clash
- [x] smartdns
- [x] hosts

#### 注意事项

1. 仅支持基本规则转换，即域名、通配域名构成的规则，对形如 `||example.org^$popup` 等规则无法转换(合并、去重不受影响) 
2. 接受不可避免的缩限，如 `||example.org^` 将拦截 example.org 及其所有子域，但将其转换为 hosts 格式时，将无法匹配子域名。
3. 规则有效性检测基于域名解析，因此仅支持基本规则 (只能检测当前域有效性，而无法检测其是否存在有效子域，故此功能可能存在误杀)。

<h2 id="b">🛠️ 快速开始</h2>

### 示例配置

```yaml
application:
  
  # 输入配置
  input:
    - name: 'Subscription 1'               #可选参数: 规则名称，如无将使用 path 作为名称
      path: 'https://example.org/rule.txt' #必要参数: 规则 url (http/https) 或 本地文件位置 (绝对/相对路径)
      type: easylist                       #可选参数: 规则类型：easylist (默认)、dns、dnsmasq、clash、smartdns、hosts

    - name: 'Subscription 2'
      path: 'rule/local.txt'
      type: hosts

  # 输出配置
  output:
    #文件头配置，将自动作为注释添加至每个规则文件开始
    #可使用占位符 ${name}、${type}、${desc} 以及 ${date} (当前日期)、${total} (规则总数)
    file_header: |
      ADFS AdBlock ${type}
      Last Modified: ${date}
      Total Size: ${total}
      Homepage: https://github.com/fordes123/ad-filters-subscriber/
      
    files:
      - name: easylist.txt     #必要参数: 文件名
        type: easylist         #必要参数: 文件类型: easylist、dns、dnsmasq、clash、smartdns、hosts
        file_header:           #可选参数: 文件头配置，将自动作为注释添加至每个规则文件开始 (此处优先于 output.file_header)
        desc: 'ADFS EasyList'  #可选参数: 文件描述，可在file_header中通过 ${} 中使用
        filter:
          - basic              #基本规则，不包含任何控制、匹配符号, 可以转换为 hosts
          - wildcard           #通配规则，仅使用通配符
          - unknown            #其他规则，如使用了正则、高级修饰符号的规则，这些规则目前无法转换为其他格式
        rule:                  #可选参数: 限定此文件使用的规则源，如果不指定则使用 input 中的所有规则源
          - Subscription 1
          - Subscription 2     
```

---

本程序基于 `Java21` 编写，使用 `Maven` 进行构建，你可以参照[示例配置](./config/application-example.yaml)，编辑 `config/application.yaml`
，并通过以下任意一种方式快速开始：

#### **本地调试**

```bash
git clone https://github.com/fordes123/ad-filters-subscriber.git
cd ad-filters-subscriber
mvn clean
mvn spring-boot:run
```

#### **Github Action**

- fork 本项目
- 自定义规则订阅 (可选)
  - 参照[示例配置](./config/application-example.yaml)，修改配置文件: `config/application.yaml`
- 打开 `Github Action` 页面，选中左侧 `Update Filters` 授权 `Workflow` 定时执行(⚠ 重要步骤)
- 点击 `Run workflow` 或等待自动执行。执行完成后规则将生成在 `release` 分支

#### **Codespaces**

- 登录 `Github`，点击本仓库右上角 `Code` 按钮，选择并创建新的 `Codespaces`
- 等待 `Codespaces` 启动，即可直接对本项目进行调试

<h2 id="c">🎯 规则订阅</h2>

# AbBlock

![Last Update](https://img.shields.io/github/last-commit/xndeye/adblock_list?style=flat-square&branch=release)
![Build Status](https://img.shields.io/github/actions/workflow/status/xndeye/adblock_list/auto-update.yml?branch=main&style=flat-square)
![Stars](https://img.shields.io/github/stars/xndeye/adblock_list?style=flat-square)
![Forks](https://img.shields.io/github/forks/xndeye/adblock_list?style=flat-square)


💪 强大而克制的广告过滤规则，可拦截 99%[^1] 的 Web 广告！

> [!TIP]
> 本仓库通过 [ad-filters-subscriber](https://github.com/fordes123/ad-filters-subscriber/) 构建，定时合并多个优质上游规则，并去除重复和失效项。  
> 构建和转换错误请反馈至 [此处](https://github.com/fordes123/ad-filters-subscriber/issues)，误杀和规则推荐请提交至本仓库 [issues](https://github.com/xndeye/adblock_list/issues)

| 文件            | 说明                               |        github        |         ghproxy          |         jsdelivr          |
| --------------- | :--------------------------------- | :------------------: | :----------------------: | :-----------------------: |
| `easylist.txt`  | 完整主规则                         | [link][easylist-raw] | [link][easylist-ghproxy] | [link][easylist-jsdelivr] |
| `modify.txt`    | 不含 DNS 过滤规则的 `easylist.txt` |  [link][modify-raw]  |  [link][modify-ghproxy]  |  [link][modify-jsdelivr]  |
| `dns.txt`       | 仅含 DNS 过滤规则的 `easylist.txt` |   [link][dns-raw]    |   [link][dns-ghproxy]    |   [link][dns-jsdelivr]    |
| `dnsmasq.conf`  | dnsmasq 及其衍生版本               | [link][dnsmasq-raw]  | [link][dnsmasq-ghproxy]  | [link][dnsmasq-jsdelivr]  |
| `clash.yaml`    | clash 及其衍生版本                 |  [link][clash-raw]   |  [link][clash-ghproxy]   |  [link][clash-jsdelivr]   |
| `smartdns.conf` | smartdns                           | [link][smartdns-raw] | [link][smartdns-ghproxy] | [link][smartdns-jsdelivr] |

[easylist-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/easylist.txt

[easylist-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/easylist.txt

[easylist-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@refs/heads/release/easylist.txt

[modify-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/modify.txt

[modify-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/modify.txt

[modify-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@refs/heads/release/modify.txt

[dns-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/dns.txt

[dns-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/dns.txt

[dns-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@refs/heads/release/dns.txt

[dnsmasq-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/dnsmasq.conf

[dnsmasq-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/dnsmasq.conf

[dnsmasq-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@refs/heads/release/dnsmasq.conf

[clash-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/clash.yaml

[clash-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/clash.yaml

[clash-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@refs/heads/release/clash.yaml

[smartdns-raw]: https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/smartdns.conf

[smartdns-ghproxy]: https://ghproxy.net/https://raw.githubusercontent.com/ALLOVERleng/ad-filters-subscriber/refs/heads/release/smartdns.conf

[smartdns-jsdelivr]: https://gcore.jsdelivr.net/gh/ALLOVERleng/ad-filters-subscriber@refs/heads/release/smartdns.conf

下面是使用了本项目进行构建的规则仓库，可在其中寻找合适的规则订阅:

<details>
<summary>点击查看</summary>
<ul>
    <br/>
    <li><a href="https://github.com/xndeye/adblock_list/">xndeye/adblock_list</a></li>
</ul>
</details>


<h2 id="d">💬 问题反馈</h2>

- 👉 [issues](https://github.com/fordes123/ad-filters-subscriber/issues)
