#!/usr/bin/env python3
"""Publish root source metadata after its GitHub release assets exist."""
import argparse,base64,json,os,subprocess,tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--file',required=True);p.add_argument('--branch',default='main');a=p.parse_args()
repo=os.environ['GITHUB_REPOSITORY'];endpoint=f'repos/{repo}/contents/patches-bundle.json'
metadata=Path(a.file).read_bytes();data=json.loads(metadata)
# Confirm the asset itself exists before pointing Android clients at it.
subprocess.run(['gh','release','view','v'+data['version'],'--repo',repo],check=True,stdout=subprocess.DEVNULL)
result=subprocess.run(['gh','api',endpoint+'?ref='+a.branch],text=True,capture_output=True)
body={'message':'Update patch source metadata for '+data['version'],'branch':a.branch,'content':base64.b64encode(metadata).decode()}
if result.returncode==0:body['sha']=json.loads(result.stdout)['sha']
elif '404' not in result.stderr:raise RuntimeError(result.stderr)
with tempfile.TemporaryDirectory() as tmp:
    request=Path(tmp)/'request.json';request.write_text(json.dumps(body))
    subprocess.run(['gh','api','--method','PUT',endpoint,'--input',str(request)],check=True,stdout=subprocess.DEVNULL)
