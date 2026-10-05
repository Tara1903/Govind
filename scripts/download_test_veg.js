const https = require('https');
const fs = require('fs');
const path = require('path');

const list = [
  { name: 'test_zucchini_green.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/dd/TwinZucchini.jpg/960px-TwinZucchini.jpg' },
  { name: 'test_celery1.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c9/Apium_graveolens_RF.jpg/960px-Apium_graveolens_RF.jpg' },
  { name: 'test_celery2.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bc/Celery_%28177029685%29.jpeg/960px-Celery_%28177029685%29.jpeg' }
];

const destDir = path.resolve(__dirname, '../temp_candidates');

function download(url, dest) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'GovindProduceAudit/1.0 (taras@example.com)' } }, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return download(res.headers.location, dest).then(resolve).catch(reject);
      }
      if (res.statusCode !== 200) return reject(new Error(`Status ${res.statusCode}`));
      const file = fs.createWriteStream(dest);
      res.pipe(file);
      file.on('finish', () => file.close(resolve));
    }).on('error', reject);
  });
}

async function run() {
  for (const it of list) {
    const dest = path.join(destDir, it.name);
    try {
      await download(it.url, dest);
      console.log(`[OK] ${it.name} (${fs.statSync(dest).size} bytes)`);
    } catch (e) {
      console.error(`[FAIL] ${it.name}: ${e.message}`);
    }
  }
}

run();
