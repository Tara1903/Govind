const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');

const env = fs.readFileSync('admin/.env.local', 'utf8');
const supabaseUrl = env.match(/NEXT_PUBLIC_SUPABASE_URL=([^\r\n]+)/)[1];
const supabaseKey = env.match(/SUPABASE_SERVICE_ROLE_KEY=([^\r\n]+)/)[1];
const supabase = createClient(supabaseUrl, supabaseKey);

const REPLACEMENTS = {
  'banana-flower': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/14/Blossom_to_banana_04.jpg/960px-Blossom_to_banana_04.jpg',
  'banana-morris': 'https://images.unsplash.com/photo-1603833665858-e61d17a86224?auto=format&fit=crop&w=600&q=80',
  'baby-corn': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/56/Baby_corn_1.jpg/960px-Baby_corn_1.jpg',
  'lettuce': 'https://images.unsplash.com/photo-1556801712-76c8eb07bbc9?auto=format&fit=crop&w=600&q=80'
};

async function uploadToStorage(storagePath, sourceUrl) {
  try {
    const res = await fetch(sourceUrl, {
      headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)' },
      signal: AbortSignal.timeout(10000)
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const buf = await res.arrayBuffer();
    const { error } = await supabase.storage.from('products').upload(storagePath, buf, {
      contentType: 'image/jpeg',
      upsert: true
    });
    if (error) {
      console.warn(`Storage upload warning for ${storagePath}:`, error.message);
      return sourceUrl;
    }
    const { data: pubData } = supabase.storage.from('products').getPublicUrl(storagePath);
    return pubData.publicUrl;
  } catch (err) {
    console.warn(`Error uploading ${storagePath}: ${err.message}. Using sourceUrl.`);
    return sourceUrl;
  }
}

async function run() {
  for (const [slug, sourceUrl] of Object.entries(REPLACEMENTS)) {
    const storagePath = `fresh/${slug}.jpg`;
    console.log(`Processing: ${slug}...`);
    const finalUrl = await uploadToStorage(storagePath, sourceUrl);
    
    const { data: prod, error: pErr } = await supabase
      .from('products')
      .update({ image_url: finalUrl })
      .eq('slug', slug)
      .select('id, name');
    
    if (pErr) {
      console.error(`Failed to update ${slug}:`, pErr.message);
    } else if (prod && prod.length > 0) {
      const prodId = prod[0].id;
      await supabase.from('product_images').delete().eq('product_id', prodId);
      await supabase.from('product_images').insert([{
        product_id: prodId,
        image_url: finalUrl,
        display_order: 0
      }]);
      console.log(`  ✓ Updated ${prod[0].name} (${slug}) -> ${finalUrl}`);
    }
  }
  console.log('Done!');
}

run();
