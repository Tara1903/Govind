const https = require('https');

const SEARCHES = [
  { slug: 'special-punjabi-thali', q: 'North Indian Thali vegetarian' },
  { slug: 'deluxe-punjabi-thali', q: 'Indian Thali meal' },
  { slug: 'chole-bhature', q: 'Chole bhature' },
  { slug: 'gulab-jamun', q: 'Gulab jamun' },
  { slug: 'punjabi-sweet-lassi', q: 'Lassi drink' },
  { slug: 'paneer-paratha', q: 'Paneer paratha' },
  { slug: 'ginger', q: 'Zingiber officinale rhizome white background' },
  { slug: 'green-peas', q: 'Green peas pods' },
  { slug: 'green-apple', q: 'Granny Smith apple' },
  { slug: 'green-chilli', q: 'Green chili pepper' },
  { slug: 'green-grapes', q: 'Green grapes bunch' },
  { slug: 'celery', q: 'Apium graveolens celery stalks' },
  { slug: 'coriander', q: 'Coriander leaves fresh' },
  { slug: 'dates-fresh', q: 'Phoenix dactylifera fresh dates' },
  { slug: 'coconut', q: 'Cocos nucifera coconut whole' },
  { slug: 'bottle-gourd', q: 'Lagenaria siceraria calabash fruit' },
  { slug: 'zucchini', q: 'Cucurbita pepo zucchini fruit' },
  { slug: 'raw-mango', q: 'Green raw mango' },
  { slug: 'drumstick', q: 'Moringa oleifera pods' },
  { slug: 'yam', q: 'Amorphophallus paeoniifolius tuber' },
  { slug: 'raw-banana', q: 'Green cooking plantains' },
  { slug: 'cluster-beans', q: 'Cyamopsis tetragonoloba pods' },
  { slug: 'butter-beans', q: 'Phaseolus lunatus fresh pods' },
  { slug: 'custard-apple', q: 'Annona squamosa fruit' },
  { slug: 'ridge-gourd', q: 'Luffa acutangula' },
  { slug: 'wholesale-ginger-25kg', q: 'Ginger market crate' },
  { slug: 'wholesale-green-peas-20kg', q: 'Peas crate market' },
  { slug: 'wholesale-apple-shimla-20kg', q: 'Apple crate market' },
  { slug: 'wholesale-garlic-25kg', q: 'Garlic sacks market' }
];

function searchWikimedia(query) {
  return new Promise((resolve) => {
    const url = 'https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=' + encodeURIComponent(query) + '&gsrlimit=5&prop=imageinfo&iiprop=url|size|mime&format=json';
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
  for (const s of SEARCHES) {
    const res = await searchWikimedia(s.q);
    console.log(`\n=== [${s.slug}] Query: "${s.q}" ===`);
    res.forEach((r, i) => {
      console.log(`  ${i+1}. ${r.title} (${r.width}x${r.height})`);
      console.log(`     ${r.url}`);
    });
    await new Promise(r => setTimeout(r, 150));
  }
}

run();
