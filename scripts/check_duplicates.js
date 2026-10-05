const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const dir = path.join(__dirname, '../temp_images');
if (!fs.existsSync(dir)) {
  console.log('temp_images does not exist');
  process.exit(0);
}

const files = fs.readdirSync(dir);
const hashes = {};
for (const file of files) {
  const buf = fs.readFileSync(path.join(dir, file));
  const hash = crypto.createHash('md5').update(buf).digest('hex');
  if (!hashes[hash]) hashes[hash] = [];
  hashes[hash].push({ file, size: buf.length });
}

console.log('Total files in temp_images:', files.length);
let dupSets = 0;
for (const [h, list] of Object.entries(hashes)) {
  if (list.length > 1) {
    dupSets++;
    console.log(`\nDuplicate MD5 (${h}, ${list[0].size} bytes):`);
    list.forEach(item => console.log('  -', item.file));
  }
}
console.log('\nTotal duplicate sets:', dupSets);
