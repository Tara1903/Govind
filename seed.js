require('dotenv').config({ path: ['admin/.env.local', '.env.local', '.env'] });
const { createClient } = require('@supabase/supabase-js');
const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL;
const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;
if (!supabaseUrl || !supabaseServiceKey) {
  throw new Error('Missing SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY in environment');
}
const supabase = createClient(supabaseUrl, supabaseServiceKey);

const data = [
  { top: 'Fresh Vegetables', sub: 'Everyday Essentials', products: [
    { n: 'Potato', unit: 'kg', ref: 32, gov: 28 },
    { n: 'Onion', unit: 'kg', ref: 70, gov: 64 },
    { n: 'Tomato', unit: 'kg', ref: 34, gov: 29 },
    { n: 'Carrot', unit: 'kg', ref: 50, gov: 45 },
    { n: 'Cucumber', unit: 'kg', ref: 40, gov: 35 },
    { n: 'Brinjal', unit: 'kg', ref: 45, gov: 39 },
    { n: 'Green Peas', unit: 'kg', ref: 90, gov: 79 },
    { n: 'Sweet Corn', unit: 'kg', ref: 65, gov: 55 },
    { n: 'Capsicum', unit: 'kg', ref: 55, gov: 47 },
    { n: 'Green Chilli', unit: 'kg', ref: 72, gov: 62 }
  ]},
  { top: 'Fresh Vegetables', sub: 'Leafy Greens', products: [
    { n: 'Spinach', unit: 'bunch', ref: 15, gov: 12 },
    { n: 'Coriander', unit: 'bunch', ref: 12, gov: 9 },
    { n: 'Mint', unit: 'bunch', ref: 10, gov: 8 },
    { n: 'Mustard Greens', unit: 'bunch', ref: 22, gov: 18 },
    { n: 'Amaranth Leaves', unit: 'bunch', ref: 15, gov: 12 },
    { n: 'Methi Leaves', unit: 'bunch', ref: 25, gov: 20 },
    { n: 'Sorrel Leaves', unit: 'bunch', ref: 20, gov: 17 }
  ]},
  { top: 'Fresh Vegetables', sub: 'Roots & Tubers', products: [
    { n: 'Beetroot', unit: 'kg', ref: 49, gov: 43 },
    { n: 'Radish', unit: 'kg', ref: 42, gov: 36 },
    { n: 'Sweet Potato', unit: 'kg', ref: 42, gov: 35 },
    { n: 'Turnip', unit: 'kg', ref: 50, gov: 44 },
    { n: 'Colocasia', unit: 'kg', ref: 60, gov: 52 },
    { n: 'Yam', unit: 'kg', ref: 75, gov: 65 }
  ]},
  { top: 'Fresh Vegetables', sub: 'Gourds', products: [
    { n: 'Bottle Gourd', unit: 'kg', ref: 34, gov: 28 },
    { n: 'Ridge Gourd', unit: 'kg', ref: 46, gov: 39 },
    { n: 'Snake Gourd', unit: 'kg', ref: 40, gov: 34 },
    { n: 'Bitter Gourd', unit: 'kg', ref: 48, gov: 41 },
    { n: 'Ash Gourd', unit: 'kg', ref: 20, gov: 18 },
    { n: 'Pumpkin', unit: 'kg', ref: 26, gov: 22 },
    { n: 'Drumstick', unit: 'kg', ref: 65, gov: 55 }
  ]},
  { top: 'Fresh Vegetables', sub: 'Beans & Pods', products: [
    { n: 'French Beans', unit: 'kg', ref: 66, gov: 56 },
    { n: 'Cluster Beans', unit: 'kg', ref: 48, gov: 41 },
    { n: 'Broad Beans', unit: 'kg', ref: 55, gov: 47 },
    { n: 'Butter Beans', unit: 'kg', ref: 52, gov: 44 }
  ]},
  { top: 'Fresh Vegetables', sub: 'Cruciferous Vegetables', products: [
    { n: 'Cabbage', unit: 'kg', ref: 36, gov: 30 },
    { n: 'Cauliflower', unit: 'kg', ref: 44, gov: 38 },
    { n: 'Broccoli', unit: 'kg', ref: 130, gov: 110 }
  ]},
  { top: 'Fresh Vegetables', sub: 'Specialty Vegetables', products: [
    { n: 'Baby Corn', unit: 'kg', ref: 48, gov: 42 },
    { n: 'Mushroom', unit: 'kg', ref: 104, gov: 89 },
    { n: 'Zucchini', unit: 'kg', ref: 130, gov: 109 },
    { n: 'Lettuce', unit: 'piece', ref: 90, gov: 75 },
    { n: 'Celery', unit: 'bunch', ref: 130, gov: 109 },
    { n: 'Spring Onion', unit: 'bunch', ref: 30, gov: 25 }
  ]},
  { top: 'Fresh Vegetables', sub: 'Herbs & Fresh Greens', products: [
    { n: 'Garlic', unit: 'kg', ref: 186, gov: 159 },
    { n: 'Ginger', unit: 'kg', ref: 155, gov: 129 },
    { n: 'Raw Banana', unit: 'kg', ref: 10, gov: 9 },
    { n: 'Coconut', unit: 'piece', ref: 30, gov: 25 },
    { n: 'Banana Flower', unit: 'piece', ref: 25, gov: 20 }
  ]},
  { top: 'Fresh Fruits', sub: 'Everyday Fruits', products: [
    { n: 'Apple Shimla', unit: 'kg', ref: 163, gov: 145 },
    { n: 'Apple Washington', unit: 'kg', ref: 260, gov: 229 },
    { n: 'Banana Regular', unit: 'kg', ref: 61, gov: 52 },
    { n: 'Banana Morris', unit: 'kg', ref: 42, gov: 36 },
    { n: 'Orange', unit: 'kg', ref: 78, gov: 69 },
    { n: 'Papaya', unit: 'kg', ref: 49, gov: 41 },
    { n: 'Guava', unit: 'kg', ref: 74, gov: 64 },
    { n: 'Pineapple', unit: 'piece', ref: 50, gov: 42 },
    { n: 'Watermelon', unit: 'kg', ref: 40, gov: 32 }
  ]},
  { top: 'Fresh Fruits', sub: 'Premium Fruits', products: [
    { n: 'Green Apple', unit: 'kg', ref: 286, gov: 255 },
    { n: 'Pear', unit: 'kg', ref: 163, gov: 139 },
    { n: 'Pomegranate', unit: 'kg', ref: 150, gov: 129 },
    { n: 'Black Grapes', unit: 'kg', ref: 169, gov: 149 },
    { n: 'Green Grapes', unit: 'kg', ref: 163, gov: 149 },
    { n: 'Sapota', unit: 'kg', ref: 65, gov: 56 },
    { n: 'Custard Apple', unit: 'kg', ref: 73, gov: 64 }
  ]},
  { top: 'Fresh Fruits', sub: 'Citrus Fruits', products: [
    { n: 'Imported Orange', unit: 'kg', ref: 90, gov: 79 },
    { n: 'Amla', unit: 'kg', ref: 117, gov: 99 }
  ]},
  { top: 'Fresh Fruits', sub: 'Seasonal Fruits', products: [
    { n: 'Ripe Mango', unit: 'kg', ref: 215, gov: 189 },
    { n: 'Raw Mango', unit: 'kg', ref: 130, gov: 115 },
    { n: 'Jackfruit', unit: 'kg', ref: 189, gov: 165 },
    { n: 'Muskmelon', unit: 'kg', ref: 27, gov: 23 },
    { n: 'Lychee', unit: 'kg', ref: 312, gov: 279 },
    { n: 'Apricot', unit: 'kg', ref: 247, gov: 219 }
  ]},
  { top: 'Fresh Fruits', sub: 'Imported / Exotic Fruits', products: [
    { n: 'Avocado', unit: 'piece', ref: 140, gov: 119 },
    { n: 'Kiwi', unit: 'piece', ref: 55, gov: 45 },
    { n: 'Dragon Fruit', unit: 'kg', ref: 360, gov: 299 },
    { n: 'Cherries', unit: 'kg', ref: 750, gov: 649 },
    { n: 'Blueberries', unit: '125g pack', ref: 450, gov: 399 },
    { n: 'Strawberries', unit: '250g pack', ref: 280, gov: 229 },
    { n: 'Dates Fresh', unit: 'kg', ref: 450, gov: 399 }
  ]}
];

