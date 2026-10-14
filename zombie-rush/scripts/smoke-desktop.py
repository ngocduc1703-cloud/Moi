"""Open a real Swing window under an Xvfb server that has signalled readiness."""
import os
import select
import subprocess
from pathlib import Path

root=Path(__file__).resolve().parent.parent
tools=Path(os.environ.get('TOOLS_DIR','/workspace/.tile-tools'))
read_fd,write_fd=os.pipe()
server=None
try:
    server=subprocess.Popen([str(tools/'native/usr/bin/Xvfb'),'-displayfd',str(write_fd),'-screen','0','1200x800x24','-ac','-nolisten','tcp'],pass_fds=(write_fd,),stdout=subprocess.DEVNULL,stderr=subprocess.PIPE)
    os.close(write_fd);write_fd=-1
    if not select.select([read_fd],[],[],10)[0]:raise RuntimeError('Xvfb did not become ready')
    display=os.read(read_fd,100).decode().strip()
    if not display:raise RuntimeError('Xvfb failed to select a display')
    env=os.environ.copy();env['DISPLAY']=':'+display
    subprocess.run([str(tools/'jdk/bin/java'),'-jar',str(root/'desktop/target/desktop-1.0.0.jar'),'--smoke-test'],env=env,check=True,timeout=30)
finally:
    os.close(read_fd)
    if write_fd!=-1:os.close(write_fd)
    if server is not None:
        server.terminate();server.wait(timeout=5)
