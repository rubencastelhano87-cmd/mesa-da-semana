const fs = require('fs');
const crypto = require('crypto');
const mode = process.argv[2];
if (mode === 'seal') {
  const publicKey = fs.readFileSync('signing/recovery-public.pem');
  const key = crypto.randomBytes(32), iv = crypto.randomBytes(12);
  const cipher = crypto.createCipheriv('aes-256-gcm', key, iv);
  const payload = JSON.stringify({
    keystore: fs.readFileSync(process.env.MESA_KEYSTORE).toString('base64'),
    storePassword: process.env.MESA_STORE_PASSWORD,
    keyAlias: process.env.MESA_KEY_ALIAS,
    keyPassword: process.env.MESA_KEY_PASSWORD
  });
  const ciphertext = Buffer.concat([cipher.update(payload, 'utf8'), cipher.final()]);
  const encryptedKey = crypto.publicEncrypt({key: publicKey, oaepHash: 'sha256', padding: crypto.constants.RSA_PKCS1_OAEP_PADDING}, key);
  fs.writeFileSync('signing/mesa-release.sealed.json', JSON.stringify({
    schema: 1, algorithm: 'RSA-OAEP-SHA256+AES-256-GCM',
    key: encryptedKey.toString('base64'), iv: iv.toString('base64'),
    tag: cipher.getAuthTag().toString('base64'), ciphertext: ciphertext.toString('base64')
  }, null, 2));
} else if (mode === 'recover') {
  const privateKey = fs.readFileSync(process.argv[3]);
  const sealed = JSON.parse(fs.readFileSync('signing/mesa-release.sealed.json', 'utf8'));
  const key = crypto.privateDecrypt({key: privateKey, oaepHash: 'sha256', padding: crypto.constants.RSA_PKCS1_OAEP_PADDING}, Buffer.from(sealed.key, 'base64'));
  const decipher = crypto.createDecipheriv('aes-256-gcm', key, Buffer.from(sealed.iv, 'base64'));
  decipher.setAuthTag(Buffer.from(sealed.tag, 'base64'));
  const data = JSON.parse(Buffer.concat([decipher.update(Buffer.from(sealed.ciphertext, 'base64')), decipher.final()]).toString('utf8'));
  const destination = process.argv[4];
  fs.mkdirSync(destination, {recursive: true});
  fs.writeFileSync(destination + '/mesa-release.jks', Buffer.from(data.keystore, 'base64'), {mode: 0o600});
  delete data.keystore;
  fs.writeFileSync(destination + '/signing-private.json', JSON.stringify(data), {mode: 0o600});
  console.log('Assinatura recuperada. Não publicar os ficheiros recuperados.');
} else {
  throw Error('Expected seal or recover');
}
