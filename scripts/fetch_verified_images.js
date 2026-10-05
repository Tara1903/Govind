const https = require('https');
const fs = require('fs');
const path = require('path');

const TARGETS = [
  // Kitchen
  { slug: 'special-punjabi-thali', exp: 'kitchen', file: 'File:North Indian veg thali.jpg' },
  { slug: 'deluxe-punjabi-thali', exp: 'kitchen', file: 'File:My traditional Indian thali meal (48625245542).jpg' },
  { slug: 'chole-bhature', exp: 'kitchen', file: 'File:Chole Bhature 1.jpg' },
  { slug: 'gulab-jamun', exp: 'kitchen', file: 'File:Gulab Jamun as a Diwali Sweet.jpg' },
  { slug: 'punjabi-sweet-lassi', exp: 'kitchen', file: 'File:Lassi 1.jpg' },
  { slug: 'paneer-paratha', exp: 'kitchen', file: 'File:Paratha Sabji MA01.jpg' },

  // Fresh
  { slug: 'ginger', exp: 'fresh', query: 'Zingiber officinale rhizome market' },
  { slug: 'green-peas', exp: 'fresh', file: 'File:Green pea pods.jpg' },
  { slug: 'green-apple', exp: 'fresh', file: 'File:Granny Smith Apples.jpg' },
  { slug: 'green-chilli', exp: 'fresh', file: 'File:Green chili pepper.jpg' },
  { slug: 'green-grapes', exp: 'fresh', file: 'File:A bunch of grapes.jpg' },
  { slug: 'celery', exp: 'fresh', query: 'Apium graveolens celery stalks fresh' },
  { slug: 'coriander', exp: 'fresh', file: 'File:Bunches of coriander leaves.jpg' },
  { slug: 'dates-fresh', exp: 'fresh', file: 'File:Kajur.jpg' },
  { slug: 'coconut', exp: 'fresh', file: 'File:Coconuts - single and cracked open.jpg' },
  { slug: 'bottle-gourd', exp: 'fresh', file: 'File:Bottle Gourd - Flickr - nekonomania.jpg' },
  { slug: 'zucchini', exp: 'fresh', file: 'File:Calabacín, Múnich, Alemania, 2013-03-30, DD 01.JPG' },
  { slug: 'raw-mango', exp: 'fresh', file: 'File:Raw mango 2.jpg' },
  { slug: 'drumstick', exp: 'fresh', file: 'File:Moringa oleifera drumstick pods.JPG' },
  { slug: 'yam', exp: 'fresh', file: 'File:Amorphophallus paeoniifolius Farmingage072.jpg' },
  { slug: 'raw-banana', exp: 'fresh', file: 'File:Plantains.jpg' },
  { slug: 'cluster-beans', exp: 'fresh', file: 'File:Cluster bean cluster.jpg' },
  { slug: 'butter-beans', exp: 'fresh', query: 'Phaseolus lunatus seeds pods' },
  { slug: 'custard-apple', exp: 'fresh', file: 'File:Sugar apple with cross section.jpg' },
  { slug: 'ridge-gourd', exp: 'fresh', file: 'File:Luffa acutangula1.jpg' },

  // Wholesale
  { slug: 'wholesale-ginger-25kg', exp: 'wholesale', file: 'File:Münster, Wochenmarkt -- 2015 -- 7410.jpg' },
  { slug: 'wholesale-green-peas-20kg', exp: 'wholesale', file: 'File:Erbsen-Frischmarkt-Poolkiste-Josef Schlaghecken.jpg' },
  { slug: 'wholesale-apple-shimla-20kg', exp: 'wholesale', query: 'Apples in crate market' },
  { slug: 'wholesale-garlic-25kg', exp: 'wholesale', query: 'Garlic bulbs market crate' }
];

const destDir = path.resolve(__dirname, '../temp_candidates');
if (!fs.existsSync(destDir)) fs.mkdirSync(destDir, { recursive: true });

function fetchJson(url) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'GovindProduceAudit/1.0 (taras@example.com)' } }, (res) => {
      let data = '';
      res.on('data', c => data += c);
      res.on('end', () => {
        try {
          resolve(JSON.parse(data));
        } catch (e) {
          reject(e);
        }
      });
    }).on('error', reject);
  });
}

function downloadFile(url, dest) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'GovindProduceAudit/1.0 (taras@example.com)' } }, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return downloadFile(res.headers.location, dest).then(resolve).catch(reject);
      }
      if (res.statusCode !== 200) {
        return reject(new Error(`HTTP ${res.statusCode}`));
      }
      const file = fs.createWriteStream(dest);
      res.pipe(file);
      file.on('finish', () => file.close(resolve));
    }).on('error', reject);
  });
}

async function getImageUrl(target) {
  let apiUrl = '';
  if (target.file) {
    apiUrl = `https://commons.wikimedia.org/w/api.php?action=query&titles=${encodeURIComponent(target.file)}&prop=imageinfo&iiprop=url&iiurlwidth=960&format=json`;
  } else {
    apiUrl = `https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch=${encodeURIComponent(target.query)}&gsrnamespace=6&gsrlimit=1&prop=imageinfo&iiprop=url&iiurlwidth=960&format=json`;
  }

  const json = await fetchJson(apiUrl);
  if (!json.query || !json.query.pages) return null;
  const page = Object.values(json.query.pages)[0];
  if (!page.imageinfo || !page.imageinfo[0]) return null;
  return {
    title: page.title,
    url: page.imageinfo[0].thumburl || page.imageinfo[0].url
  };
}

async function run() {
  console.log(`Starting fetch for ${TARGETS.length} targets...`);
  for (const t of TARGETS) {
    try {
      const info = await getImageUrl(t);
      if (!info) {
        console.error(`[NOT FOUND] ${t.slug}`);
        continue;
      }
      const outPath = path.join(destDir, `${t.exp}_${t.slug}.jpg`);
      await downloadFile(info.url, outPath);
      const stat = fs.statSync(outPath);
      console.log(`[SAVED] ${t.slug} (${t.exp}) -> ${stat.size} bytes (source: "${info.title}")`);
    } catch (err) {
      console.error(`[ERROR] ${t.slug}: ${err.message}`);
    }
    await new Promise(r => setTimeout(r, 200));
  }
  console.log('Finished downloading candidate images!');
}

run();
