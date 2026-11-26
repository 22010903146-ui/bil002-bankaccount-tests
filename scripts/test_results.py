"""Export genuine Surefire outcomes and reconcile known case IDs."""
from pathlib import Path
import csv, json, sys, xml.etree.ElementTree as ET
def collect(directory):
    rows=[]
    for path in sorted(Path(directory).glob('TEST-*.xml')):
        root=ET.parse(path).getroot()
        for case in root.iter('testcase'):
            problem=case.find('failure')
            status='PASS'
            if problem is not None:status='FAIL'
            elif case.find('error') is not None:
                status='ERROR';problem=case.find('error')
            elif case.find('skipped') is not None:status='SKIP'
            rows.append({'class':case.get('classname',''),'method':case.get('name',''),
                         'status':status,'seconds':case.get('time',''),
                         'message':problem.get('message','') if problem is not None else ''})
    if not rows:raise ValueError('No Surefire XML results found')
    return rows
if __name__=='__main__':
    directory=sys.argv[1] if len(sys.argv)>1 else 'target/surefire-reports'
    rows=collect(directory)
    destination=Path('docs/execution-results.csv');destination.parent.mkdir(parents=True,exist_ok=True)
    with destination.open('w',newline='',encoding='utf-8') as stream:
        writer=csv.DictWriter(stream,fieldnames=list(rows[0]));writer.writeheader();writer.writerows(rows)
    print(json.dumps({s:sum(r['status']==s for r in rows) for s in ['PASS','FAIL','ERROR','SKIP']},indent=2))
