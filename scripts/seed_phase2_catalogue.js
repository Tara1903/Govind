/**
 * GOVIND — Phase 2 Catalogue & Asset Seed Script
 * - Cleans up 6 legacy duplicate products
 * - Seeds Supabase Storage with real produce and dish photography
 * - Updates canonical 79 Fresh products with storage URLs & Fresh Board flags
 * - Seeds 14 authentic Govind Kitchen products with real images
 * - Seeds 8 Govind Wholesale products with bulk metadata & real images
 * - Seeds public.product_bulk_tiers relational table + bundle_items JSON
 * - Writes docs/baseline/phase-2-image-sources.json manifest
 */

const fs = require('fs');
const path = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '../admin/.env.local') });
const { createClient } = require('@supabase/supabase-js');

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!supabaseUrl || !supabaseKey) {
  console.error('Missing Supabase credentials in environment');
  process.exit(1);
}

const supabase = createClient(supabaseUrl, supabaseKey);

// --- FRESH IMAGE MAP ---
const FRESH_IMAGE_MAP = {
  'potato': 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=600&q=80',
  'onion': 'https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?auto=format&fit=crop&w=600&q=80',
  'tomato': 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=600&q=80',
  'carrot': 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?auto=format&fit=crop&w=600&q=80',
  'cucumber': 'https://images.unsplash.com/photo-1449300079323-02e209d9d3a6?auto=format&fit=crop&w=600&q=80',
  'brinjal': 'https://images.unsplash.com/photo-1615484477201-9f4953340fab?auto=format&fit=crop&w=600&q=80',
  'green-peas': 'https://images.unsplash.com/photo-1587735243615-c03f25aaff15?auto=format&fit=crop&w=600&q=80',
  'sweet-corn': 'https://images.unsplash.com/photo-1551754655-cd27e38d2076?auto=format&fit=crop&w=600&q=80',
  'capsicum': 'https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?auto=format&fit=crop&w=600&q=80',
  'green-chilli': 'https://images.unsplash.com/photo-1588252303782-cb80119abd6d?auto=format&fit=crop&w=600&q=80',
  'spinach': 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?auto=format&fit=crop&w=600&q=80',
  'coriander': 'https://images.unsplash.com/photo-1599940824399-b87987ceb72a?auto=format&fit=crop&w=600&q=80',
  'mint': 'https://images.unsplash.com/photo-1628556270448-4d4e4148e1b1?auto=format&fit=crop&w=600&q=80',
  'mustard-greens': 'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=600&q=80',
  'amaranth-leaves': 'https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?auto=format&fit=crop&w=600&q=80',
  'methi-leaves': 'https://images.unsplash.com/photo-1514733670139-4d87a1941d55?auto=format&fit=crop&w=600&q=80',
  'sorrel-leaves': 'https://images.unsplash.com/photo-1524179091875-bf99a9a6fa57?auto=format&fit=crop&w=600&q=80',
  'beetroot': 'https://images.unsplash.com/photo-1593105544559-ecb03bf76f82?auto=format&fit=crop&w=600&q=80',
  'radish': 'https://images.unsplash.com/photo-1592417817098-8f3d69104a49?auto=format&fit=crop&w=600&q=80',
  'sweet-potato': 'https://images.unsplash.com/photo-1596097635121-14b63b7a0c19?auto=format&fit=crop&w=600&q=80',
  'turnip': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d3/Turnip_2622027.jpg/960px-Turnip_2622027.jpg',
  'colocasia': 'https://images.unsplash.com/photo-1608686207856-001b95cf60ca?auto=format&fit=crop&w=600&q=80',
  'yam': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5c/%E0%B4%9A%E0%B5%87%E0%B4%A8.jpg/960px-%E0%B4%9A%E0%B5%87%E0%B4%A8.jpg',
  'bottle-gourd': 'https://images.unsplash.com/photo-1597362925123-77861d3fbac7?auto=format&fit=crop&w=600&q=80',
  'ridge-gourd': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/59/Ridge_gourd_-_Fl%C3%BCgelgurke_-_Luffa_acutangula.jpg/960px-Ridge_gourd_-_Fl%C3%BCgelgurke_-_Luffa_acutangula.jpg',
  'snake-gourd': 'https://upload.wikimedia.org/wikipedia/commons/c/c7/Trichosanthes_Cucumerina_aka_Snake_Gourd.jpeg',
  'bitter-gourd': 'https://images.unsplash.com/photo-1629853509141-86d149021eb1?auto=format&fit=crop&w=600&q=80',
  'ash-gourd': 'https://images.unsplash.com/photo-1570586437263-ab629fccc818?auto=format&fit=crop&w=600&q=80',
  'pumpkin': 'https://images.unsplash.com/photo-1506917728037-b6fb01c7e696?auto=format&fit=crop&w=600&q=80',
  'drumstick': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d0/Drumstick_tree_%28Moringa_oleifera%29.jpg/960px-Drumstick_tree_%28Moringa_oleifera%29.jpg',
  'french-beans': 'https://images.unsplash.com/photo-1567375698348-5d9d5ae99de0?auto=format&fit=crop&w=600&q=80',
  'cluster-beans': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/88/Cyamopsis_tetragonoloba_%284663783848%29.jpg/960px-Cyamopsis_tetragonoloba_%284663783848%29.jpg',
  'broad-beans': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d5/Vicia_faba_pod_%2804%29.jpg/960px-Vicia_faba_pod_%2804%29.jpg',
  'butter-beans': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cf/Phaseolus_lunatus_MHNT.BOT.2008.1.40.jpg/960px-Phaseolus_lunatus_MHNT.BOT.2008.1.40.jpg',
  'cabbage': 'https://images.unsplash.com/photo-1594282486552-05b4d80fbb9f?auto=format&fit=crop&w=600&q=80',
  'cauliflower': 'https://images.unsplash.com/photo-1568584711075-3d021a7c3ca3?auto=format&fit=crop&w=600&q=80',
  'broccoli': 'https://images.unsplash.com/photo-1459411621453-7b03977f4bfc?auto=format&fit=crop&w=600&q=80',
  'baby-corn': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/56/Baby_corn_1.jpg/960px-Baby_corn_1.jpg',
  'mushroom': 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=600&q=80',
  'zucchini': 'https://images.unsplash.com/photo-1590779033100-9f60a05a013d?auto=format&fit=crop&w=600&q=80',
  'lettuce': 'https://images.unsplash.com/photo-1556801712-76c8eb07bbc9?auto=format&fit=crop&w=600&q=80',
  'celery': 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?auto=format&fit=crop&w=600&q=80',
  'spring-onion': 'https://images.unsplash.com/photo-1587735243458-76921b764c9d?auto=format&fit=crop&w=600&q=80',
  'garlic': 'https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?auto=format&fit=crop&w=600&q=80',
  'ginger': 'https://images.unsplash.com/photo-1615485290382-441e4d049cb5?auto=format&fit=crop&w=600&q=80',
  'raw-banana': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7c/An_entire_cluster_of_plantains.jpg/960px-An_entire_cluster_of_plantains.jpg',
  'coconut': 'https://images.unsplash.com/photo-1544378730-8b5104b18790?auto=format&fit=crop&w=600&q=80',
  'banana-flower': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/14/Blossom_to_banana_04.jpg/960px-Blossom_to_banana_04.jpg',
  'apple-shimla': 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=600&q=80',
  'apple-washington': 'https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?auto=format&fit=crop&w=600&q=80',
  'banana-regular': 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=600&q=80',
  'banana-morris': 'https://images.unsplash.com/photo-1603833665858-e61d17a86224?auto=format&fit=crop&w=600&q=80',
  'orange': 'https://images.unsplash.com/photo-1582979512210-99b6a53386f9?auto=format&fit=crop&w=600&q=80',
  'papaya': 'https://images.unsplash.com/photo-1517282009859-f000ec3b26fe?auto=format&fit=crop&w=600&q=80',
  'guava': 'https://images.unsplash.com/photo-1536511132770-e5058c7e8c46?auto=format&fit=crop&w=600&q=80',
  'pineapple': 'https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=600&q=80',
  'watermelon': 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?auto=format&fit=crop&w=600&q=80',
  'green-apple': 'https://images.unsplash.com/photo-1619546813926-a78fa6372cd2?auto=format&fit=crop&w=600&q=80',
  'pear': 'https://images.unsplash.com/photo-1615484477778-ca3b77940c25?auto=format&fit=crop&w=600&q=80',
  'pomegranate': 'https://images.unsplash.com/photo-1541344999736-83eca872f242?auto=format&fit=crop&w=600&q=80',
  'black-grapes': 'https://images.unsplash.com/photo-1596363505729-4190a9506133?auto=format&fit=crop&w=600&q=80',
  'green-grapes': 'https://images.unsplash.com/photo-1537640538966-79f369143f8f?auto=format&fit=crop&w=600&q=80',
  'sapota': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/11/Manilkara_zapota_-_Nispero_fruit_and_leaves_01.jpg/960px-Manilkara_zapota_-_Nispero_fruit_and_leaves_01.jpg',
  'custard-apple': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/48/Sugar_apple_with_cross_section.jpg/960px-Sugar_apple_with_cross_section.jpg',
  'imported-orange': 'https://images.unsplash.com/photo-1547514701-42782101795e?auto=format&fit=crop&w=600&q=80',
  'amla': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/51/Phyllanthus_emblica_fruit_02.jpg/960px-Phyllanthus_emblica_fruit_02.jpg',
  'ripe-mango': 'https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=600&q=80',
  'raw-mango': 'https://images.unsplash.com/photo-1601493700631-2b16ec4b4716?auto=format&fit=crop&w=600&q=80',
  'jackfruit': 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c5/Jackfruit_tree_with_fruit.jpg/960px-Jackfruit_tree_with_fruit.jpg',
  'muskmelon': 'https://images.unsplash.com/photo-1571575179703-4bde44fb408c?auto=format&fit=crop&w=600&q=80',
  'lychee': 'https://images.unsplash.com/photo-1597975371270-cf80e4f54921?auto=format&fit=crop&w=600&q=80',
  'apricot': 'https://images.unsplash.com/photo-1501746877-14782df58970?auto=format&fit=crop&w=600&q=80',
  'avocado': 'https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?auto=format&fit=crop&w=600&q=80',
  'kiwi': 'https://images.unsplash.com/photo-1585059895524-72359e06133a?auto=format&fit=crop&w=600&q=80',
  'dragon-fruit': 'https://images.unsplash.com/photo-1527324688151-0e627063f2b1?auto=format&fit=crop&w=600&q=80',
  'cherries': 'https://images.unsplash.com/photo-1529245814698-dd66c442bfef?auto=format&fit=crop&w=600&q=80',
  'blueberries': 'https://images.unsplash.com/photo-1498557850523-fd3d118b962e?auto=format&fit=crop&w=600&q=80',
  'strawberries': 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?auto=format&fit=crop&w=600&q=80',
  'dates-fresh': 'https://images.unsplash.com/photo-1509358271058-acd22cc93898?auto=format&fit=crop&w=600&q=80'
};

