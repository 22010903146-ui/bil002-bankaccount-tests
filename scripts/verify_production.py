"""Verify the original production, CI, and requirement files against installation baseline."""
import hashlib,json
from pathlib import Path
def verify(root):
    baseline=json.loads((root/'docs/PRODUCTION_BASELINE.json').read_text())
    changed=[]
    for name,digest in baseline['original_files'].items():
        path=root/name
        if not path.is_file() or hashlib.sha256(path.read_bytes()).hexdigest()!=digest:
            changed.append(name)
    if changed:raise SystemExit('Original files changed: '+', '.join(changed))
    print('Original production, requirements and CI files are unchanged.')
if __name__=='__main__':verify(Path.cwd())
