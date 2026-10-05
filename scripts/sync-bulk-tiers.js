const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');

const envLocal = fs.readFileSync('admin/.env.local', 'utf-8');
const getEnv = (key) => {
  const match = envLocal.match(new RegExp(key + '=(.*)'));
  return match ? match[1].replace(/['"]/g, '').trim() : null;
};

const supabase = createClient(getEnv('NEXT_PUBLIC_SUPABASE_URL'), getEnv('SUPABASE_SERVICE_ROLE_KEY'));

async function syncBulkTiersToBundleItems() {
  try {
    const { data: tiers, error: tErr } = await supabase.from('product_bulk_tiers').select('*').eq('is_active', true);
    if (tErr) throw tErr;

    const { data: products, error: pErr } = await supabase.from('products').select('id, name, bundle_items');
    if (pErr) throw pErr;

    console.log(`Syncing ${tiers.length} tiers across ${products.length} products...`);

    let updatedCount = 0;
    for (const prod of products) {
      const prodTiers = tiers.filter(t => t.product_id === prod.id).sort((a, b) => a.minimum_quantity - b.minimum_quantity);
      if (prodTiers.length > 0) {
        const wholesalePricing = {
          wholesale_eligible: true,
          tiers: prodTiers.map(t => ({
            min_qty: t.minimum_quantity,
            type: t.pricing_type || 'percentage',
            value: Number(t.discount_percentage || 0),
            status: 'active'
          }))
        };

        const existingBundle = prod.bundle_items || {};
        const updatedBundle = {
          ...existingBundle,
          wholesale_pricing: wholesalePricing
        };

        const { error: uErr } = await supabase
          .from('products')
          .update({ bundle_items: updatedBundle })
          .eq('id', prod.id);

        if (uErr) {
          console.error(`Failed to update product ${prod.name}:`, uErr.message);
        } else {
          updatedCount++;
          console.log(`Synced ${prod.name}: ${prodTiers.length} tiers`);
        }
      }
    }

    console.log(`\nSuccessfully synced ${updatedCount} products with wholesale bulk tiers!`);
  } catch (err) {
    console.error('Sync error:', err);
  }
}

syncBulkTiersToBundleItems();
