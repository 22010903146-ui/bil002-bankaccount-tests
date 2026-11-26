"""Calculate actual JMeter metrics; no measurements are synthesized."""
import csv, json, math, sys
from pathlib import Path

def summarize(path):
    with open(path, newline='', encoding='utf-8-sig') as stream:
        rows = [r for r in csv.DictReader(stream) if r['label'] in ('deposit', 'balance')]
    if not rows:
        raise ValueError('No deposit/balance samples found')
    elapsed = sorted(float(r['elapsed']) for r in rows)
    start = min(float(r['timeStamp']) for r in rows)
    end = max(float(r['timeStamp']) + float(r['elapsed']) for r in rows)
    span = (end-start)/1000
    return dict(samples=len(rows), mean_ms=round(sum(elapsed)/len(elapsed), 3),
                p90_ms=elapsed[math.ceil(.9*len(elapsed))-1],
                throughput_per_second=round(len(rows)/span, 3) if span>0 else None,
                error_percent=round(100*sum(r['success'].lower()!='true' for r in rows)/len(rows), 3),
                sample_window_seconds=round(span,3),
                percentile_method='nearest-rank', labels=['deposit','balance'])

if __name__=='__main__':
    if len(sys.argv)!=2:
        raise SystemExit('Usage: python3 scripts/performance_summary.py results.jtl')
    result = summarize(sys.argv[1])
    output=Path(sys.argv[1]).with_suffix('.summary.json')
    output.write_text(json.dumps(result,indent=2),encoding='utf-8')
    print(json.dumps(result,indent=2))
