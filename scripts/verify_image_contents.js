const fs = require('fs');
const path = require('path');
const https = require('https');
const { createClient } = require('@supabase/supabase-js');
require('dotenv').config({ path: path.resolve(__dirname, '../admin/.env.local') });

const supabase = createClient(
  process.env.NEXT_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL,
  process.env.SUPABASE_SERVICE_ROLE_KEY
);

async function downloadSample(url, dest) {
  return new Promise((resolve, reject) => {
    https.get(url, (res) => {
      if (res.statusCode === 200) {
        const fileStream = fs.createWriteStream(dest);
        res.pipe(fileStream);
        fileStream.on('finish', () => {
          fileStream.close(resolve);
        });
      } else {
        reject(new Error(`Failed with status ${res.statusCode}`));
      }
    }).on('error', reject);
  });
}

async function run() {
  const { data: products } = await supabase
    .from('products')
    .select('id, name, slug, experience_type, image_url')
    .order('experience_type')
    .order('name');

  const tmpDir = path.resolve(__dirname, '../temp_images');
  if (!fs.existsSync(tmpDir)) {
    fs.mkdirSync(tmpDir, { recursive: true });
  }

  console.log(`Downloading all ${products.length} images to ${tmpDir}...`);
  for (const p of products) {
    const filename = `${p.experience_type}_${p.slug}.jpg`;
    const dest = path.join(tmpDir, filename);
    if (!fs.existsSync(dest)) {
      try {
        await downloadSample(p.image_url, dest);
        process.stdout.write('.');
      } catch (e) {
        console.error(`\nFailed for ${p.name}: ${e.message}`);
      }
    }
  }
  console.log('\nDownload complete.');
}

run();
