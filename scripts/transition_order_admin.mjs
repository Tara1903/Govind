import { createClient } from '@supabase/supabase-js';
import fs from 'fs';

const env = fs.readFileSync('admin/.env.local', 'utf8');
let url, serviceKey;
env.split('\n').forEach(line => {
  if (line.startsWith('NEXT_PUBLIC_SUPABASE_URL=')) url = line.split('=')[1].trim();
  if (line.startsWith('SUPABASE_SERVICE_ROLE_KEY=')) serviceKey = line.split('=')[1].trim();
});

const sb = createClient(url, serviceKey);

async function transitionOrder() {
  const orderId = 'c98a11b4-f409-4cfd-ae6f-ccf6d0fa1ab7';
  console.log(`Starting transition for Order ${orderId}: PLACED -> CONFIRMED...`);

  // 1. Update order status
  const now = new Date().toISOString();
  const { data: updatedOrder, error: updateError } = await sb
    .from('orders')
    .update({ 
      order_status: 'CONFIRMED',
      updated_at: now
    })
    .eq('id', orderId)
    .select()
    .single();

  if (updateError) {
    console.error('Update error:', updateError);
    return;
  }

  // 2. Insert into order_status_history
  const { data: historyEntry, error: historyError } = await sb
    .from('order_status_history')
    .insert([{
      order_id: orderId,
      status: 'CONFIRMED',
      notes: 'Admin accepted and confirmed customer order for kitchen/packaging',
      created_at: now
    }])
    .select()
    .single();

  if (historyError) {
    console.log('History insert note (may have been inserted by trigger):', historyError.message);
  }

  // 3. Verify current state
  const { data: finalOrder } = await sb.from('orders').select('*').eq('id', orderId).single();
  const { data: allHistory } = await sb.from('order_status_history').select('*').eq('order_id', orderId).order('created_at', { ascending: true });

  console.log('Final Order Status:', finalOrder.order_status);
  console.log('History Count:', allHistory ? allHistory.length : 0);
  allHistory?.forEach(h => console.log(`  - [${h.created_at}] Status: ${h.status} | Note: ${h.notes || h.note}`));
}

transitionOrder();
