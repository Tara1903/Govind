const https = require('https');
const fs = require('fs');
const path = require('path');

const list = [
  { name: 'test_celery_market.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2b/Liat_Portal_for_Foodie_Disorder_-_Celery_from_San_Francisco_Farmers_Market.jpg/960px-Liat_Portal_for_Foodie_Disorder_-_Celery_from_San_Francisco_Farmers_Market.jpg' },
  { name: 'test_garlic_market_france.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0b/Garlic_for_sale_in_a_market_in_France.jpg/960px-Garlic_for_sale_in_a_market_in_France.jpg' }
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
      console.log(`[FAIL] ${it.name}: ${e.message}`);
    }
  }
}

run();
