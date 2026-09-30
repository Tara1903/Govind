require('dotenv').config({ path: ['admin/.env.local', '.env.local', '.env'] });
const { createClient } = require('@supabase/supabase-js');
const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL;
const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;
if (!supabaseUrl || !supabaseServiceKey) {
  throw new Error('Missing SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY in environment');
}
const supabase = createClient(supabaseUrl, supabaseServiceKey);

async function check() {
  const { data: p } = await supabase.from('products').select('*');
  const { data: i } = await supabase.from('product_images').select('*');
  const { data: c } = await supabase.from('categories').select('*');
  console.log('Products:', p.length);
  console.log('Images:', i.length);
  console.log('Categories:', c.length);
}
check();
