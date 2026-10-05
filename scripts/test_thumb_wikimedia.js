const testList = [
  // Bitter gourd (Momordica charantia)
  { slug: 'bitter-gourd', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ad/Momordica_charantia_01.jpg/960px-Momordica_charantia_01.jpg' },
  // Ash gourd / Winter melon (Benincasa hispida)
  { slug: 'ash-gourd', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f7/Winter_Melon.JPG/960px-Winter_Melon.JPG' },
  // Cantaloupe / Muskmelon
  { slug: 'muskmelon', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/28/Cantaloupe_and_cross_section.jpg/960px-Cantaloupe_and_cross_section.jpg' },
  // Sweet potato
  { slug: 'sweet-potato', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4e/Sweet_potato_tubers.jpg/960px-Sweet_potato_tubers.jpg' },
  // White Radish / Daikon
  { slug: 'radish', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/02/Daikon_radish.jpg/960px-Daikon_radish.jpg' },
  // Spring onion / Scallions
  { slug: 'spring-onion', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/87/Scallions.jpg/960px-Scallions.jpg' },
  // Sorrel leaves
  { slug: 'sorrel-leaves', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e0/Oseille_commune.jpg/960px-Oseille_commune.jpg' },
  // Pomegranate
  { slug: 'pomegranate', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7b/Pomegranate02_edit.jpg/960px-Pomegranate02_edit.jpg' },
  // Boondi Raita
  { slug: 'boondi-raita', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7f/Raita_with_Boondi.jpg/960px-Raita_with_Boondi.jpg' },
  // Rajma Chawal
  { slug: 'rajma-chawal-bowl', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f7/Rajma_Chawal_-_Delhi.jpg/960px-Rajma_Chawal_-_Delhi.jpg' },
  // Gobhi Paratha
  { slug: 'gobhi-paratha', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4b/Gobi_Paratha.JPG/960px-Gobi_Paratha.JPG' },
  // Butter Naan
  { slug: 'butter-naan', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/Butter_naan.JPG/960px-Butter_naan.JPG' },
  // Dal Makhani Rice
  { slug: 'dal-makhani-jeera-rice', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/91/Dal_Makhani_with_rice.jpg/960px-Dal_Makhani_with_rice.jpg' },
  // Wholesale Potato Sacks
  { slug: 'wholesale-potato-50kg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ab/Patates.jpg/960px-Patates.jpg' },
  // Wholesale Onion Mesh
  { slug: 'wholesale-onion-50kg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/15/Red_Onions_in_mesh_bag.JPG/960px-Red_Onions_in_mesh_bag.JPG' },
  // Wholesale Tomato Crates
  { slug: 'wholesale-tomato-25kg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/89/Tomato_je.jpg/960px-Tomato_je.jpg' },
  // Wholesale Garlic Market
  { slug: 'wholesale-garlic-25kg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/27/Garlic_bulbs_market.jpg/960px-Garlic_bulbs_market.jpg' },
  // Wholesale Ginger Roots
  { slug: 'wholesale-ginger-25kg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7b/Ginger_roots_market.jpg/960px-Ginger_roots_market.jpg' },
  // Wholesale Green Peas
  { slug: 'wholesale-green-peas-20kg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b8/Green_peas_in_market.jpg/960px-Green_peas_in_market.jpg' },
  // Wholesale Apple Crates
  { slug: 'wholesale-apple-shimla-20kg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/15/Red_Apple.jpg/960px-Red_Apple.jpg' },
  // Wholesale Banana Bunch
  { slug: 'wholesale-banana-15kg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/Bananas.jpg/960px-Bananas.jpg' }
];

async function run() {
  console.log('Testing thumb.wikimedia.org paths...');
  for (const item of testList) {
    try {
      const res = await fetch(item.url, {
        headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)' },
        signal: AbortSignal.timeout(6000)
      });
      if (res.ok) {
        const buf = await res.arrayBuffer();
        console.log(`[OK ${res.status}] ${item.slug}: ${buf.byteLength} bytes`);
      } else {
        console.log(`[FAIL ${res.status}] ${item.slug}`);
      }
    } catch (e) {
      console.log(`[ERR] ${item.slug}: ${e.message}`);
    }
  }
}
run();