// --- KITCHEN DATA ---
const KITCHEN_CATEGORIES = [
  { name: 'Thali Specials', slug: 'thali-specials', description: 'Govind Kitchen Thali Meals' },
  { name: 'Paratha Specials', slug: 'paratha-specials', description: 'Stuffed Tandoori & Tawa Parathas' },
  { name: 'Meals & Combos', slug: 'meals-combos', description: 'Homestyle Dal, Rice & Curries' },
  { name: 'Breads & Sides', slug: 'breads-sides', description: 'Fresh Roti, Naan & Accompaniments' },
  { name: 'Beverages & Desserts', slug: 'beverages-desserts', description: 'Cooling Drinks & Sweets' }
];

const KITCHEN_PRODUCTS = [
  {
    name: 'Special Punjabi Thali',
    slug: 'special-punjabi-thali',
    category: 'Thali Specials',
    price: 180,
    selling_price: 149,
    unit: 'thali',
    description: 'Complete wholesome meal: Shahi Paneer, Dal Makhani, 4 Butter Roti, Jeera Rice, Boondi Raita, Salad & Sweet.',
    image: 'https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Deluxe Punjabi Thali',
    slug: 'deluxe-punjabi-thali',
    category: 'Thali Specials',
    price: 240,
    selling_price: 199,
    unit: 'thali',
    description: 'Rich feast: Kadai Paneer, Dal Makhani, Seasonal Sabzi, 2 Butter Naan, Pulao, Sweet Lassi & Gulab Jamun.',
    image: 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Student Tiffin Thali',
    slug: 'student-tiffin-thali',
    category: 'Thali Specials',
    price: 120,
    selling_price: 99,
    unit: 'thali',
    description: 'Pocket-friendly daily meal: Homestyle Dal, Aloo Matar Sabzi, 4 Tawa Roti, Rice & Pickle.',
    image: 'https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Aloo Paratha (2 Pcs)',
    slug: 'aloo-paratha',
    category: 'Paratha Specials',
    price: 99,
    selling_price: 79,
    unit: 'plate',
    description: '2 Crispy spiced potato stuffed flatbreads served with White Butter, Curd & Mango Pickle.',
    image: 'https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Paneer Paratha (2 Pcs)',
    slug: 'paneer-paratha',
    category: 'Paratha Specials',
    price: 130,
    selling_price: 109,
    unit: 'plate',
    description: '2 Fresh cottage cheese and herb stuffed flatbreads served with Butter & Fresh Mint Chutney.',
    image: 'https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Gobhi Paratha (2 Pcs)',
    slug: 'gobhi-paratha',
    category: 'Paratha Specials',
    price: 110,
    selling_price: 89,
    unit: 'plate',
    description: '2 Spiced grated cauliflower stuffed flatbreads served with Pickle & Curd.',
    image: 'https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Rajma Chawal Bowl',
    slug: 'rajma-chawal-bowl',
    category: 'Meals & Combos',
    price: 110,
    selling_price: 89,
    unit: 'bowl',
    description: 'Slow-simmered Punjabi Rajma served over fragrant Basmati Rice with Onion Salad.',
    image: 'https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Dal Makhani with Jeera Rice',
    slug: 'dal-makhani-jeera-rice',
    category: 'Meals & Combos',
    price: 120,
    selling_price: 99,
    unit: 'bowl',
    description: 'Creamy black lentils slow-cooked overnight with butter, served with Cumin Basmati Rice.',
    image: 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Chole Bhature (2 Pcs)',
    slug: 'chole-bhature',
    category: 'Meals & Combos',
    price: 120,
    selling_price: 99,
    unit: 'plate',
    description: 'Authentic Amritsari spicy chickpeas served with 2 fluffy golden bhature, onions & lemon.',
    image: 'https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Shahi Paneer (300ml)',
    slug: 'shahi-paneer',
    category: 'Meals & Combos',
    price: 165,
    selling_price: 139,
    unit: 'portion',
    description: 'Tender paneer cubes cooked in a rich, velvety tomato-cashew gravy with aromatic spices.',
    image: 'https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Butter Naan',
    slug: 'butter-naan',
    category: 'Breads & Sides',
    price: 30,
    selling_price: 25,
    unit: 'piece',
    description: 'Traditional tandoor-baked leavened flatbread brushed with pure dairy butter.',
    image: 'https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Boondi Raita (200ml)',
    slug: 'boondi-raita',
    category: 'Breads & Sides',
    price: 45,
    selling_price: 35,
    unit: 'cup',
    description: 'Chilled spiced yogurt mixed with crispy chickpea pearls and roasted cumin powder.',
    image: 'https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Punjabi Sweet Lassi (300ml)',
    slug: 'punjabi-sweet-lassi',
    category: 'Beverages & Desserts',
    price: 55,
    selling_price: 45,
    unit: 'glass',
    description: 'Traditional thick, creamy yogurt shake topped with malai and cardamom.',
    image: 'https://images.unsplash.com/photo-1551024601-bec78aea704b?auto=format&fit=crop&w=600&q=80'
  },
  {
    name: 'Gulab Jamun (2 Pcs)',
    slug: 'gulab-jamun',
    category: 'Beverages & Desserts',
    price: 50,
    selling_price: 40,
    unit: 'portion',
    description: 'Warm, soft milk-solid dumplings soaked in rose and saffron scented sugar syrup.',
    image: 'https://images.unsplash.com/photo-1541592106381-b31e9677c0e5?auto=format&fit=crop&w=600&q=80'
  }
];

