const https = require('https');

const queries = [
  'green grapes fruit fresh',
  'Celery stalks fresh vegetable',
  'Luffa acutangula ridge gourd fresh',
  'Annona squamosa sugar apple fruit',
  'Elephant foot yam vegetable tuber',
  'Zingiber officinale fresh ginger',
  'North Indian Punjabi thali vegetarian'
];

function searchWikimedia(query) {
  return new Promise((resolve) => {
    const url = 'https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=' + encodeURIComponent(query) + '&gsrlimit=4&prop=imageinfo&iiprop=url|size|mime&iiurlwidth=960&format=json';
    https.get(url, { headers: { 'User-Agent': 'GovindProduceAudit/1.0 (taras@example.com)' } }, (res) => {
      let data = '';
      res.on('data', c => data += c);
      res.on('end', () => {
        try {
          const json = JSON.parse(data);
          const pages = json.query ? Object.values(json.query.pages) : [];
          const results = [];
          pages.forEach(p => {
            if (p.imageinfo && p.imageinfo[0] && p.imageinfo[0].mime === 'image/jpeg') {
              results.push({
                title: p.title,
                thumb: p.imageinfo[0].thumburl,
                width: p.imageinfo[0].width,
                height: p.imageinfo[0].height
              });
            }
          });
          resolve(results);
        } catch (e) {
          resolve([]);
        }
      });
    }).on('error', () => resolve([]));
  });
}

async function run() {
  for (const q of queries) {
    const res = await searchWikimedia(q);
    console.log(`\n=== Query: "${q}" ===`);
    res.forEach((r, i) => {
      console.log(`  ${i+1}. ${r.title} (${r.width}x${r.height})`);
      console.log(`     ${r.thumb}`);
    });
  }
}

run();
