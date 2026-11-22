"""Create a compact 246px QR only for an actual HTTPS APK URL."""
import sys
from urllib.parse import urlparse
import qrcode
from PIL import Image
url, destination = sys.argv[1:]
parsed = urlparse(url)
if parsed.scheme != 'https' or not parsed.path.endswith('.apk'):
    raise SystemExit('É necessária a ligação HTTPS real do APK.')
qr = qrcode.QRCode(error_correction=qrcode.constants.ERROR_CORRECT_M, border=4, box_size=1)
qr.add_data(url)
qr.make(fit=True)
modules = qr.modules_count + 8
scale = 246 // modules
if scale < 2:
    raise SystemExit('Ligação demasiado longa para QR compacto legível.')
qr.box_size = scale
image = qr.make_image(fill_color='black', back_color='white').convert('RGB')
canvas = Image.new('RGB', (246, 246), 'white')
canvas.paste(image, ((246-image.width)//2, (246-image.height)//2))
canvas.save(destination)