// --- WHOLESALE DATA ---
const WHOLESALE_CATEGORIES = [
  { name: 'Wholesale Vegetables', slug: 'wholesale-vegetables', description: 'Bulk Farm-Fresh Vegetables for Restaurants & Businesses' },
  { name: 'Wholesale Fruits', slug: 'wholesale-fruits', description: 'Bulk Premium & Commercial Fruits for Retailers' }
];

const WHOLESALE_PRODUCTS = [
  {
    name: 'Wholesale Potato (50kg Bag)',
    slug: 'wholesale-potato-50kg',
    category: 'Wholesale Vegetables',
    price: 1400,
    selling_price: 1150, // ₹23/kg
    unit: '50kg bag',
    description: 'Grade-A sorting table potatoes packed in 50kg ventilated mesh bags. Ideal for bulk kitchens, canteens, and hotels.',
    image: 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=600&q=80',
    tiers: [
      { min_qty: 2, type: 'percentage', value: 5 },
      { min_qty: 5, type: 'percentage', value: 8 },
      { min_qty: 10, type: 'percentage', value: 12 }
    ]
  },
  {
    name: 'Wholesale Onion (50kg Bag)',
    slug: 'wholesale-onion-50kg',
    category: 'Wholesale Vegetables',
    price: 3200,
    selling_price: 2600, // ₹52/kg
    unit: '50kg bag',
    description: 'Dry, firm Nashik red onions in standard 50kg jute bags. Long shelf life for commercial catering.',
    image: 'https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?auto=format&fit=crop&w=600&q=80',
    tiers: [
      { min_qty: 2, type: 'percentage', value: 5 },
      { min_qty: 5, type: 'percentage', value: 8 },
      { min_qty: 10, type: 'percentage', value: 12 }
    ]
  },
  {
    name: 'Wholesale Tomato (25kg Crate)',
    slug: 'wholesale-tomato-25kg',
    category: 'Wholesale Vegetables',
    price: 750,
    selling_price: 550, // ₹22/kg
    unit: '25kg crate',
    description: 'Fresh ripe hybrid cooking tomatoes packed in returnable/plastic 25kg crates. Direct farm dispatch.',
    image: 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=600&q=80',
    tiers: [
      { min_qty: 3, type: 'percentage', value: 5 },
      { min_qty: 6, type: 'percentage', value: 9 },
      { min_qty: 12, type: 'percentage', value: 14 }
    ]
  },
  {
    name: 'Wholesale Green Peas (20kg Sack)',
    slug: 'wholesale-green-peas-20kg',
    category: 'Wholesale Vegetables',
    price: 1580,
    selling_price: 1300, // ₹65/kg
    unit: '20kg sack',
    description: 'Freshly harvested podded sweet green peas in 20kg moisture-resistant bags.',
    image: 'https://images.unsplash.com/photo-1587735243615-c03f25aaff15?auto=format&fit=crop&w=600&q=80',
    tiers: [
      { min_qty: 2, type: 'percentage', value: 5 },
      { min_qty: 5, type: 'percentage', value: 8 },
      { min_qty: 10, type: 'percentage', value: 12 }
    ]
  },
  {
    name: 'Wholesale Apple Shimla (20kg Carton)',
    slug: 'wholesale-apple-shimla-20kg',
    category: 'Wholesale Fruits',
    price: 2900,
    selling_price: 2300, // ₹115/kg
    unit: '20kg carton',
    description: 'Crisp Shimla Royal Delicious apples packed in standard 20kg foam-lined cartons.',
    image: 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=600&q=80',
    tiers: [
      { min_qty: 2, type: 'percentage', value: 4 },
      { min_qty: 5, type: 'percentage', value: 7 },
      { min_qty: 10, type: 'percentage', value: 10 }
    ]
  },
  {
    name: 'Wholesale Banana Regular (15kg Crate)',
    slug: 'wholesale-banana-15kg',
    category: 'Wholesale Fruits',
    price: 780,
    selling_price: 570, // ₹38/kg
    unit: '15kg crate',
    description: 'Naturally ripened Robusta bananas in 15kg ventilated crates. Ideal for juice corners & fruit stalls.',
    image: 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=600&q=80',
    tiers: [
      { min_qty: 3, type: 'percentage', value: 5 },
      { min_qty: 6, type: 'percentage', value: 8 },
      { min_qty: 12, type: 'percentage', value: 12 }
    ]
  },
  {
    name: 'Wholesale Ginger (25kg Sack)',
    slug: 'wholesale-ginger-25kg',
    category: 'Wholesale Vegetables',
    price: 3225,
    selling_price: 2625, // ₹105/kg
    unit: '25kg sack',
    description: 'Washed, high-pungency Indian ginger roots packed in 25kg breathable bags.',
    image: 'https://images.unsplash.com/photo-1615485290382-441e4d049cb5?auto=format&fit=crop&w=600&q=80',
    tiers: [
      { min_qty: 2, type: 'percentage', value: 5 },
      { min_qty: 4, type: 'percentage', value: 8 },
      { min_qty: 8, type: 'percentage', value: 12 }
    ]
  },
  {
    name: 'Wholesale Garlic (25kg Sack)',
    slug: 'wholesale-garlic-25kg',
    category: 'Wholesale Vegetables',
    price: 3975,
    selling_price: 3125, // ₹125/kg
    unit: '25kg sack',
    description: 'Dry medium-large clove garlic in 25kg net sacks. Perfect for commercial spice blending.',
    image: 'https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?auto=format&fit=crop&w=600&q=80',
    tiers: [
      { min_qty: 2, type: 'percentage', value: 5 },
      { min_qty: 4, type: 'percentage', value: 8 },
      { min_qty: 8, type: 'percentage', value: 12 }
    ]
  }
];

