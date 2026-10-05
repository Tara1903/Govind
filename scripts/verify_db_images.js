const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');
const path = require('path');

const envPath = path.resolve(__dirname, '../admin/.env.local');
const env = fs.readFileSync(envPath, 'utf8');
const supabaseUrl = env.match(/NEXT_PUBLIC_SUPABASE_URL=([^\r\n]+)/)[1];
const supabaseKey = env.match(/SUPABASE_SERVICE_ROLE_KEY=([^\r\n]+)/)[1];
const supabase = createClient(supabaseUrl, supabaseKey);

async function check() {
  const { data: prods, error } = await supabase
    .from('products')
    .select('id, name, slug, experience_type, image_url')
    .order('experience_type', { ascending: true });

  if (error) {
    console.error('Error fetching products:', error);
    return;
  }

  console.log(`Total products: ${prods.length}`);
  const missing = prods.filter(p => !p.image_url || p.image_url.trim() === '');
  console.log(`Products missing image_url: ${missing.length}`);
  if (missing.length > 0) {
    missing.forEach(m => console.log(`  - ${m.slug} (${m.experience_type})`));
  } else {
    console.log('All products have valid image URLs!');
  }
}

check();
