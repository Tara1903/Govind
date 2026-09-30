/**
 * GOVIND — Phase 2 Live Catalogue Verification Script
 * Validates:
 * 1. Product counts across experiences (FRESH, KITCHEN, WHOLESALE)
 * 2. Legacy duplicate deletion verification
 * 3. Categories audit
 * 4. Fresh Board audit
 * 5. Images audit (storage URLs, placeholder elimination)
 * 6. Product bulk tiers relational audit
 * 7. Live HTTP check on sample storage assets
 */

const fs = require('fs');
const path = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '../admin/.env.local') });
const { createClient } = require('@supabase/supabase-js');

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!supabaseUrl || !supabaseKey) {
  console.error('Missing Supabase credentials in environment');
  process.exit(1);
}

const supabase = createClient(supabaseUrl, supabaseKey);

async function verify() {
  console.log('=== RUNNING GOVIND PHASE 2 FORENSIC VERIFICATION ===\n');

  // 1. PRODUCTS COUNT & BREAKDOWN
  const { data: allProducts, error: prodErr } = await supabase
    .from('products')
    .select('id, name, slug, price, selling_price, experience_type, on_fresh_board, bulk_available, active, image_url, category_id')
    .order('name');

  if (prodErr) {
    console.error('Failed to query products:', prodErr);
    process.exit(1);
  }

  const fresh = allProducts.filter(p => p.experience_type === 'FRESH');
  const kitchen = allProducts.filter(p => p.experience_type === 'KITCHEN');
  const wholesale = allProducts.filter(p => p.experience_type === 'WHOLESALE');
  const activeCount = allProducts.filter(p => p.active).length;

  console.log(`TOTAL PRODUCTS: ${allProducts.length}`);
  console.log(`- FRESH products: ${fresh.length}`);
  console.log(`- KITCHEN products: ${kitchen.length}`);
  console.log(`- WHOLESALE products: ${wholesale.length}`);
  console.log(`- ACTIVE products: ${activeCount}`);

  // 2. CHECK LEGACY DUPLICATES
  const legacyIds = [
    '0f9bfcb9-d51f-4c93-bdd8-cd3d7ac775d9',
    '4586947e-6d7d-4a18-9409-4cefbf262670',
    '47f78a5d-0547-49d7-8fe5-ba55a7698319',
    'd74cde44-7c8a-45e3-b084-dbf1b92eb98c',
    'f796e431-17b7-4755-acea-368693646a3b',
    'fca418fb-04e6-456a-99bc-dd6df3a42181'
  ];
  const lingeringLegacy = allProducts.filter(p => legacyIds.includes(p.id));
  console.log(`\nLEGACY DUPLICATES REMAINING: ${lingeringLegacy.length}`);
  if (lingeringLegacy.length > 0) {
    console.log('WARNING: Lingering legacy IDs:', lingeringLegacy.map(p => `${p.name} (${p.id})`));
  }

  // 3. FRESH BOARD AUDIT
  const freshBoardItems = allProducts.filter(p => p.on_fresh_board);
  console.log(`\nFRESH BOARD ITEMS: ${freshBoardItems.length}`);
  console.log(freshBoardItems.map(p => `  • ${p.name} (₹${p.selling_price})`).join('\n'));

  // 4. CATEGORIES AUDIT
  const { data: categories, error: catErr } = await supabase
    .from('categories')
    .select('id, name, slug, experience_type, active')
    .order('name');
  console.log(`\nTOTAL CATEGORIES: ${categories ? categories.length : 0}`);
  if (categories) {
    const catsByExp = {};
    categories.forEach(c => {
      const exp = c.experience_type || 'FRESH';
      catsByExp[exp] = (catsByExp[exp] || 0) + 1;
    });
    console.log('Categories by experience:', catsByExp);
  }

  // 5. PRODUCT IMAGES AUDIT
  const { data: images, error: imgErr } = await supabase
    .from('product_images')
    .select('id, product_id, image_url, display_order');
  console.log(`\nTOTAL PRODUCT_IMAGES ROWS: ${images ? images.length : 0}`);
  const uiAvatarsCount = images ? images.filter(i => i.image_url.includes('ui-avatars.com')).length : 0;
  const storageCount = images ? images.filter(i => i.image_url.includes('/storage/v1/object/public/products')).length : 0;
  console.log(`- Images using ui-avatars.com placeholder: ${uiAvatarsCount}`);
  console.log(`- Images hosted on Supabase Storage: ${storageCount}`);

  // Check product.image_url placeholders
  const prodUiAvatars = allProducts.filter(p => p.image_url && p.image_url.includes('ui-avatars.com')).length;
  console.log(`- Products with ui-avatars image_url: ${prodUiAvatars}`);

  // 6. PRODUCT BULK TIERS AUDIT
  const { data: tiers, error: tierErr } = await supabase
    .from('product_bulk_tiers')
    .select('*')
    .order('product_id');
  console.log(`\nTOTAL PRODUCT_BULK_TIERS ROWS: ${tiers ? tiers.length : 0}`);
  if (tiers && tiers.length > 0) {
    const prodsWithTiers = new Set(tiers.map(t => t.product_id));
    console.log(`- Distinct products with bulk tiers: ${prodsWithTiers.size}`);
    console.log('Sample Tiers (first 3):');
    tiers.slice(0, 3).forEach(t => {
      console.log(`  • Product ${t.product_id}: Min Qty ${t.minimum_quantity} -> ${t.discount_percentage}% off (₹${t.discounted_unit_price})`);
    });
  }

  // 7. LIVE HTTP ASSET STATUS CHECK
  console.log('\n--- Live HTTP Asset Health Check ---');
  const sampleUrls = [
    fresh.length > 0 ? fresh[0].image_url : null,
    kitchen.length > 0 ? kitchen[0].image_url : null,
    wholesale.length > 0 ? wholesale[0].image_url : null
  ].filter(Boolean);

  for (const url of sampleUrls) {
    try {
      const res = await fetch(url, { method: 'HEAD' });
      console.log(`HTTP ${res.status} [${res.headers.get('content-type')}] : ${url.substring(0, 80)}...`);
    } catch (e) {
      console.log(`HTTP ERROR: ${e.message} for ${url}`);
    }
  }

  // 8. MANIFEST CHECK
  const manifestPath = 'C:\\Web Apps\\Govind\\docs\\baseline\\phase-2-image-sources.json';
  if (fs.existsSync(manifestPath)) {
    const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
    console.log(`\nIMAGE MANIFEST: Present at ${manifestPath} (${manifest.length} records)`);
  } else {
    console.log(`\nIMAGE MANIFEST: NOT FOUND at ${manifestPath}`);
  }

  console.log('\n=== VERIFICATION COMPLETE ===');
}

verify().catch(console.error);
