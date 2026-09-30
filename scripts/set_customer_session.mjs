import { createClient } from '@supabase/supabase-js';
import fs from 'fs';
import { execSync } from 'child_process';

const env = fs.readFileSync('admin/.env.local', 'utf8');
let url, anonKey, serviceKey;
env.split('\n').forEach(line => {
  if (line.startsWith('NEXT_PUBLIC_SUPABASE_URL=')) url = line.split('=')[1].trim();
  if (line.startsWith('NEXT_PUBLIC_SUPABASE_ANON_KEY=')) anonKey = line.split('=')[1].trim();
  if (line.startsWith('SUPABASE_SERVICE_ROLE_KEY=')) serviceKey = line.split('=')[1].trim();
});

const adminSb = createClient(url, serviceKey);
const clientSb = createClient(url, anonKey);

async function run() {
  const { data: linkData, error: linkErr } = await adminSb.auth.admin.generateLink({
    type: 'magiclink',
    email: 'harisinghsikh1252@gmail.com'
  });
  
  if (linkErr) {
    console.error('linkErr:', linkErr);
    return;
  }
  
  const { data: verifyData, error: verifyErr } = await clientSb.auth.verifyOtp({
    email: 'harisinghsikh1252@gmail.com',
    token: linkData.properties.email_otp,
    type: 'email'
  });
  
  if (verifyErr || !verifyData.session) {
    console.error('verifyErr:', verifyErr);
    return;
  }
  
  const s = verifyData.session;
  console.log('Got session for:', s.user.email, s.user.id);
  
  const xml = `<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <boolean name="has_completed_onboarding" value="true" />
    <boolean name="is_guest" value="false" />
    <string name="last_experience">FRESH</string>
    <string name="access_token">${s.access_token}</string>
    <string name="refresh_token">${s.refresh_token}</string>
    <string name="user_id">${s.user.id}</string>
    <string name="user_email">${s.user.email}</string>
</map>
`;

  fs.writeFileSync('scripts/session_prefs.xml', xml);
  console.log('session_prefs.xml written');
}

run();
