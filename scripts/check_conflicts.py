#!/usr/bin/env python3
"""发布前校验：同一域名不得同时出现在阻断与放行集合中。

背景：上游会把域名列表直接混进 adblock 文件（HyperADRules ads 实测 17,132 条裸行），
这类规则解析为 URL 型、写出阶段才收紧成 ``||domain^``；若冲突消解与写出使用不同的
目标键，同一域名的阻断与放行就会同时进入产物，客户端行为取决于规则顺序。

例外：带 ``$important`` 的阻断规则在客户端压过例外规则，阻断结果确定，不计入冲突。

用法::

    python3 scripts/check_conflicts.py dist/dns.txt
    python3 scripts/check_conflicts.py dist/dns.txt --baseline scripts/conflicts-baseline.txt
    python3 scripts/check_conflicts.py dist/dns.txt dist/hosts.txt --report conflicts.txt

退出码：0 通过；1 存在新的黑白冲突；2 参数或文件错误。
"""

from __future__ import annotations

import argparse
import re
import sys

BLOCK = re.compile(r"^\|\|([^$^]+)\^")
ALLOW = re.compile(r"^@@\|\|([^$^]+)\^")
# 地址映射行（`<IP> <domain>`）是设计内语义，不参与域名冲突判定。
MAPPING = re.compile(r"^\d{1,3}(\.\d{1,3}){3}\s")


def parse(path: str) -> tuple[set[str], set[str], int, int]:
    """返回 (阻断域名集, 放行域名集, 地址映射行数, 带 $important 的阻断行数)。"""
    block: set[str] = set()
    allow: set[str] = set()
    mappings = 0
    important = 0
    # utf-8-sig 兼容带 BOM 的手工编辑文件，避免首行规则被静默跳过。
    with open(path, encoding="utf-8-sig", errors="replace") as handle:
        for line in handle:
            line = line.strip()
            if not line or line.startswith("!"):
                continue
            match = ALLOW.match(line)
            if match:
                allow.add(match.group(1).strip().lower())
                continue
            match = BLOCK.match(line)
            if match:
                # $important 在客户端压过例外规则，阻断结果确定，不属于顺序敏感的冲突。
                if "$important" in line:
                    important += 1
                    continue
                block.add(match.group(1).strip().lower())
                continue
            if MAPPING.match(line):
                mappings += 1
    return block, allow, mappings, important


def load_baseline(path: str | None) -> set[str]:
    if not path:
        return set()
    known: set[str] = set()
    try:
        with open(path, encoding="utf-8-sig") as handle:
            for line in handle:
                line = line.strip()
                if line and not line.startswith("#"):
                    known.add(line.lower())
    except FileNotFoundError:
        print(f"警告: 基线文件不存在，按空基线处理: {path}", file=sys.stderr)
    return known


def main() -> int:
    # Windows 控制台默认 GBK，中文输出可能抛 UnicodeEncodeError；CI 为 UTF-8，此处统一兜底。
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")

    parser = argparse.ArgumentParser(description="检查黑白名单冲突")
    parser.add_argument("paths", nargs="+", help="待检查的 dns 格式产物")
    parser.add_argument("--baseline", help="已知冲突清单，命中则不算回归")
    parser.add_argument("--max", type=int, default=0, help="允许的新增冲突上限（默认 0）")
    parser.add_argument("--report", help="把全部冲突写入该文件")
    parser.add_argument("--sample", type=int, default=20, help="输出样例条数")
    args = parser.parse_args()

    baseline = load_baseline(args.baseline)
    failed = False

    for path in args.paths:
        try:
            block, allow, mappings, important = parse(path)
        except OSError as error:
            print(f"无法读取 {path}: {error}", file=sys.stderr)
            return 2

        conflicts = sorted(block & allow)
        known = sorted(set(conflicts) & baseline)
        fresh = sorted(set(conflicts) - baseline)

        print(f"== {path}")
        print(f"   阻断 {len(block):,}  放行 {len(allow):,}  地址映射行 {mappings:,}（不参与判定）")
        print(f"   $important 阻断 {important:,}（优先于放行，不参与判定）")
        print(f"   同目标冲突 {len(conflicts):,}  其中已知 {len(known):,}  新增 {len(fresh):,}")

        if args.report:
            with open(args.report, "w", encoding="utf-8") as handle:
                handle.write("# 同目标黑白冲突（阻断 + 放行）\n")
                handle.write("\n".join(conflicts) + ("\n" if conflicts else ""))

        if len(fresh) > args.max:
            print(f"   [FAIL] 新增冲突 {len(fresh)} 条，超过上限 {args.max}：")
            for domain in fresh[: args.sample]:
                print(f"     {domain}")
            if len(fresh) > args.sample:
                print(f"     ... 其余 {len(fresh) - args.sample} 条见报告")
            failed = True
        elif fresh:
            print(f"   [WARN] 新增冲突 {len(fresh)} 条，未超过上限 {args.max}")
            for domain in fresh[: args.sample]:
                print(f"     {domain}")
        else:
            print("   [OK] 无新增冲突")

    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
