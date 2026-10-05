const fs = require('fs');
const path = require('path');
const dir = path.join(__dirname, '../temp_images');
const files = fs.readdirSync(dir).filter(f => f.endsWith('.jpg'));

const html = `<!DOCTYPE html><html><head><style>
  body { font-family: system-ui, sans-serif; background: #0f172a; color: #fff; padding: 24px; }
  h2 { margin-bottom: 20px; color: #4ade80; }
  .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
  .card { background: #1e293b; border-radius: 12px; overflow: hidden; padding: 10px; border: 1px solid #334155; }
  img { width: 100%; height: 160px; object-fit: cover; border-radius: 8px; background: #334155; }
  .name { font-size: 13px; font-weight: 600; margin-top: 8px; color: #e2e8f0; }
  .meta { font-size: 11px; color: #94a3b8; margin-top: 2px; }
</style></head><body>
  <h2>Govind Products Visual Audit (Total: ${files.length})</h2>
  <div class='grid'>
    ${files.map(f => {
      const stat = fs.statSync(path.join(dir, f));
      return `<div class='card'>
        <img src='${f}' loading='lazy' alt='${f}' />
        <div class='name'>${f.replace('.jpg', '')}</div>
        <div class='meta'>${(stat.size / 1024).toFixed(1)} KB</div>
      </div>`;
    }).join('\n')}
  </div>
</body></html>`;

fs.writeFileSync(path.join(dir, 'gallery.html'), html);
console.log('Visual gallery created at temp_images/gallery.html with', files.length, 'images');
