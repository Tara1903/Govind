import { NextResponse } from 'next/server'
import { createClient } from '@supabase/supabase-js'

export async function GET() {
  const supabase = createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.SUPABASE_SERVICE_ROLE_KEY!
  )

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const [sentToday, totalDevices, unreadCount, activeCampaigns] = await Promise.all([
    supabase.from('notification_outbox').select('id', { count: 'exact' })
      .eq('status', 'SENT').gte('sent_at', today.toISOString()),
    supabase.from('notification_devices').select('id', { count: 'exact' }).eq('enabled', true),
    supabase.from('notifications').select('id', { count: 'exact' }).is('read_at', null),
    supabase.from('notification_campaigns').select('id', { count: 'exact' })
      .in('status', ['SCHEDULED', 'PROCESSING']),
  ])

  return NextResponse.json({
    sent_today: sentToday.count || 0,
    total_devices: totalDevices.count || 0,
    total_unread: unreadCount.count || 0,
    active_campaigns: activeCampaigns.count || 0,
  })
}
