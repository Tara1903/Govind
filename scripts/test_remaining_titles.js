const https = require('https');
const fs = require('fs');
const path = require('path');

const testTitles = [
  { name: 'test_zucchini_basket.jpg', title: 'File:Zucchini in basket 2021 G1.jpg' },
  { name: 'test_celery_bunch.jpg', title: 'File:Pascal celery.jpg' },
  { name: 'test_celery_market.jpg', title: 'File:Apium graveolens, Celery, Stalks.jpg' },
  { name: 'test_lauki1.jpg', title: 'File:Lagenaria siceraria baby fruit.jpeg' },
  { name: 'test_lauki2.jpg', title: 'File:Edible immature Lagenaria siceraria fruits, longissima snake gourds.jpg' },
  { name: 'test_garlic_sack.jpg', title: 'File:Garlic in a net bag.jpg' },
  { name: 'test_garlic_market.jpg', title: 'File:Bulbs of Garlic Displayed For Sale At A Local Market.jpg' }
];

const destDir = path.resolve(__dirname, '../temp_candidates');

function fetchThumb(fileTitle) {
  const url = `https://commons.wikimedia.org/w/api.php?action=query&titles=${encodeURIComponent(fileTitle)}&prop=imageinfo&iiprop=url&iiurlwidth=960&format=json`;
  return new Promise((resolve) => {
    https.get(url, { headers: { 'User-Agent': 'GovindProduceAudit/1.0 (taras@example.com)' } }, (res) => {
      let data = '';
      res.on('data', c => data += c);
      res.on('end', () => {
        try {
          const json = JSON.parse(data);
          const p = Object.values(json.query.pages)[0];
          if (p.imageinfo && p.imageinfo[0]) {
            resolve(p.imageinfo[0].thumburl || p.imageinfo[0].url);
          } else {
            resolve(null);
          }
        } catch (e) {
          resolve(null);
        }
      });
    }).on('error', () => resolve(null));
  });
}

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
  for (const it of testTitles) {
    const thumbUrl = await fetchThumb(it.title);
    if (!thumbUrl) {
      console.log(`[NOT FOUND] ${it.title}`);
      continue;
    }
    const dest = path.join(destDir, it.name);
    try {
      await download(thumbUrl, dest);
      console.log(`[OK] ${it.name} (${fs.statSync(dest).size} bytes)`);
    } catch (e) {
      console.log(`[FAIL] ${it.name}: ${e.message}`);
    }
  }
}

run();
