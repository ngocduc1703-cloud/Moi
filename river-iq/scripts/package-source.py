from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

root = Path(__file__).resolve().parent.parent
output = root / "dist" / "QuaSongIQ-NetBeans-Source.zip"
output.parent.mkdir(exist_ok=True)
excluded = {"target", "dist", ".build", ".git", ".idea", "private", "__pycache__"}
files = []
for path in root.rglob("*"):
    if not path.is_file() or any(part in excluded for part in path.relative_to(root).parts):
        continue
    if path.suffix in {".p12", ".jks", ".keystore", ".class", ".pyc"}:
        continue
    files.append(path)
with ZipFile(output, "w", ZIP_DEFLATED, compresslevel=9) as archive:
    for path in sorted(files):
        archive.write(path, Path("QuaSongIQ") / path.relative_to(root))
print(f"Source ready: {output} ({output.stat().st_size:,} bytes, {len(files)} files)")
