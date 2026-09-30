import { createClient } from '@supabase/supabase-js';
import fs from 'fs';

const env = fs.readFileSync('admin/.env.local', 'utf8');
let url, anonKey;
env.split('\n').forEach(line => {
  if (line.startsWith('NEXT_PUBLIC_SUPABASE_URL=')) url = line.split('=')[1].trim();
  if (line.startsWith('NEXT_PUBLIC_SUPABASE_ANON_KEY=')) anonKey = line.split('=')[1].trim();
});

const sb = createClient(url, anonKey);

async function testAdminAccess() {
  const { data: authData, error: authError } = await sb.auth.signInWithPassword({
    email: 'admin@govind.com',
    password: 'GovindAdminPass2026!'
  });

  if (authError) {
    console.error('Sign in error:', authError);
    return;
  }
  console.log('Admin signed in successfully! User ID:', authData.user.id);

  const projectRef = 'crkuiuxajywlgmlnklvj';
  const sessionStr = JSON.stringify(authData.session);
  const base64Session = 'base64-' + Buffer.from(sessionStr).toString('base64');
  
  // @supabase/ssr format
  const cookieHeader = `sb-${projectRef}-auth-token=${encodeURIComponent(base64Session)}`;

  const routes = [
    '/',
    '/orders',
    '/orders/c98a11b4-f409-4cfd-ae6f-ccf6d0fa1ab7',
    '/products',
    '/categories',
    '/inventory',
    '/customers',
    '/coupons',
    '/promotions',
    '/settings',
    '/deliveries',
    '/fresh-board',
    '/daily-rates',
    '/punjabi-menu'
  ];

  for (const r of routes) {
    const res = await fetch('http://localhost:3001' + r, {
      headers: {
        'Cookie': cookieHeader
      }
    });
    console.log(r, 'status:', res.status, res.status === 200 ? 'PASS' : 'REDIRECT -> ' + res.headers.get('location'));
  }
}

testAdminAccess();