function getImage(name) {
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=random&color=fff&size=512`;
}

async function seed() {
  console.log('Seeding categories...');
  
  let catMap = {};
  
  for (const block of data) {
    const slug = block.sub.toLowerCase().replace(/[^a-z0-9]+/g, '-');
    const { data: ext, error: selErr } = await supabase.from('categories').select('*').eq('slug', slug).single();
    let catId;
    if (ext) {
      catId = ext.id;
      await supabase.from('categories').update({ description: block.top }).eq('id', catId);
    } else {
      const { data: ins, error: insErr } = await supabase.from('categories').insert([{
        name: block.sub,
        slug: slug,
        description: block.top,
        experience_type: 'FRESH'
      }]).select().single();
      if (insErr) { console.error('Error category', insErr); continue; }
      catId = ins.id;
    }
    catMap[block.sub] = catId;
    
    for (const p of block.products) {
      const pSlug = p.n.toLowerCase().replace(/[^a-z0-9]+/g, '-');
      const isExotic = ['Imported / Exotic Fruits', 'Specialty Vegetables'].includes(block.sub);
      const isSeasonal = ['Seasonal Fruits'].includes(block.sub);
      const isBestseller = ['Potato', 'Onion', 'Tomato', 'Banana Regular', 'Apple Shimla', 'Coriander'].includes(p.n);
      const isPremium = ['Premium Fruits'].includes(block.sub);
      
      const pData = {
        name: p.n,
        slug: pSlug,
        category_id: catId,
        price: p.ref,
        selling_price: p.gov,
        unit: p.unit,
        description: `Fresh ${p.n.toLowerCase()} selected for everyday cooking and snacking.`,
        stock_quantity: 100,
        experience_type: 'FRESH',
        active: true,
        bestseller: isBestseller,
        seasonal: isSeasonal,
        fresh_today: isBestseller,
        bundle_items: { exotic: isExotic, premium: isPremium }
      };
      
      const { data: pExt } = await supabase.from('products').select('id').eq('slug', pSlug).single();
      let prodId;
      if (pExt) {
        await supabase.from('products').update(pData).eq('id', pExt.id);
        prodId = pExt.id;
      } else {
        const { data: pIns, error: pInsErr } = await supabase.from('products').insert([pData]).select().single();
        if (pInsErr) { console.error('Error product', p.n, pInsErr); continue; }
        prodId = pIns.id;
      }
      
      const imgUrl = getImage(p.n);
      const { data: imgExt } = await supabase.from('product_images').select('id').eq('product_id', prodId).single();
      if (imgExt) {
        await supabase.from('product_images').update({ image_url: imgUrl }).eq('id', imgExt.id);
      } else {
        await supabase.from('product_images').insert([{ product_id: prodId, image_url: imgUrl }]);
      }
      console.log(`Seeded ${p.n}`);
    }
  }
  console.log('Seeding complete.');
}
seed();
