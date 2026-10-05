const https = require('https');
const fs = require('fs');
const path = require('path');

const candidates = [
  { slug: 'special-punjabi-thali', exp: 'kitchen', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/8b/North_Indian_Vegetarian_Thali-MB51.jpg/960px-North_Indian_Vegetarian_Thali-MB51.jpg' },
  { slug: 'deluxe-punjabi-thali', exp: 'kitchen', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/My_traditional_Indian_thali_meal_%2848625245542%29.jpg/960px-My_traditional_Indian_thali_meal_%2848625245542%29.jpg' },
  { slug: 'chole-bhature', exp: 'kitchen', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/aa/Chole_Bhature_1.jpg/960px-Chole_Bhature_1.jpg' },
  { slug: 'gulab-jamun', exp: 'kitchen', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/6/61/Gulab_Jamun_as_a_Diwali_Sweet.jpg/960px-Gulab_Jamun_as_a_Diwali_Sweet.jpg' },
  { slug: 'punjabi-sweet-lassi', exp: 'kitchen', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/59/Lassi_1.jpg/960px-Lassi_1.jpg' },
  { slug: 'paneer-paratha', exp: 'kitchen', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0b/Paratha_is_a_dough_fried_flatbread_native_to_India_and_Pakistan.jpg/960px-Paratha_is_a_dough_fried_flatbread_native_to_India_and_Pakistan.jpg' },
  { slug: 'green-peas', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Green_pea_pods.jpg/960px-Green_pea_pods.jpg' },
  { slug: 'green-apple', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ed/Granny_Smith_Apples.jpg/960px-Granny_Smith_Apples.jpg' },
  { slug: 'green-chilli', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/Green_chili_pepper.jpg/960px-Green_chili_pepper.jpg' },
  { slug: 'green-grapes', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/%22Bunch_of_Green_Grapes_in_Salem%22.jpg/960px-%22Bunch_of_Green_Grapes_in_Salem%22.jpg' },
  { slug: 'coriander', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Bunches_of_coriander_leaves.jpg/960px-Bunches_of_coriander_leaves.jpg' },
  { slug: 'dates-fresh', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/dc/Kajur.jpg/960px-Kajur.jpg' },
  { slug: 'coconut', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f1/Coconuts_-_single_and_cracked_open.jpg/960px-Coconuts_-_single_and_cracked_open.jpg' },
  { slug: 'bottle-gourd', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c3/Bottle_Gourd_-_Flickr_-_nekonomania.jpg/960px-Bottle_Gourd_-_Flickr_-_nekonomania.jpg' },
  { slug: 'zucchini', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/Calabac%C3%ADn%2C_M%C3%BAnich%2C_Alemania%2C_2013-03-30%2C_DD_01.JPG/960px-Calabac%C3%ADn%2C_M%C3%BAnich%2C_Alemania%2C_2013-03-30%2C_DD_01.JPG' },
  { slug: 'raw-mango', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/d1/Raw_mango_2.jpg/960px-Raw_mango_2.jpg' },
  { slug: 'drumstick', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/81/Moringa_oleifera_drumstick_pods.JPG/960px-Moringa_oleifera_drumstick_pods.JPG' },
  { slug: 'yam', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f8/Amorphophallus_paeoniifolius_Farmingage072.jpg/960px-Amorphophallus_paeoniifolius_Farmingage072.jpg' },
  { slug: 'raw-banana', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/7c/An_entire_cluster_of_plantains.jpg/960px-An_entire_cluster_of_plantains.jpg' },
  { slug: 'cluster-beans', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Cluster_bean_cluster.jpg/960px-Cluster_bean_cluster.jpg' },
  { slug: 'custard-apple', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/48/Sugar_apple_with_cross_section.jpg/960px-Sugar_apple_with_cross_section.jpg' },
  { slug: 'ridge-gourd', exp: 'fresh', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/86/Luffa_acutangula1.jpg/960px-Luffa_acutangula1.jpg' },
  { slug: 'wholesale-ginger-25kg', exp: 'wholesale', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/57/M%C3%BCnster%2C_Wochenmarkt_--_2015_--_7410.jpg/960px-M%C3%BCnster%2C_Wochenmarkt_--_2015_--_7410.jpg' },
  { slug: 'wholesale-green-peas-20kg', exp: 'wholesale', url: 'https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Erbsen-Frischmarkt-Poolkiste-Josef_Schlaghecken.jpg/960px-Erbsen-Frischmarkt-Poolkiste-Josef_Schlaghecken.jpg' },
];

const testDir = path.resolve(__dirname, '../temp_candidates');
if (!fs.existsSync(testDir)) fs.mkdirSync(testDir, { recursive: true });

function downloadBuffer(url) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'GovindProduceAudit/1.0 (taras@example.com)' } }, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return downloadBuffer(res.headers.location).then(resolve).catch(reject);
      }
      if (res.statusCode !== 200) {
        return reject(new Error(`Status ${res.statusCode}`));
      }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve(Buffer.concat(chunks)));
    }).on('error', reject);
  });
}

async function run() {
  for (const c of candidates) {
    const dest = path.join(testDir, `${c.exp}_${c.slug}.jpg`);
    try {
      const buf = await downloadBuffer(c.url);
      fs.writeFileSync(dest, buf);
      console.log(`[OK] ${c.slug}: ${buf.length} bytes`);
    } catch (e) {
      console.error(`[FAIL] ${c.slug}: ${e.message}`);
    }
  }
}

run();
