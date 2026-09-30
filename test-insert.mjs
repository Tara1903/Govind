import dotenv from 'dotenv'
import { createClient } from '@supabase/supabase-js'
dotenv.config({ path: ['admin/.env.local', '.env.local', '.env'] })
const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL
const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY
if (!supabaseUrl || !supabaseServiceKey) {
  throw new Error('Missing SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY in environment')
}
const supabase = createClient(supabaseUrl, supabaseServiceKey)
async function testInsert() {
  const { data, error } = await supabase.from('products').insert([
    {
      name: 'Test Product',
      slug: 'test-product',
      price: 100,
      selling_price: 90,
      unit: 'kg',
      on_fresh_board: true
    }
  ])
  console.log(error || 'Success')
}
testInsert()
