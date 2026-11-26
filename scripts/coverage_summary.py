"""Aggregate the ORIGINAL application's service coverage from JaCoCo CSV."""
import csv, json, sys
def coverage(path):
    with open(path,newline='',encoding='utf-8-sig') as stream:
        rows=[r for r in csv.DictReader(stream) if 'service' in r['PACKAGE'].replace('/','.').split('.')]
    if not rows:
        raise ValueError('No service package rows. Do not report harness coverage as application coverage.')
    result={}
    for kind in ('BRANCH','INSTRUCTION','LINE'):
        covered=sum(int(r[kind+'_COVERED']) for r in rows)
        missed=sum(int(r[kind+'_MISSED']) for r in rows)
        result[kind.lower()]={'covered':covered,'missed':missed,
            'percent':round(100*covered/(covered+missed),2) if covered+missed else None}
    # JaCoCo instruction/line coverage is not presented as exact statement coverage.
    b=result['branch']
    result['branch_target_met']=(b['covered']+b['missed']>0 and b['covered']/(b['covered']+b['missed'])>=.8)
    return result
if __name__=='__main__':
    if len(sys.argv)!=2:raise SystemExit('Usage: python3 scripts/coverage_summary.py docs/kapsama/jacoco.csv')
    print(json.dumps(coverage(sys.argv[1]),indent=2))
