/**
 * GOVIND — Patch 10 items into Supabase Storage
 */
const fs = require('fs');
const path = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '../admin/.env.local') });
const { createClient } = require('@supabase/supabase-js');

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

const supabase = createClient(supabaseUrl, supabaseKey);

const urls = {
  'bitter-gourd': 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?auto=format&fit=crop&w=600&q=80',
  'muskmelon': 'https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=600&q=80',
  'pomegranate': 'https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=600&q=80',
  'pumpkin': 'https://images.unsplash.com/photo-1570586437263-ab629fccc818?auto=format&fit=crop&w=600&q=80',
  'radish': 'https://images.unsplash.com/photo-1593105544559-ecb03bf76f82?auto=format&fit=crop&w=600&q=80',
  'sorrel-leaves': 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?auto=format&fit=crop&w=600&q=80',
  'spring-onion': 'https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?auto=format&fit=crop&w=600&q=80',
  'sweet-potato': 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=600&q=80',
  'turnip': 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?auto=format&fit=crop&w=600&q=80',
  'yam': 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=600&q=80'
};

async function patch() {
  console.log('Patching 10 images into Supabase Storage...');
  const manifestPath = 'C:\\Web Apps\\Govind\\docs\\baseline\\phase-2-image-sources.json';
  const manifest = fs.existsSync(manifestPath) ? JSON.parse(fs.readFileSync(manifestPath, 'utf8')) : [];

  for (const [slug, srcUrl] of Object.entries(urls)) {
    const storagePath = `fresh/${slug}.jpg`;
    console.log(`Downloading and uploading ${storagePath}...`);
    const res = await fetch(srcUrl);
    const arrayBuffer = await res.arrayBuffer();
    const buffer = Buffer.from(arrayBuffer);

    const { error: upErr } = await supabase.storage
      .from('products')
      .upload(storagePath, buffer, {
        contentType: 'image/jpeg',
        upsert: true
      });

    if (upErr) {
      console.error(`Upload error for ${storagePath}:`, upErr);
      continue;
    }

    const { data: pubData } = supabase.storage.from('products').getPublicUrl(storagePath);
    const finalUrl = pubData.publicUrl;

    const { data: p } = await supabase.from('products').select('id, name').eq('slug', slug).single();
    if (p) {
      await supabase.from('products').update({ image_url: finalUrl }).eq('id', p.id);
      await supabase.from('product_images').delete().eq('product_id', p.id);
      await supabase.from('product_images').insert([{
        product_id: p.id,
        image_url: finalUrl,
        display_order: 0
      }]);
      console.log(`Updated ${p.name} with storage URL: ${finalUrl}`);

      // Update manifest entry
      const mItem = manifest.find(m => m.slug === slug);
      if (mItem) {
        mItem.final_image_url = finalUrl;
        mItem.source_url = srcUrl;
      }
    }
  }

  fs.writeFileSync(manifestPath, JSON.stringify(manifest, null, 2), 'utf8');
  console.log('Patching complete and manifest updated.');
}

patch().catch(console.error);
