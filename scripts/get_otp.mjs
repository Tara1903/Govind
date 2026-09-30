import { createClient } from '@supabase/supabase-js';
import fs from 'fs';

const env = fs.readFileSync('admin/.env.local', 'utf8');
let url, serviceKey;
env.split('\n').forEach(line => {
  if (line.startsWith('NEXT_PUBLIC_SUPABASE_URL=')) url = line.split('=')[1].trim();
  if (line.startsWith('SUPABASE_SERVICE_ROLE_KEY=')) serviceKey = line.split('=')[1].trim();
});

const sb = createClient(url, serviceKey);

async function getLatestOtp() {
  // Query auth.audit_log_entries or user confirmation
  // Or check auth.users confirmation_token
  const { data: users, error } = await sb.auth.admin.listUsers();
  const u = users?.users?.find(x => x.email === 'harisinghsikh1252@gmail.com');
  console.log('User found:', u?.id);
  
  // Or generate a magic link / OTP directly via admin api
  const { data: linkData, error: linkErr } = await sb.auth.admin.generateLink({
    type: 'magiclink',
    email: 'harisinghsikh1252@gmail.com'
  });
  console.log('Generated OTP properties:', linkData?.properties?.email_otp || linkData?.properties);
}

getLatestOtp();
