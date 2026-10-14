from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import shutil
import hashlib

root=Path(__file__).resolve().parent.parent
dist=root/'dist'
for name in ['home.png','game.png']:
    shutil.copyfile(dist/'screenshots'/name,root/'docs'/name)
files=[root/name for name in ['pom.xml','nbactions.xml','.gitignore','LICENSE','README.md','core/pom.xml','desktop/pom.xml','desktop/nbactions.xml','android/pom.xml']]
for folder in ['core/src','desktop/src','android/src','scripts','docs']:
    files.extend(p for p in (root/folder).rglob('*') if p.is_file())
with ZipFile(dist/'DaiLoZombie-NetBeans-Source.zip','w',ZIP_DEFLATED,compresslevel=9) as z:
    for p in sorted(set(files)): z.write(p,Path('DaiLoZombie')/p.relative_to(root))
shutil.copyfile(root/'desktop/target/desktop-1.0.0.jar',dist/'DaiLoZombie-Desktop.jar')
names=['DaiLoZombie-Setup.exe','DaiLoZombie-Windows-Portable.zip','DaiLoZombie-Android.apk','DaiLoZombie-NetBeans-Source.zip','DaiLoZombie-Desktop.jar']
lines=[]
for name in names:
    p=dist/name
    lines.append(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+name)
    print(f'{name}: {p.stat().st_size:,} bytes')
(dist/'SHA256SUMS.txt').write_text('\n'.join(lines)+'\n')
