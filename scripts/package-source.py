from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import hashlib
import shutil

root = Path(__file__).resolve().parent.parent
dist = root / 'dist'
dist.mkdir(exist_ok=True)
for name in ('home.png', 'game.png'):
    shutil.copyfile(dist / 'screenshots' / name, root / 'docs' / name)
folders = ('core/src', 'desktop/src', 'android/src', 'scripts', 'docs')
files = [root / name for name in ('pom.xml', 'nbactions.xml', '.gitignore', 'LICENSE', 'README.md',
                                 'core/pom.xml', 'desktop/pom.xml', 'desktop/nbactions.xml', 'android/pom.xml')]
for folder in folders:
    files.extend(p for p in (root / folder).rglob('*') if p.is_file())
with ZipFile(dist / 'VuonGhepBa-NetBeans-Source.zip', 'w', ZIP_DEFLATED, compresslevel=9) as z:
    for p in sorted(set(files)):
        z.write(p, Path('VuonGhepBa') / p.relative_to(root))
shutil.copyfile(root / 'desktop/target/desktop-1.0.0.jar', dist / 'VuonGhepBa-Desktop.jar')
artifacts = ['VuonGhepBa-Setup.exe', 'VuonGhepBa-Windows-Portable.zip', 'VuonGhepBa-Android.apk',
             'VuonGhepBa-NetBeans-Source.zip', 'VuonGhepBa-Desktop.jar']
lines = []
for name in artifacts:
    p = dist / name
    digest = hashlib.sha256(p.read_bytes()).hexdigest()
    lines.append(f'{digest}  {name}')
    print(f'{name}: {p.stat().st_size:,} bytes')
(dist / 'SHA256SUMS.txt').write_text('\n'.join(lines) + '\n')
with ZipFile(dist / 'VuonGhepBa-DayDu.zip', 'w', ZIP_DEFLATED, compresslevel=1) as z:
    for name in artifacts + ['SHA256SUMS.txt']:
        z.write(dist / name, name)
    z.write(root / 'README.md', 'HUONG_DAN.md')
    z.write(root / 'docs/VALIDATION.md', 'KIEM_THU.md')
