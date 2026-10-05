const crypto = require('crypto');

function getWikimediaUrl(filename, width = 960) {
  // Replace spaces with underscores
  const cleanName = filename.trim().replace(/ /g, '_');
  const md5 = crypto.createHash('md5').update(cleanName).digest('hex');
  const h1 = md5[0];
  const h2 = md5.slice(0, 2);
  const encodedName = encodeURIComponent(cleanName).replace(/%2F/g, '/');
  return `https://thumb.wikimedia.org/wikipedia/commons/thumb/${h1}/${h2}/${encodedName}/${width}px-${encodedName}`;
}

const files = [
  { slug: 'bitter-gourd', file: 'Momordica charantia 01.jpg' },
  { slug: 'ash-gourd', file: 'Wax gourd.jpg' },
  { slug: 'ash-gourd-alt', file: 'Benincasa hispida fruit.jpg' },
  { slug: 'muskmelon', file: 'Cantaloupe and cross section.jpg' },
  { slug: 'sweet-potato', file: 'Sweet potato tubers.jpg' },
  { slug: 'radish', file: 'Daikon radish.jpg' },
  { slug: 'spring-onion', file: 'Scallions.jpg' },
  { slug: 'pomegranate', file: 'Pomegranate02 edit.jpg' },
  { slug: 'boondi-raita', file: 'Boondi Raita.jpg' },
  { slug: 'rajma-chawal-bowl', file: 'Rajma Chawal.jpg' },
  { slug: 'gobhi-paratha', file: 'Gobi Paratha.JPG' },
  { slug: 'butter-naan', file: 'Butter naan.JPG' },
  { slug: 'dal-makhani-jeera-rice', file: 'Dal Makhani.jpg' },
  { slug: 'wholesale-onion-50kg', file: 'Red Onions.jpg' },
  { slug: 'wholesale-garlic-25kg', file: 'Garlic bulbs.jpg' },
  { slug: 'wholesale-ginger-25kg', file: 'Ginger roots.jpg' },
  { slug: 'wholesale-green-peas-20kg', file: 'Green peas.jpg' },
  { slug: 'wholesale-banana-15kg', file: 'Bananas.jpg' }
];

async function run() {
  console.log('Testing computed Wikimedia URLs...');
  for (const item of files) {
    const url = getWikimediaUrl(item.file);
    try {
      const res = await fetch(url, {
        headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) GovindAudit/1.0' },
        signal: AbortSignal.timeout(5000)
      });
      if (res.ok) {
        const buf = await res.arrayBuffer();
        console.log(`[OK ${res.status}] ${item.slug} (${buf.byteLength} bytes) -> ${url}`);
      } else {
        console.log(`[FAIL ${res.status}] ${item.slug} (${item.file})`);
      }
    } catch (e) {
      console.log(`[ERR] ${item.slug}: ${e.message}`);
    }
  }
}
run();
