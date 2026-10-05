async function getMediaWikiImageUrl(query) {
  const apiUrl = `https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch=${encodeURIComponent(query)}&gsrnamespace=6&gsrlimit=1&prop=imageinfo&iiprop=url&iiurlwidth=800&format=json`;
  const res = await fetch(apiUrl, {
    headers: { 'User-Agent': 'GovindProduceAudit/1.0 (taras@example.com)' }
  });
  if (!res.ok) throw new Error(`API HTTP ${res.status}`);
  const data = await res.json();
  if (!data.query || !data.query.pages) return null;
  const page = Object.values(data.query.pages)[0];
  if (!page.imageinfo || !page.imageinfo[0]) return null;
  return page.imageinfo[0].thumburl || page.imageinfo[0].url;
}

const queries = [
  'Momordica charantia bitter gourd',
  'Benincasa hispida ash gourd',
  'Cantaloupe muskmelon fruit',
  'Ipomoea batatas sweet potato',
  'Raphanus sativus white radish',
  'Allium fistulosum scallion',
  'Punica granatum pomegranate',
  'Rumex acetosa sorrel',
  'Boondi raita',
  'Rajma chawal',
  'Gobi paratha',
  'Butter naan',
  'Dal makhani',
  'Garlic bulbs crate market',
  'Ginger roots market'
];

async function run() {
  for (const q of queries) {
    try {
      const url = await getMediaWikiImageUrl(q);
      console.log(`[QUERY: "${q}"] -> ${url}`);
    } catch (e) {
      console.log(`[ERR: "${q}"]: ${e.message}`);
    }
  }
}
run();
