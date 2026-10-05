const { createClient } = require('@supabase/supabase-js');
const path = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '../admin/.env.local') });

const supabase = createClient(
  process.env.NEXT_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL,
  process.env.SUPABASE_SERVICE_ROLE_KEY
);

async function run() {
  const { data, error } = await supabase
    .from('products')
    .select('id, name, slug, experience_type, image_url')
    .order('experience_type')
    .order('name');

  if (error) {
    console.error('Error fetching products:', error);
    return;
  }

  console.log(`Total products in database: ${data.length}`);

  const duplicateMap = {};
  data.forEach((p) => {
    if (!duplicateMap[p.image_url]) {
      duplicateMap[p.image_url] = [];
    }
    duplicateMap[p.image_url].push({ name: p.name, exp: p.experience_type, slug: p.slug });
  });

  console.log('\n=== DUPLICATE IMAGE URLS ACROSS ALL EXPERIENCES ===');
  let dupCount = 0;
  for (const [url, prods] of Object.entries(duplicateMap)) {
    if (prods.length > 1) {
      dupCount++;
      console.log(`\nURL: ${url}`);
      prods.forEach((p) => console.log(`   - [${p.exp}] ${p.name} (${p.slug})`));
    }
  }
  console.log(`\nTotal duplicate URLs: ${dupCount}`);

  console.log('\n=== KITCHEN PRODUCTS (Total: ' + data.filter(p => p.experience_type === 'KITCHEN').length + ') ===');
  data
    .filter((p) => p.experience_type === 'KITCHEN')
    .forEach((p) => console.log(`- ${p.name} (${p.slug}): ${p.image_url}`));

  console.log('\n=== WHOLESALE PRODUCTS (Total: ' + data.filter(p => p.experience_type === 'WHOLESALE').length + ') ===');
  data
    .filter((p) => p.experience_type === 'WHOLESALE')
    .forEach((p) => console.log(`- ${p.name} (${p.slug}): ${p.image_url}`));
}

run();