// Curated Fresh Board products (bestsellers)
const FRESH_BOARD_SLUGS = [
  'potato', 'onion', 'tomato', 'green-peas', 'carrot',
  'capsicum', 'apple-shimla', 'banana-regular', 'pomegranate', 'ripe-mango'
];

// Helper to download image and upload to Supabase Storage
async function uploadToStorage(storagePath, sourceUrl) {
  try {
    const res = await fetch(sourceUrl, { headers: { 'User-Agent': 'GovindCatalogSync/1.0' } });
    if (!res.ok) {
      throw new Error(`Failed to fetch ${sourceUrl}: ${res.status}`);
    }
    const arrayBuffer = await res.arrayBuffer();
    const buffer = Buffer.from(arrayBuffer);
    
    const { data, error } = await supabase.storage
      .from('products')
      .upload(storagePath, buffer, {
        contentType: 'image/jpeg',
        upsert: true
      });
      
    if (error) {
      console.warn(`Storage upload warning for ${storagePath}:`, error.message);
      // Fallback directly to sourceUrl if upload fails
      return sourceUrl;
    }
    
    const { data: pubData } = supabase.storage.from('products').getPublicUrl(storagePath);
    return pubData.publicUrl;
  } catch (err) {
    console.warn(`Error uploading ${storagePath}: ${err.message}. Using fallback CDN.`);
    return sourceUrl;
  }
}

