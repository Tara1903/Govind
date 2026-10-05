const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');
const path = require('path');

const envPath = path.resolve(__dirname, '../admin/.env.local');
const env = fs.readFileSync(envPath, 'utf8');
const supabaseUrl = env.match(/NEXT_PUBLIC_SUPABASE_URL=([^\r\n]+)/)[1];
const supabaseKey = env.match(/SUPABASE_SERVICE_ROLE_KEY=([^\r\n]+)/)[1];
const supabase = createClient(supabaseUrl, supabaseKey);

const REPLACEMENTS = {
  // FRESH PRODUCE
  'mushroom': {
    exp: 'fresh',
    url: 'https://images.unsplash.com/photo-1504544750208-dc0358e63f7f?auto=format&fit=crop&w=600&q=80'
  },
  'bitter-gourd': {
    exp: 'fresh',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9d/Taiwan_2009_Tainan_City_Organic_Farm_Bitter_Gourd_FRD_7956.jpg/960px-Taiwan_2009_Tainan_City_Organic_Farm_Bitter_Gourd_FRD_7956.jpg'
  },
  'ash-gourd': {
    exp: 'fresh',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2b/Ash_Gourd-Benincasa_hispida.jpg/960px-Ash_Gourd-Benincasa_hispida.jpg'
  },
  'muskmelon': {
    exp: 'fresh',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9a/Cantaloupe.jpg/960px-Cantaloupe.jpg'
  },
  'mustard-greens': {
    exp: 'fresh',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/50/Mustard_greens.jpg/960px-Mustard_greens.jpg'
  },
  'sweet-potato': {
    exp: 'fresh',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/22/Ipomoea_batatas_%28Sweet_potato%29_in_China.jpg/960px-Ipomoea_batatas_%28Sweet_potato%29_in_China.jpg'
  },
  'radish': {
    exp: 'fresh',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/84/Raphanus_sativus_convar_lobo20100405_19.jpg/960px-Raphanus_sativus_convar_lobo20100405_19.jpg'
  },
  'spring-onion': {
    exp: 'fresh',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/14/Scallions.jpg/960px-Scallions.jpg'
  },
  'sorrel-leaves': {
    exp: 'fresh',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a3/%28MHNT%29_Rumex_acetosa_-_Habit.jpg/960px-%28MHNT%29_Rumex_acetosa_-_Habit.jpg'
  },
  'pomegranate': {
    exp: 'fresh',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9b/Pomegranate02_edit.jpg/960px-Pomegranate02_edit.jpg'
  },

  // KITCHEN DISHES
  'boondi-raita': {
    exp: 'kitchen',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0b/Boondi_Raita.jpg/960px-Boondi_Raita.jpg'
  },
  'rajma-chawal-bowl': {
    exp: 'kitchen',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e3/Rajma_Chawal.jpg/960px-Rajma_Chawal.jpg'
  },
  'gobhi-paratha': {
    exp: 'kitchen',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/55/Picture_of_tasty_Gobi_paratha.JPG/960px-Picture_of_tasty_Gobi_paratha.JPG'
  },
  'butter-naan': {
    exp: 'kitchen',
    url: 'https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=600&q=80'
  },
  'dal-makhani-jeera-rice': {
    exp: 'kitchen',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f8/Dal_Makhani.jpg/960px-Dal_Makhani.jpg'
  },

  // WHOLESALE BULK
  'wholesale-potato-50kg': {
    exp: 'wholesale',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ab/Patates.jpg/960px-Patates.jpg'
  },
  'wholesale-onion-50kg': {
    exp: 'wholesale',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/dd/Red_Onions.jpg/960px-Red_Onions.jpg'
  },
  'wholesale-tomato-25kg': {
    exp: 'wholesale',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/89/Tomato_je.jpg/960px-Tomato_je.jpg'
  },
  'wholesale-garlic-25kg': {
    exp: 'wholesale',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2f/Garlic_bulbs.jpg/960px-Garlic_bulbs.jpg'
  },
  'wholesale-ginger-25kg': {
    exp: 'wholesale',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/12/Ginger_roots.jpg/960px-Ginger_roots.jpg'
  },
  'wholesale-green-peas-20kg': {
    exp: 'wholesale',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a7/Green_peas.jpg/960px-Green_peas.jpg'
  },
  'wholesale-apple-shimla-20kg': {
    exp: 'wholesale',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/15/Red_Apple.jpg/960px-Red_Apple.jpg'
  },
  'wholesale-banana-15kg': {
    exp: 'wholesale',
    url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/Bananas.jpg/960px-Bananas.jpg'
  }
};

async function uploadToStorage(storagePath, sourceUrl) {
  try {
    const res = await fetch(sourceUrl, {
      headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) GovindAudit/1.0' },
      signal: AbortSignal.timeout(10000)
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const buf = await res.arrayBuffer();
    const { error } = await supabase.storage.from('products').upload(storagePath, buf, {
      contentType: 'image/jpeg',
      upsert: true
    });
    if (error) {
      console.warn(`Storage upload warning for ${storagePath}:`, error.message);
      return sourceUrl;
    }
    const { data: pubData } = supabase.storage.from('products').getPublicUrl(storagePath);
    return `${pubData.publicUrl}?t=${Date.now()}`;
  } catch (err) {
    console.warn(`Error uploading ${storagePath}: ${err.message}. Using sourceUrl.`);
    return sourceUrl;
  }
}

async function run() {
  console.log(`Starting image replacement for ${Object.keys(REPLACEMENTS).length} products...\n`);
  let successCount = 0;
  for (const [slug, item] of Object.entries(REPLACEMENTS)) {
    const storagePath = `${item.exp}/${slug}.jpg`;
    process.stdout.write(`Processing [${item.exp.toUpperCase()}] ${slug}... `);
    const finalUrl = await uploadToStorage(storagePath, item.url);
    
    // Update products table
    const { data: prod, error: pErr } = await supabase
      .from('products')
      .update({ image_url: finalUrl })
      .eq('slug', slug)
      .select('id, name');
    
    if (pErr) {
      console.log(`FAILED: ${pErr.message}`);
    } else if (prod && prod.length > 0) {
      const prodId = prod[0].id;
      await supabase.from('product_images').delete().eq('product_id', prodId);
      await supabase.from('product_images').insert([{
        product_id: prodId,
        image_url: finalUrl,
        display_order: 0
      }]);
      console.log(`✓ (${prod[0].name})`);
      successCount++;
    } else {
      console.log(`? Slug not found in products table`);
    }
  }
  console.log(`\nSuccessfully updated ${successCount}/${Object.keys(REPLACEMENTS).length} products in Supabase.`);
}

run();
