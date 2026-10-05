const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');
const path = require('path');
const https = require('https');

const envPath = path.resolve(__dirname, '../admin/.env.local');
const env = fs.readFileSync(envPath, 'utf8');
const supabaseUrl = env.match(/NEXT_PUBLIC_SUPABASE_URL=([^\r\n]+)/)[1];
const supabaseKey = env.match(/SUPABASE_SERVICE_ROLE_KEY=([^\r\n]+)/)[1];
const supabase = createClient(supabaseUrl, supabaseKey);

const FIXES = [
  {
    slug: 'butter-naan',
    exp: 'kitchen',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/75/Naan_Bread.JPG/960px-Naan_Bread.JPG'
  },
  {
    slug: 'paneer-paratha',
    exp: 'kitchen',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0b/Paratha_is_a_dough_fried_flatbread_native_to_India_and_Pakistan.jpg/960px-Paratha_is_a_dough_fried_flatbread_native_to_India_and_Pakistan.jpg'
  },
  {
    slug: 'aloo-paratha',
    exp: 'kitchen',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bb/Aloo_Paratha1.jpg/960px-Aloo_Paratha1.jpg'
  },
  {
    slug: 'student-tiffin-thali',
    exp: 'kitchen',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/dd/A_thali_with_daal_roti_bhindi_ki_sabzi_and_mango_pickle.jpg/960px-A_thali_with_daal_roti_bhindi_ki_sabzi_and_mango_pickle.jpg'
  },
  {
    slug: 'colocasia',
    exp: 'fresh',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c7/Taro%2C_Kalo_%28Colocasia_esculenta%29_%282868495177%29.jpg/960px-Taro%2C_Kalo_%28Colocasia_esculenta%29_%282868495177%29.jpg'
  },
  {
    slug: 'methi-leaves',
    exp: 'fresh',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/49/Methi_leaves.jpg/960px-Methi_leaves.jpg'
  },
  {
    slug: 'amaranth-leaves',
    exp: 'fresh',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0e/Amaranthus_tricolor_1zz.jpg/960px-Amaranthus_tricolor_1zz.jpg'
  }
];

function downloadBuffer(url) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'GovindProduceAudit/1.0 (taras@example.com)' } }, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return downloadBuffer(res.headers.location).then(resolve).catch(reject);
      }
      if (res.statusCode !== 200) {
        return reject(new Error(`Failed with status ${res.statusCode}`));
      }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve(Buffer.concat(chunks)));
    }).on('error', reject);
  });
}

async function run() {
  console.log(`Starting fix for ${FIXES.length} items...`);
  const ts = Date.now();

  for (const item of FIXES) {
    console.log(`\nProcessing ${item.slug} (${item.exp})...`);
    try {
      const buf = await downloadBuffer(item.url);
      console.log(`  Downloaded ${buf.length} bytes from ${item.url.slice(0, 70)}...`);

      const storagePath = `${item.exp}/${item.slug}.jpg`;
      const { error: uploadError } = await supabase.storage
        .from('products')
        .upload(storagePath, buf, {
          contentType: 'image/jpeg',
          upsert: true
        });

      if (uploadError) {
        console.error(`  Upload error:`, uploadError);
        continue;
      }

      const { data: publicUrlData } = supabase.storage
        .from('products')
        .getPublicUrl(storagePath);

      const finalUrl = `${publicUrlData.publicUrl}?t=${ts}`;
      console.log(`  New public URL: ${finalUrl}`);

      const { error: updateProdError } = await supabase
        .from('products')
        .update({ image_url: finalUrl })
        .eq('slug', item.slug);

      if (updateProdError) {
        console.error(`  Update products error:`, updateProdError);
      } else {
        console.log(`  Updated products table successfully.`);
      }

      // Also update product_images table if present
      const { data: prod } = await supabase.from('products').select('id').eq('slug', item.slug).single();
      if (prod) {
        await supabase
          .from('product_images')
          .update({ image_url: finalUrl })
          .eq('product_id', prod.id);
      }
    } catch (err) {
      console.error(`  Error processing ${item.slug}:`, err.message);
    }
  }

  console.log('\nAll fixes complete!');
}

run();
