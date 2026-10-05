const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');

const envLocal = fs.readFileSync('admin/.env.local', 'utf-8');
const getEnv = (key) => {
  const match = envLocal.match(new RegExp(key + '=(.*)'));
  return match ? match[1].replace(/['"]/g, '').trim() : null;
};

const supabase = createClient(getEnv('NEXT_PUBLIC_SUPABASE_URL'), getEnv('SUPABASE_SERVICE_ROLE_KEY'));

async function inspectBulkPricing() {
  try {
    const { data: tiers, error: tErr } = await supabase.from('product_bulk_tiers').select('*');
    if (tErr) throw tErr;

    const prodIds = [...new Set(tiers.map(t => t.product_id))];
    console.log(`Total tiers: ${tiers.length}, Distinct products with tiers: ${prodIds.length}`);

    const { data: prods, error: pErr } = await supabase
      .from('products')
      .select('id, name, experience_type, selling_price, bundle_items')
      .in('id', prodIds);
    if (pErr) throw pErr;

    console.log('\nProducts configured with bulk tiers:');
    prods.forEach(p => {
      const pTiers = tiers.filter(t => t.product_id === p.id);
      console.log(`- [${p.experience_type}] ${p.name} (₹${p.selling_price}) -> ${pTiers.length} tiers:`, 
        pTiers.map(t => `${t.minimum_quantity}+: ${t.discount_percentage}% off (₹${t.discounted_unit_price})`).join(', ')
      );
    });

  } catch (err) {
    console.error('Error:', err);
  }
}

inspectBulkPricing();
