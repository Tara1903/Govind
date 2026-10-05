const { createClient } = require('@supabase/supabase-js');
const path = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '../admin/.env.local') });

const supabase = createClient(
  process.env.NEXT_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL,
  process.env.SUPABASE_SERVICE_ROLE_KEY
);

async function run() {
  const { data: products } = await supabase
    .from('products')
    .select('id, name, slug, category_id, experience_type, image_url, description')
    .order('experience_type')
    .order('name');

  const { data: categories } = await supabase.from('categories').select('id, name, slug');
  const catMap = {};
  categories.forEach(c => { catMap[c.id] = c.name; });

  console.log(`Auditing ${products.length} products...\n`);
  
  products.forEach(p => {
    console.log(`[${p.experience_type}] ${p.name} (${p.slug})`);
    console.log(`   Cat: ${catMap[p.category_id] || 'Unknown'}`);
    console.log(`   Img: ${p.image_url}`);
  });
}

run();
