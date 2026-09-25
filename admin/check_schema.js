const { createClient } = require('@supabase/supabase-js')
const fs = require('fs')
const envStr = fs.readFileSync('.env.local', 'utf-8')
const url = envStr.match(/NEXT_PUBLIC_SUPABASE_URL=(.*)/)[1].trim()
const key = envStr.match(/SUPABASE_SERVICE_ROLE_KEY=(.*)/)[1].trim()

const supabase = createClient(url, key)

async function check() {
  const response = await fetch(`${url}/rest/v1/orders?limit=1`, {
    headers: { apikey: key, Authorization: `Bearer ${key}` }
  })
  
  // To get the columns, we can do an OPTIONS request or just try to insert and fail.
  // Actually, supabase JS client provides RPC or we can just fetch and look at postgrest response.
  // Easiest is to fetch an empty array and look at the headers? No.
}
check()
