const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');
const path = require('path');

const envPath = path.resolve(__dirname, '../admin/.env.local');
const env = fs.readFileSync(envPath, 'utf8');
const supabaseUrl = env.match(/NEXT_PUBLIC_SUPABASE_URL=([^\r\n]+)/)[1];
const supabaseKey = env.match(/SUPABASE_SERVICE_ROLE_KEY=([^\r\n]+)/)[1];
const supabase = createClient(supabaseUrl, supabaseKey);

const FIXES = [
  // 1. Dragon fruit (authentic Pitaya cross section)
  { slug: 'dragon-fruit', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_images/test_dragon_fruit.jpg') },

  // Kitchen
  { slug: 'special-punjabi-thali', exp: 'kitchen', localFile: path.resolve(__dirname, '../temp_candidates/kitchen_special-punjabi-thali.jpg') },
  { slug: 'deluxe-punjabi-thali', exp: 'kitchen', localFile: path.resolve(__dirname, '../temp_candidates/test_deluxe_thali.jpg') },
  { slug: 'chole-bhature', exp: 'kitchen', localFile: path.resolve(__dirname, '../temp_candidates/kitchen_chole-bhature.jpg') },
  { slug: 'gulab-jamun', exp: 'kitchen', localFile: path.resolve(__dirname, '../temp_candidates/kitchen_gulab-jamun.jpg') },
  { slug: 'punjabi-sweet-lassi', exp: 'kitchen', localFile: path.resolve(__dirname, '../temp_candidates/kitchen_punjabi-sweet-lassi.jpg') },
  { slug: 'paneer-paratha', exp: 'kitchen', localFile: path.resolve(__dirname, '../temp_candidates/test_paneer_paratha.jpg') },

  // Fresh
  { slug: 'ginger', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/test_ginger.jpg') },
  { slug: 'green-peas', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/fresh_green-peas.jpg') },
  { slug: 'green-apple', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/fresh_green-apple.jpg') },
  { slug: 'green-chilli', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/fresh_green-chilli.jpg') },
  { slug: 'green-grapes', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/test_green_grapes.jpg') },
  { slug: 'celery', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/test_celery_market.jpg') },
  { slug: 'coriander', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/fresh_coriander.jpg') },
  { slug: 'dates-fresh', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/test_dates_bowl.jpg') },
  { slug: 'coconut', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/fresh_coconut.jpg') },
  { slug: 'bottle-gourd', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/test_lauki_real.jpg') },
  { slug: 'zucchini', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/test_zucchini_basket.jpg') },
  { slug: 'raw-mango', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/fresh_raw-mango.jpg') },
  { slug: 'drumstick', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/fresh_drumstick.jpg') },
  { slug: 'yam', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/test_yam.jpg') },
  { slug: 'raw-banana', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/fresh_raw-banana.jpg') },
  { slug: 'cluster-beans', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/fresh_cluster-beans.jpg') },
  { slug: 'butter-beans', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/test_butter_beans.jpg') },
  { slug: 'custard-apple', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/test_custard_apple.jpg') },
  { slug: 'ridge-gourd', exp: 'fresh', localFile: path.resolve(__dirname, '../temp_candidates/fresh_ridge-gourd.jpg') },

  // Wholesale
  { slug: 'wholesale-ginger-25kg', exp: 'wholesale', localFile: path.resolve(__dirname, '../temp_candidates/wholesale_wholesale-ginger-25kg.jpg') },
  { slug: 'wholesale-green-peas-20kg', exp: 'wholesale', localFile: path.resolve(__dirname, '../temp_candidates/wholesale_wholesale-green-peas-20kg.jpg') },
  { slug: 'wholesale-apple-shimla-20kg', exp: 'wholesale', localFile: path.resolve(__dirname, '../temp_candidates/test_wholesale_apple.jpg') },
  { slug: 'wholesale-garlic-25kg', exp: 'wholesale', localFile: path.resolve(__dirname, '../temp_candidates/test_garlic_market_france.jpg') }
];

async function deploy() {
  console.log(`Starting deployment of ${FIXES.length} verified pure vegetarian images...`);
  const ts = Date.now();
  let successCount = 0;

  for (const item of FIXES) {
    console.log(`\nProcessing ${item.slug} (${item.exp})...`);
    if (!fs.existsSync(item.localFile)) {
      console.error(`  LOCAL FILE MISSING: ${item.localFile}`);
      continue;
    }

    const buf = fs.readFileSync(item.localFile);
    console.log(`  Read ${buf.length} bytes from ${path.basename(item.localFile)}`);

    const storagePath = `${item.exp}/${item.slug}.jpg`;
    const { error: uploadError } = await supabase.storage
      .from('products')
      .upload(storagePath, buf, {
        contentType: 'image/jpeg',
        upsert: true
      });

    if (uploadError) {
      console.error(`  Upload failed:`, uploadError);
      continue;
    }
    console.log(`  Uploaded to storage products/${storagePath}`);

    const { data: publicUrlData } = supabase.storage
      .from('products')
      .getPublicUrl(storagePath);

    const finalUrl = `${publicUrlData.publicUrl}?t=${ts}`;
    console.log(`  Final URL: ${finalUrl}`);

    // Update public.products
    const { error: prodError } = await supabase
      .from('products')
      .update({ image_url: finalUrl })
      .eq('slug', item.slug);

    if (prodError) {
      console.error(`  Failed to update public.products:`, prodError);
      continue;
    }

    // Update public.product_images
    const { data: prod } = await supabase
      .from('products')
      .select('id')
      .eq('slug', item.slug)
      .single();

    if (prod) {
      await supabase
        .from('product_images')
        .update({ image_url: finalUrl })
        .eq('product_id', prod.id);
    }

    // Sync to temp_images so local visual audit is 100% updated
    const localAuditPath = path.resolve(__dirname, `../temp_images/${item.exp.toUpperCase()}_${item.slug}.jpg`);
    fs.copyFileSync(item.localFile, localAuditPath);

    successCount++;
    console.log(`  [SUCCESS] ${item.slug} updated in storage, database, and local audit!`);
  }

  console.log(`\n========================================`);
  console.log(`Successfully deployed ${successCount} / ${FIXES.length} verified product images!`);
  console.log(`========================================`);
}

deploy();
