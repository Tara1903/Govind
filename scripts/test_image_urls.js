const https = require('https');
const http = require('http');

const URLS = {
  'mushroom': 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/01/ChampignonMushroom.jpg/800px-ChampignonMushroom.jpg',
  'bitter-gourd': 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/ad/Momordica_charantia_01.jpg/800px-Momordica_charantia_01.jpg',
  'ash-gourd': 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f7/Winter_Melon.JPG/800px-Winter_Melon.JPG',
  'muskmelon': 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/28/Cantaloupe_and_cross_section.jpg/800px-Cantaloupe_and_cross_section.jpg',
  'mustard-greens': 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Brassica_juncea_var._crispifolia_1.JPG/800px-Brassica_juncea_var._crispifolia_1.JPG',
  'sweet-potato': 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Sweet_potato_tubers.jpg/800px-Sweet_potato_tubers.jpg',
  'radish': 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/Daikon_radish.jpg/800px-Daikon_radish.jpg',
  'spring-onion': 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/Scallions.jpg/800px-Scallions.jpg',
  'sorrel-leaves': 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Oseille_commune.jpg/800px-Oseille_commune.jpg',
  'pomegranate': 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Pomegranate02_edit.jpg/800px-Pomegranate02_edit.jpg',
  'boondi-raita': 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/7f/Raita_with_Boondi.jpg/800px-Raita_with_Boondi.jpg',
  'rajma-chawal-bowl': 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f7/Rajma_Chawal_-_Delhi.jpg/800px-Rajma_Chawal_-_Delhi.jpg',
  'gobhi-paratha': 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Gobi_Paratha.JPG/800px-Gobi_Paratha.JPG',
  'butter-naan': 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Butter_naan.JPG/800px-Butter_naan.JPG',
  'dal-makhani-jeera-rice': 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Dal_Makhani_with_rice.jpg/800px-Dal_Makhani_with_rice.jpg',
  'wholesale-potato-50kg': 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/ab/Patates.jpg/800px-Patates.jpg',
  'wholesale-onion-50kg': 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Red_Onions_in_mesh_bag.JPG/800px-Red_Onions_in_mesh_bag.JPG',
  'wholesale-tomato-25kg': 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/89/Tomato_je.jpg/800px-Tomato_je.jpg',
  'wholesale-garlic-25kg': 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/27/Garlic_bulbs_market.jpg/800px-Garlic_bulbs_market.jpg',
  'wholesale-ginger-25kg': 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Ginger_roots_market.jpg/800px-Ginger_roots_market.jpg',
  'wholesale-green-peas-20kg': 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b8/Green_peas_in_market.jpg/800px-Green_peas_in_market.jpg',
  'wholesale-apple-shimla-20kg': 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Red_Apple.jpg/800px-Red_Apple.jpg',
  'wholesale-banana-15kg': 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Bananas.jpg/800px-Bananas.jpg'
};

async function testFetch(slug, url) {
  try {
    const res = await fetch(url, {
      headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) GovindAudit/1.0' },
      signal: AbortSignal.timeout(10000)
    });
    if (res.ok) {
      const buf = await res.arrayBuffer();
      console.log(`[OK] ${slug}: HTTP ${res.status}, ${buf.byteLength} bytes`);
      return true;
    } else {
      console.log(`[FAIL] ${slug}: HTTP ${res.status}`);
      return false;
    }
  } catch (e) {
    console.log(`[ERROR] ${slug}: ${e.message}`);
    return false;
  }
}

async function run() {
  console.log('Testing image URLs...');
  for (const [slug, url] of Object.entries(URLS)) {
    await testFetch(slug, url);
  }
}
run();
