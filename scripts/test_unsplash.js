const testUrls = [
  // Mushrooms
  { slug: 'mushroom', url: 'https://images.unsplash.com/photo-1504544750208-dc0358e63f7f?auto=format&fit=crop&w=600&q=80' },
  { slug: 'mushroom_alt', url: 'https://images.unsplash.com/photo-1588615419957-566a7ecf265b?auto=format&fit=crop&w=600&q=80' },
  // Pomegranate
  { slug: 'pomegranate', url: 'https://images.unsplash.com/photo-1615485290382-441e4d049cb5?auto=format&fit=crop&w=600&q=80' },
  { slug: 'pomegranate_alt', url: 'https://images.unsplash.com/photo-1541344999736-83eca872f242?auto=format&fit=crop&w=600&q=80' },
  // Radish (white daikon / mooli)
  { slug: 'radish', url: 'https://images.unsplash.com/photo-1590779033100-9f60a05a013d?auto=format&fit=crop&w=600&q=80' },
  // Sweet Potato
  { slug: 'sweet-potato', url: 'https://images.unsplash.com/photo-1596097635121-14b63b7a0c19?auto=format&fit=crop&w=600&q=80' },
  // Spring Onion
  { slug: 'spring-onion', url: 'https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?auto=format&fit=crop&w=600&q=80' },
  // Muskmelon / Cantaloupe
  { slug: 'muskmelon', url: 'https://images.unsplash.com/photo-1571575179703-4bde44fb408c?auto=format&fit=crop&w=600&q=80' },
  // Mustard Greens / Sarson
  { slug: 'mustard-greens', url: 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?auto=format&fit=crop&w=600&q=80' },
  // Naan
  { slug: 'butter-naan', url: 'https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=600&q=80' },
  // Dal Makhani
  { slug: 'dal-makhani-jeera-rice', url: 'https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=600&q=80' },
  // Rajma Chawal
  { slug: 'rajma-chawal-bowl', url: 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?auto=format&fit=crop&w=600&q=80' },
  // Wholesale Produce
  { slug: 'wholesale-potato', url: 'https://images.unsplash.com/photo-1508747703725-719777637510?auto=format&fit=crop&w=600&q=80' },
  { slug: 'wholesale-onion', url: 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=600&q=80' }
];

async function run() {
  for (const item of testUrls) {
    try {
      const res = await fetch(item.url, { headers: { 'User-Agent': 'Mozilla/5.0' }, signal: AbortSignal.timeout(5000) });
      console.log(`[${res.status}] ${item.slug} (${res.headers.get('content-type')})`);
    } catch (e) {
      console.log(`[ERR] ${item.slug}: ${e.message}`);
    }
  }
}
run();
