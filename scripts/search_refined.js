const https = require('https');

const SEARCH_ITEMS = [
  { slug: 'deluxe-thali', q: 'vegetarian thali katori' },
  { slug: 'deluxe-thali2', q: 'North Indian Vegetarian Thali' },
  { slug: 'paneer-paratha', q: 'paratha butter' },
  { slug: 'ginger', q: 'Zingiber officinale rhizome' },
  { slug: 'green-grapes', q: 'green grapes table fruit' },
  { slug: 'celery', q: 'Apium graveolens celery' },
  { slug: 'dates-fresh', q: 'Dates fruit bowl' },
  { slug: 'bottle-gourd', q: 'Lagenaria siceraria fruit' },
  { slug: 'zucchini', q: 'green zucchini squash' },
  { slug: 'yam', q: 'Elephant foot yam tuber' },
  { slug: 'butter-beans', q: 'Lima bean fresh' },
  { slug: 'custard-apple', q: 'Annona squamosa' },
  { slug: 'wholesale-apple', q: 'Apples in crates' },
  { slug: 'wholesale-garlic', q: 'Garlic bulbs market' },
  { slug: 'wholesale-ginger', q: 'Ginger roots market' }
];

function searchWikimedia(query) {
  return new Promise((resolve) => {
    const url = 'https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=' + encodeURIComponent(query) + '&gsrlimit=6&prop=imageinfo&iiprop=url|size|mime&iiurlwidth=960&format=json';
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
                url: p.imageinfo[0].url,
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
  for (const s of SEARCH_ITEMS) {
    const res = await searchWikimedia(s.q);
    console.log(`\n=== [${s.slug}] Query: "${s.q}" ===`);
    res.forEach((r, i) => {
      console.log(`  ${i+1}. ${r.title} (${r.width}x${r.height})`);
      console.log(`     ${r.thumb || r.url}`);
    });
    await new Promise(r => setTimeout(r, 150));
  }
}

run();
