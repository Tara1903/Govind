const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');
const path = require('path');
const https = require('https');

const envPath = path.resolve(__dirname, '../admin/.env.local');
const env = fs.readFileSync(envPath, 'utf8');
const supabaseUrl = env.match(/NEXT_PUBLIC_SUPABASE_URL=([^\r\n]+)/)[1];
const supabaseKey = env.match(/SUPABASE_SERVICE_ROLE_KEY=([^\r\n]+)/)[1];
const supabase = createClient(supabaseUrl, supabaseKey);

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

  console.log(`Re-downloading all ${products.length} images to ${tmpDir}...`);
  for (const p of products) {
    const filename = `${p.experience_type}_${p.slug}.jpg`;
    const dest = path.join(tmpDir, filename);
    try {
      await downloadSample(p.image_url, dest);
      process.stdout.write('.');
    } catch (e) {
      console.error(`\nFailed for ${p.name}: ${e.message}`);
    }
  }
  console.log('\nAll downloads refreshed!');
}

run();
