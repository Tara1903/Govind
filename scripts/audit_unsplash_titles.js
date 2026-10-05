const https = require('https');
const fs = require('fs');
const path = require('path');

const sources = JSON.parse(fs.readFileSync(path.join(__dirname, '../docs/baseline/phase-2-image-sources.json'), 'utf8'));

function fetchPage(url) {
  return new Promise((resolve) => {
    https.get(url, { headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)' } }, (res) => {
      let data = '';
      res.on('data', c => data += c);
      res.on('end', () => resolve({ status: res.statusCode, data }));
    }).on('error', (err) => resolve({ status: 500, error: err.message }));
  });
}

async function run() {
  console.log(`Checking ${sources.length} sources...`);
  for (const s of sources) {
    const m = s.source_url.match(/photo-([a-zA-Z0-9_-]+)/);
    if (!m) {
      console.log(`[NON-UNSPLASH] ${s.slug} -> ${s.source_url.slice(0, 60)}`);
      continue;
    }
    const photoId = m[1];
    const pageUrl = `https://unsplash.com/photos/${photoId}`;
    const res = await fetchPage(pageUrl);
    let title = 'N/A';
    if (res.data) {
      const tm = res.data.match(/<title>([^<]+)<\/title>/);
      if (tm) title = tm[1];
    }
    console.log(`[${s.slug}] (${s.product_name}) -> Title: ${title}`);
    await new Promise(r => setTimeout(r, 200));
  }
}

run();
