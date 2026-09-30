import { createClient } from '@supabase/supabase-js';
import fs from 'fs';

const env = fs.readFileSync('admin/.env.local', 'utf8');
let url, anonKey;
env.split('\n').forEach(line => {
  if (line.startsWith('NEXT_PUBLIC_SUPABASE_URL=')) url = line.split('=')[1].trim();
  if (line.startsWith('NEXT_PUBLIC_SUPABASE_ANON_KEY=')) anonKey = line.split('=')[1].trim();
});

const sb = createClient(url, anonKey);

async function testNonAdmin() {
  const projectRef = 'crkuiuxajywlgmlnklvj';
  // Use customer user
  const customerSession = {
    access_token: 'dummy',
    user: { id: 'd2129669-fbb7-4b28-adb1-cda1fbbc2598', email: 'harisinghsikh1252@gmail.com' }
  };
  
  // Or test with real login if password exists, or test /unauthorized route directly
  const res = await fetch('http://localhost:3001/unauthorized');
  console.log('/unauthorized status:', res.status, res.status === 200 ? 'PASS' : 'FAIL');
}
testNonAdmin();
