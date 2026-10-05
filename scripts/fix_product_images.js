const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');

const env = fs.readFileSync('admin/.env.local', 'utf8');
const supabaseUrl = env.match(/NEXT_PUBLIC_SUPABASE_URL=([^\r\n]+)/)[1];
const supabaseKey = env.match(/SUPABASE_SERVICE_ROLE_KEY=([^\r\n]+)/)[1];
const supabase = createClient(supabaseUrl, supabaseKey);

const REPLACEMENTS = {
  'turnip': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d3/Turnip_2622027.jpg/960px-Turnip_2622027.jpg',
  'brinjal': 'https://images.unsplash.com/photo-1615484477201-9f4953340fab?auto=format&fit=crop&w=600&q=80',
  'custard-apple': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/48/Sugar_apple_with_cross_section.jpg/960px-Sugar_apple_with_cross_section.jpg',
  'lychee': 'https://images.unsplash.com/photo-1597975371270-cf80e4f54921?auto=format&fit=crop&w=600&q=80',
  'cherries': 'https://images.unsplash.com/photo-1529245814698-dd66c442bfef?auto=format&fit=crop&w=600&q=80',
  'jackfruit': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c5/Jackfruit_tree_with_fruit.jpg/960px-Jackfruit_tree_with_fruit.jpg',
  'sapota': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/11/Manilkara_zapota_-_Nispero_fruit_and_leaves_01.jpg/960px-Manilkara_zapota_-_Nispero_fruit_and_leaves_01.jpg',
  'amla': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/51/Phyllanthus_emblica_fruit_02.jpg/960px-Phyllanthus_emblica_fruit_02.jpg',
  'drumstick': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d0/Drumstick_tree_%28Moringa_oleifera%29.jpg/960px-Drumstick_tree_%28Moringa_oleifera%29.jpg',
  'yam': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5c/%E0%B4%9A%E0%B5%87%E0%B4%A8.jpg/960px-%E0%B4%9A%E0%B5%87%E0%B4%A8.jpg',
  'ridge-gourd': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/59/Ridge_gourd_-_Fl%C3%BCgelgurke_-_Luffa_acutangula.jpg/960px-Ridge_gourd_-_Fl%C3%BCgelgurke_-_Luffa_acutangula.jpg',
  'snake-gourd': 'https://upload.wikimedia.org/wikipedia/commons/c/c7/Trichosanthes_Cucumerina_aka_Snake_Gourd.jpeg',
  'cluster-beans': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/88/Cyamopsis_tetragonoloba_%284663783848%29.jpg/960px-Cyamopsis_tetragonoloba_%284663783848%29.jpg',
  'broad-beans': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d5/Vicia_faba_pod_%2804%29.jpg/960px-Vicia_faba_pod_%2804%29.jpg',
  'butter-beans': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cf/Phaseolus_lunatus_MHNT.BOT.2008.1.40.jpg/960px-Phaseolus_lunatus_MHNT.BOT.2008.1.40.jpg',
  'raw-banana': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7c/An_entire_cluster_of_plantains.jpg/960px-An_entire_cluster_of_plantains.jpg',
  'banana-flower': 'https://upload.wikimedia.org/wikipedia/commons/1/14/Blossom_to_banana_04.jpg',
  'banana-morris': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Banana.png/960px-Banana.png',
  'baby-corn': 'https://upload.wikimedia.org/wikipedia/commons/5/56/Baby_corn_1.jpg',
  'lettuce': 'https://images.unsplash.com/photo-1556801712-76c8eb07bbc9?auto=format&fit=crop&w=600&q=80'
};

async function uploadToStorage(storagePath, sourceUrl) {
  try {
    const res = await fetch(sourceUrl, { headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)' } });
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
  console.log('Starting product image update for 20 produce items...');
  for (const [slug, sourceUrl] of Object.entries(REPLACEMENTS)) {
    const storagePath = `fresh/${slug}.jpg`;
    console.log(`Processing: ${slug}...`);
    const finalUrl = await uploadToStorage(storagePath, sourceUrl);
    
    // Update products table
    const { data: prod, error: pErr } = await supabase
      .from('products')
      .update({ image_url: finalUrl })
      .eq('slug', slug)
      .select('id, name');
    
    if (pErr) {
      console.error(`Failed to update product ${slug}:`, pErr.message);
    } else if (prod && prod.length > 0) {
      const prodId = prod[0].id;
      // Update product_images table
      await supabase.from('product_images').delete().eq('product_id', prodId);
      await supabase.from('product_images').insert([{
        product_id: prodId,
        image_url: finalUrl,
        display_order: 0
      }]);
      console.log(`  ✓ Updated ${prod[0].name} (${slug}) -> ${finalUrl}`);
    } else {
      console.log(`  ? Product not found with slug ${slug}`);
    }
  }
  console.log('Finished updating product images!');
}

run();
