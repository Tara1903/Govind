const https = require('https');
const fs = require('fs');
const path = require('path');

const url = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e2/Fresh_Bottle_Gourd_%28Lauki%29_from_Home_Garden.jpg/960px-Fresh_Bottle_Gourd_%28Lauki%29_from_Home_Garden.jpg';
const dest = path.resolve(__dirname, '../temp_candidates/test_lauki_real.jpg');

function download(u, d) {
  return new Promise((resolve, reject) => {
    https.get(u, { headers: { 'User-Agent': 'GovindProduceAudit/1.0 (taras@example.com)' } }, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return download(res.headers.location, d).then(resolve).catch(reject);
      }
      if (res.statusCode !== 200) return reject(new Error(`Status ${res.statusCode}`));
      const file = fs.createWriteStream(d);
      res.pipe(file);
      file.on('finish', () => file.close(resolve));
    }).on('error', reject);
  });
}

download(url, dest).then(() => console.log('Downloaded lauki: ' + fs.statSync(dest).size + ' bytes')).catch(console.error);
