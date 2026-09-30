import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'
import { createServerClient } from '@supabase/ssr'

export async function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl

  // 1. Allow API routes to handle their own authentication and avoid redirect loops
  if (pathname.startsWith('/api')) {
    return NextResponse.next()
  }

  // 2. Allow public static assets and auth callback routes
  if (
    pathname.startsWith('/_next') ||
    pathname.startsWith('/favicon.ico') ||
    pathname.match(/\.(svg|png|jpg|jpeg|gif|webp)$/)
  ) {
    return NextResponse.next()
  }

  let supabaseResponse = NextResponse.next({
    request: {
      headers: request.headers,
    },
  })

  const supabase = createServerClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
    {
      cookies: {
        getAll() {
          return request.cookies.getAll()
        },
        setAll(cookiesToSet) {
          cookiesToSet.forEach(({ name, value, options }) => {
            request.cookies.set(name, value)
          })
          supabaseResponse = NextResponse.next({
            request,
          })
          cookiesToSet.forEach(({ name, value, options }) => {
            supabaseResponse.cookies.set(name, value, options)
          })
        },
      },
    }
  )

  const {
    data: { user },
  } = await supabase.auth.getUser()

  // 3. Unauthenticated access: allow /login and /unauthorized, otherwise redirect to /login
  if (!user) {
    if (pathname.startsWith('/login') || pathname.startsWith('/unauthorized')) {
      return supabaseResponse
    }
    return NextResponse.redirect(new URL('/login', request.url))
  }

  // 4. Authenticated access: verify role
  const { data: profile } = await supabase
    .from('profiles')
    .select('role')
    .eq('id', user.id)
    .single()

  const isAdmin = profile?.role?.toLowerCase() === 'admin'

  // If user is admin and visits /login, redirect to /
  if (isAdmin && pathname.startsWith('/login')) {
    return NextResponse.redirect(new URL('/', request.url))
  }

  // If user is NOT admin, only allow /unauthorized or /login
  if (!isAdmin && !pathname.startsWith('/unauthorized') && !pathname.startsWith('/login')) {
    return NextResponse.redirect(new URL('/unauthorized', request.url))
  }

  return supabaseResponse
}

export const config = {
  matcher: [
    '/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
  ],
}
