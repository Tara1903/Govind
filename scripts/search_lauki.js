const https = require('https');

async function search(q) {
  const url = `https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch=${encodeURIComponent(q)}&gsrnamespace=6&gsrlimit=6&prop=imageinfo&iiprop=url|mime&iiurlwidth=960&format=json`;
  return new Promise((resolve) => {
    https.get(url, { headers: { 'User-Agent': 'GovindProduceAudit/1.0 (taras@example.com)' } }, (res) => {
      let data = '';
      res.on('data', c => data += c);
      res.on('end', () => {
        try {
          const json = JSON.parse(data);
          const pages = json.query ? Object.values(json.query.pages) : [];
          resolve(pages.filter(p => p.imageinfo && p.imageinfo[0].mime === 'image/jpeg').map(p => ({
            title: p.title,
            thumb: p.imageinfo[0].thumburl
          })));
        } catch (e) {
          resolve([]);
        }
      });
    }).on('error', () => resolve([]));
  });
}

async function run() {
  const queries = [
    'Lauki Indian vegetable',
    'Lagenaria siceraria calabash harvested',
    'Bottle gourd harvest'
  ];
  for (const q of queries) {
    const res = await search(q);
    console.log(`\n=== "${q}" ===`);
    res.forEach(r => console.log(`  - ${r.title}\n    ${r.thumb}`));
  }
}

run();