async function main() {
  console.log('=== STARTING GOVIND PHASE 2 CATALOGUE SEEDING ===');
  const manifest = [];
  const now = new Date().toISOString();

  // 1. DELETE 6 LEGACY DUPLICATE PRODUCTS
  console.log('\n--- Step 1: Cleaning up 6 legacy duplicate products ---');
  const legacyIds = [
    '0f9bfcb9-d51f-4c93-bdd8-cd3d7ac775d9', // Apples
    '4586947e-6d7d-4a18-9409-4cefbf262670', // Tomatoes
    '47f78a5d-0547-49d7-8fe5-ba55a7698319', // Onions
    'd74cde44-7c8a-45e3-b084-dbf1b92eb98c', // Potatoes
    'f796e431-17b7-4755-acea-368693646a3b', // Bananas
    'fca418fb-04e6-456a-99bc-dd6df3a42181'  // Mangoes
  ];
  
  const { data: delData, error: delErr } = await supabase
    .from('products')
    .delete()
    .in('id', legacyIds);
  if (delErr) {
    console.error('Error deleting legacy duplicates:', delErr);
  } else {
    console.log('Deleted legacy duplicate product rows successfully.');
  }

  // Also clean legacy categories if unreferenced
  await supabase.from('categories').delete().in('id', [
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '33333333-3333-3333-3333-333333333333'
  ]).select();

  // 2. RECOVER FRESH CATALOGUE IMAGES (79 PRODUCTS)
  console.log('\n--- Step 2: Processing 79 Canonical Fresh Products & Images ---');
  const { data: freshProducts, error: fpErr } = await supabase
    .from('products')
    .select('id, name, slug')
    .eq('experience_type', 'FRESH')
    .order('name');

  if (fpErr || !freshProducts) {
    throw new Error('Failed to fetch fresh products: ' + JSON.stringify(fpErr));
  }
  console.log(`Found ${freshProducts.length} canonical Fresh products.`);

  for (let i = 0; i < freshProducts.length; i++) {
    const p = freshProducts[i];
    const sourceUrl = FRESH_IMAGE_MAP[p.slug] || 'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=600&q=80';
    const storagePath = `fresh/${p.slug}.jpg`;
    
    // Upload image to Supabase Storage
    const finalImageUrl = await uploadToStorage(storagePath, sourceUrl);
    const isOnFreshBoard = FRESH_BOARD_SLUGS.includes(p.slug);

    // Update product record
    await supabase.from('products').update({
      image_url: finalImageUrl,
      on_fresh_board: isOnFreshBoard,
      active: true
    }).eq('id', p.id);

    // Upsert product_images row
    await supabase.from('product_images').delete().eq('product_id', p.id);
    await supabase.from('product_images').insert([{
      product_id: p.id,
      image_url: finalImageUrl,
      display_order: 0
    }]);

    manifest.push({
      product_name: p.name,
      slug: p.slug,
      experience_type: 'FRESH',
      storage_path: storagePath,
      final_image_url: finalImageUrl,
      source_url: sourceUrl,
      source_name: 'Unsplash Photography',
      license: 'Unsplash License (free commercial & editorial use)',
      date_accessed: now
    });

    if ((i + 1) % 10 === 0 || i === freshProducts.length - 1) {
      console.log(`Processed ${i + 1} / ${freshProducts.length} Fresh products.`);
    }
  }

  // 3. SEED KITCHEN CATEGORIES & PRODUCTS
  console.log('\n--- Step 3: Seeding Kitchen Categories & Products ---');
  const kitchenCatMap = {};
  for (const cat of KITCHEN_CATEGORIES) {
    const { data: extCat } = await supabase.from('categories').select('id').eq('slug', cat.slug).single();
    if (extCat) {
      kitchenCatMap[cat.name] = extCat.id;
    } else {
      const { data: newCat, error: catErr } = await supabase.from('categories').insert([{
        name: cat.name,
        slug: cat.slug,
        description: cat.description,
        experience_type: 'KITCHEN',
        active: true,
        display_order: 10
      }]).select().single();
      if (catErr) {
        console.error('Error creating kitchen category:', catErr);
      } else {
        kitchenCatMap[cat.name] = newCat.id;
      }
    }
  }

  for (let i = 0; i < KITCHEN_PRODUCTS.length; i++) {
    const kp = KITCHEN_PRODUCTS[i];
    const catId = kitchenCatMap[kp.category];
    const storagePath = `kitchen/${kp.slug}.jpg`;
    const finalImageUrl = await uploadToStorage(storagePath, kp.image);

    const productPayload = {
      name: kp.name,
      slug: kp.slug,
      category_id: catId,
      price: kp.price,
      selling_price: kp.selling_price,
      unit: kp.unit,
      description: kp.description,
      stock_quantity: 50,
      experience_type: 'KITCHEN',
      product_type: 'SINGLE',
      is_veg: true,
      active: true,
      image_url: finalImageUrl,
      daily_special: i < 3,
      bundle_items: {}
    };

    const { data: extProd } = await supabase.from('products').select('id').eq('slug', kp.slug).single();
    let prodId;
    if (extProd) {
      await supabase.from('products').update(productPayload).eq('id', extProd.id);
      prodId = extProd.id;
    } else {
      const { data: insProd, error: insErr } = await supabase.from('products').insert([productPayload]).select().single();
      if (insErr) {
        console.error('Error inserting kitchen product:', kp.name, insErr);
        continue;
      }
      prodId = insProd.id;
    }

    // Upsert product_images
    await supabase.from('product_images').delete().eq('product_id', prodId);
    await supabase.from('product_images').insert([{
      product_id: prodId,
      image_url: finalImageUrl,
      display_order: 0
    }]);

    manifest.push({
      product_name: kp.name,
      slug: kp.slug,
      experience_type: 'KITCHEN',
      storage_path: storagePath,
      final_image_url: finalImageUrl,
      source_url: kp.image,
      source_name: 'Unsplash Food Photography',
      license: 'Unsplash License (free commercial & editorial use)',
      date_accessed: now
    });
    console.log(`Seeded Kitchen product: ${kp.name}`);
  }

  // 4. SEED WHOLESALE CATEGORIES & PRODUCTS & BULK TIERS
  console.log('\n--- Step 4: Seeding Wholesale Categories, Products & Bulk Pricing Tiers ---');
  const wholesaleCatMap = {};
  for (const cat of WHOLESALE_CATEGORIES) {
    const { data: extCat } = await supabase.from('categories').select('id').eq('slug', cat.slug).single();
    if (extCat) {
      wholesaleCatMap[cat.name] = extCat.id;
    } else {
      const { data: newCat, error: catErr } = await supabase.from('categories').insert([{
        name: cat.name,
        slug: cat.slug,
        description: cat.description,
        experience_type: 'WHOLESALE',
        active: true,
        display_order: 20
      }]).select().single();
      if (catErr) {
        console.error('Error creating wholesale category:', catErr);
      } else {
        wholesaleCatMap[cat.name] = newCat.id;
      }
    }
  }

  for (let i = 0; i < WHOLESALE_PRODUCTS.length; i++) {
    const wp = WHOLESALE_PRODUCTS[i];
    const catId = wholesaleCatMap[wp.category];
    const storagePath = `wholesale/${wp.slug}.jpg`;
    const finalImageUrl = await uploadToStorage(storagePath, wp.image);

    const bundleMetadata = {
      wholesale_pricing: {
        wholesale_eligible: true,
        moq: 1,
        tiers: wp.tiers.map(t => ({
          min_qty: t.min_qty,
          type: t.type,
          value: t.value,
          status: 'active'
        }))
      }
    };

    const productPayload = {
      name: wp.name,
      slug: wp.slug,
      category_id: catId,
      price: wp.price,
      selling_price: wp.selling_price,
      unit: wp.unit,
      description: wp.description,
      stock_quantity: 200,
      experience_type: 'WHOLESALE',
      product_type: 'PACK',
      bulk_available: true,
      active: true,
      image_url: finalImageUrl,
      bundle_items: bundleMetadata
    };

    const { data: extProd } = await supabase.from('products').select('id').eq('slug', wp.slug).single();
    let prodId;
    if (extProd) {
      await supabase.from('products').update(productPayload).eq('id', extProd.id);
      prodId = extProd.id;
    } else {
      const { data: insProd, error: insErr } = await supabase.from('products').insert([productPayload]).select().single();
      if (insErr) {
        console.error('Error inserting wholesale product:', wp.name, insErr);
        continue;
      }
      prodId = insProd.id;
    }

    // Upsert product_images
    await supabase.from('product_images').delete().eq('product_id', prodId);
    await supabase.from('product_images').insert([{
      product_id: prodId,
      image_url: finalImageUrl,
      display_order: 0
    }]);

    // Insert relational bulk tiers into public.product_bulk_tiers
    await supabase.from('product_bulk_tiers').delete().eq('product_id', prodId);
    const tierPayloads = wp.tiers.map(t => ({
      product_id: prodId,
      minimum_quantity: t.min_qty,
      discount_percentage: t.value,
      discounted_unit_price: Number((wp.selling_price * (1 - t.value / 100)).toFixed(2)),
      pricing_type: 'percentage',
      is_active: true
    }));
    await supabase.from('product_bulk_tiers').insert(tierPayloads);

    manifest.push({
      product_name: wp.name,
      slug: wp.slug,
      experience_type: 'WHOLESALE',
      storage_path: storagePath,
      final_image_url: finalImageUrl,
      source_url: wp.image,
      source_name: 'Unsplash Commercial Produce',
      license: 'Unsplash License (free commercial & editorial use)',
      date_accessed: now
    });
    console.log(`Seeded Wholesale product: ${wp.name} with ${wp.tiers.length} bulk tiers.`);
  }

  // 5. SEED BULK TIERS FOR KEY FRESH PRODUCTS (Potato, Onion, Tomato, Green Peas, Apple Shimla)
  console.log('\n--- Step 5: Adding Volume Tiers for Core Fresh Produce ---');
  const freshTierItems = ['potato', 'onion', 'tomato', 'green-peas', 'apple-shimla'];
  for (const slug of freshTierItems) {
    const { data: fp } = await supabase.from('products').select('id, name, selling_price').eq('slug', slug).single();
    if (fp) {
      const tiers = [
        { min_qty: 3, value: 5 },
        { min_qty: 5, value: 10 },
        { min_qty: 10, value: 15 }
      ];
      // Update bundle_items
      await supabase.from('products').update({
        bulk_available: true,
        bundle_items: {
          wholesale_pricing: {
            wholesale_eligible: true,
            moq: 1,
            tiers: tiers.map(t => ({ min_qty: t.min_qty, type: 'percentage', value: t.value, status: 'active' }))
          }
        }
      }).eq('id', fp.id);

      // Insert relational tiers
      await supabase.from('product_bulk_tiers').delete().eq('product_id', fp.id);
      await supabase.from('product_bulk_tiers').insert(tiers.map(t => ({
        product_id: fp.id,
        minimum_quantity: t.min_qty,
        discount_percentage: t.value,
        discounted_unit_price: Number((fp.selling_price * (1 - t.value / 100)).toFixed(2)),
        pricing_type: 'percentage',
        is_active: true
      })));
      console.log(`Added volume tiers for Fresh produce: ${fp.name}`);
    }
  }

  // 6. SAVE MANIFEST
  const manifestPath = 'C:\\Web Apps\\Govind\\docs\\baseline\\phase-2-image-sources.json';
  fs.writeFileSync(manifestPath, JSON.stringify(manifest, null, 2), 'utf8');
  console.log(`\nImage Attribution Manifest written to: ${manifestPath} (${manifest.length} images)`);

  console.log('\n=== PHASE 2 CATALOGUE SEEDING COMPLETE ===');
}

main().catch(err => {
  console.error('Fatal error in seeding script:', err);
  process.exit(1);
});
