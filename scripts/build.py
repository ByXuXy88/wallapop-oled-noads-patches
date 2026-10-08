#!/usr/bin/env python3
"""Compile a JVM + Android MPP against the pinned public Morphe runtime."""
import argparse,datetime,hashlib,json,os,re,shutil,subprocess,urllib.request,zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]

def sha(path):
    with path.open('rb') as f:
        return hashlib.file_digest(f,'sha256').hexdigest()

def run(args):
    subprocess.run([str(x) for x in args],cwd=ROOT,check=True)

def main():
    p=argparse.ArgumentParser();p.add_argument('--version',default='0.1.0');p.add_argument('--repository',default=os.environ.get('GITHUB_REPOSITORY','ByXuXy88/wallapop-oled-noads-patches'));a=p.parse_args()
    if not re.fullmatch(r'\d+\.\d+\.\d+(?:-[A-Za-z0-9.-]+)?',a.version):p.error('Invalid version')
    if not re.fullmatch(r'[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+',a.repository):p.error('Invalid repository')
    lock=json.loads((ROOT/'toolchain.lock.json').read_text());tc=ROOT/'.toolchain';tc.mkdir(exist_ok=True)
    for dep in lock['dependencies']:
        path=tc/dep['name']
        if not path.exists() or sha(path)!=dep['sha256']:
            tmp=path.with_suffix('.part')
            with urllib.request.urlopen(dep['url'],timeout=180) as response,tmp.open('wb') as f:shutil.copyfileobj(response,f)
            if sha(tmp)!=dep['sha256']:raise RuntimeError('Dependency hash mismatch: '+dep['name'])
            tmp.replace(path)
    gradle=tc/'gradle-9.7.0'
    if not gradle.exists():
        with zipfile.ZipFile(tc/'gradle.zip') as z:
            # Only the Kotlin compiler and supporting library jars are needed.
            for name in z.namelist():
                if name.startswith('gradle-9.7.0/lib/') and not name.endswith('/'):z.extract(name,tc)
    java_home=os.environ.get('JAVA_HOME');java=Path(java_home)/'bin/java' if java_home else Path(shutil.which('java') or '')
    if not java.is_file():raise RuntimeError('Java 21 required')
    java_home=Path(java_home) if java_home else java.resolve().parents[1]
    version=subprocess.run([str(java),'-version'],text=True,capture_output=True,check=True).stderr
    if not re.search(r'version "21[.\"]',version):raise RuntimeError('Use JDK 21 (JAVA_HOME)')
    build=ROOT/'build';classes=build/'classes';dex=build/'dex';out=build/'release'
    for directory in [classes,dex,out]:
        if directory.exists():shutil.rmtree(directory)
        directory.mkdir(parents=True)
    sources=sorted((ROOT/'src/main/kotlin').rglob('*.kt'))
    run([java,'-Xmx2g','-cp',str(gradle/'lib/*'),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-classpath',tc/'desktop.jar','-jvm-target','11','-Xlambdas=class','-Xstring-concat=inline','-d',classes,*sources])
    timestamp=int(datetime.datetime.now(datetime.timezone.utc).timestamp()*1000)
    manifest={'Manifest-Version':'1.0','Name':'Wallapop OLED and No Ads','Description':'Pure black dark mode and advertising gates for Wallapop','Version':a.version,'Timestamp':str(timestamp),'Source':f'https://github.com/{a.repository}','Author':a.repository.split('/')[0],'License':'GPLv3','Patcher-Version':lock['patcher_version']}
    # JAR manifest continuation lines, including long repository URLs.
    lines=[]
    for key,value in manifest.items():
        line=f'{key}: {value}'
        while len(line.encode('utf-8'))>70:
            prefix=line[:70]
            while len(prefix.encode('utf-8'))>70:prefix=prefix[:-1]
            lines.append(prefix);line=' '+line[len(prefix):]
        lines.append(line)
    jar=build/'patches-jvm.jar'
    with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
        z.writestr('META-INF/MANIFEST.MF','\r\n'.join(lines)+'\r\n\r\n')
        for f in sorted(classes.rglob('*')):
            if f.is_file():z.write(f,f.relative_to(classes).as_posix())
        z.write(ROOT/'LICENSE','META-INF/LICENSE')
        z.write(ROOT/'NOTICE.md','META-INF/NOTICE.md')
    run([java,'-Xmx2g','-cp',tc/'r8.jar','com.android.tools.r8.D8','--release','--min-api',str(lock['min_api']),'--lib',java_home,'--classpath',tc/'desktop.jar','--output',dex,jar])
    bundle=out/f'patches-{a.version}.mpp'
    with zipfile.ZipFile(jar) as original,zipfile.ZipFile(bundle,'w',zipfile.ZIP_DEFLATED) as z:
        for info in original.infolist():z.writestr(info,original.read(info.filename))
        for f in sorted(dex.glob('*.dex')):z.write(f,f.name)
    with zipfile.ZipFile(bundle) as z:
        if z.testzip() is not None or 'classes.dex' not in z.namelist():raise RuntimeError('Invalid MPP')
    run([java,'-jar',tc/'desktop.jar','list-patches','--patches',bundle,'--with-packages','--with-versions','--out',out/'patches-list.txt'])
    metadata={'created_at':datetime.datetime.now(datetime.timezone.utc).strftime('%Y-%m-%dT%H:%M:%S'),'description':(ROOT/'CHANGELOG.md').read_text(),'download_url':f'https://github.com/{a.repository}/releases/download/v{a.version}/{bundle.name}','signature_download_url':'','version':a.version}
    (out/'patches-bundle.json').write_text(json.dumps(metadata,indent=2,ensure_ascii=False)+'\n')
    files=[bundle,out/'patches-bundle.json',out/'patches-list.txt']
    (out/'SHA256SUMS.txt').write_text(''.join(f'{sha(f)}  {f.name}\n' for f in files))
    print('Built:',bundle)

if __name__=='__main__':main()
