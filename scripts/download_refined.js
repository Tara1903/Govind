const https = require('https');
const fs = require('fs');
const path = require('path');

const items = [
  { name: 'test_ginger.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f2/Fresh_Ginger.JPG/960px-Fresh_Ginger.JPG' },
  { name: 'test_green_grapes.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/49/Liat_Portal_for_Foodie_Disorder_-_Green_grapes.jpg/960px-Liat_Portal_for_Foodie_Disorder_-_Green_grapes.jpg' },
  { name: 'test_custard_apple.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c9/Sugar_apple%28fruit%29.jpg/960px-Sugar_apple%28fruit%29.jpg' },
  { name: 'test_yam.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8e/Elephant_foot_yam.jpg/960px-Elephant_foot_yam.jpg' },
  { name: 'test_dates_bowl.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8a/Bowl_of_Dates.jpg/960px-Bowl_of_Dates.jpg' },
  { name: 'test_deluxe_thali.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/61/%275%27_A_vegetarian_thali%2C_traditional_style_of_serving_a_meal_in_India.jpg/960px-%275%27_A_vegetarian_thali%2C_traditional_style_of_serving_a_meal_in_India.jpg' },
  { name: 'test_paneer_paratha.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ad/Aloo_Paratha_with_Butter_from_India.jpg/960px-Aloo_Paratha_with_Butter_from_India.jpg' },
  { name: 'test_wholesale_garlic.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/76/Liat_Portal_for_Foodie_Disorder_-_Fresh_Garlic.jpg/960px-Liat_Portal_for_Foodie_Disorder_-_Fresh_Garlic.jpg' },
  { name: 'test_wholesale_apple.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6b/Crate_of_apples_-_geograph.org.uk_-_3185804.jpg/960px-Crate_of_apples_-_geograph.org.uk_-_3185804.jpg' },
  { name: 'test_butter_beans.jpg', url: 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fb/Lima_beans_fresh.jpg/960px-Lima_beans_fresh.jpg' }
];

const destDir = path.resolve(__dirname, '../temp_candidates');

function download(url, dest) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'GovindProduceAudit/1.0 (taras@example.com)' } }, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return download(res.headers.location, dest).then(resolve).catch(reject);
      }
      if (res.statusCode !== 200) return reject(new Error(`Status ${res.statusCode}`));
      const file = fs.createWriteStream(dest);
      res.pipe(file);
      file.on('finish', () => file.close(resolve));
    }).on('error', reject);
  });
}

async function run() {
  for (const it of items) {
    const dest = path.join(destDir, it.name);
    try {
      await download(it.url, dest);
      console.log(`[OK] ${it.name} (${fs.statSync(dest).size} bytes)`);
    } catch (e) {
      console.error(`[FAIL] ${it.name}: ${e.message}`);
    }
  }
}

run();
