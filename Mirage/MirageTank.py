"""
Usage:
    MirageTank.py -h
    MirageTank.py -i WHITEIMG BLACKIMG -o OUTPUT [--scale=SCALE]

Arguments:
    WHITEIMG        白底下显示的图片路径
    BLACKIMG        黑底下显示的图片路径
    OUTPUT          输出文件路径, PNG格式
    SCALE           白底：黑底 放缩比例

Options:
    -h, --help      显示本帮助
    -i              输入文件(白底和黑底图片)
    -o              输出文件(png格式)
    -s              缩放比例

Examples:
    python MirageTank.py -i black.jpg white.jpg -o output.png --scale=1.2
"""

from docopt import docopt
from PIL import Image
from MTCore import MirageCore

if __name__ == '__main__':
    argv = docopt(__doc__)

    kwargs = {}

    whiteImg = Image.open(argv['WHITEIMG'])
    blackImg = Image.open(argv['BLACKIMG'])

    if argv['--scale']:
        blackScale = argv['--scale']
        blackImg = blackImg.resize((round(x * float(blackScale)) for x in blackImg.size), Image.Resampling.LANCZOS)

    MirageCore.build(whiteImg,blackImg,argv['OUTPUT'])