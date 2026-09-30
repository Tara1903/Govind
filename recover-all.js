const fs = require('fs');
const path = require('path');

const brainDir = 'C:/Users/taras/.gemini/antigravity/brain/';
const dirs = fs.readdirSync(brainDir);

let content = '';

for (const dir of dirs) {
    const fullLogPath = path.join(brainDir, dir, '.system_generated', 'logs', 'transcript_full.jsonl');
    if (fs.existsSync(fullLogPath)) {
        const lines = fs.readFileSync(fullLogPath, 'utf-8').split('\n');
        for (const line of lines) {
            if (!line) continue;
            try {
                const data = JSON.parse(line);
                if (data.tool_calls) {
                    for (const call of data.tool_calls) {
                        if (['write_to_file', 'replace_file_content'].includes(call.name)) {
                            const args = call.args || {};
                            const target = args.TargetFile || '';
                            if (target.includes('HomeScreen.kt')) {
                                console.log('[' + dir + '] Step ' + data.step_index + ': ' + call.name);
                                if (call.name === 'write_to_file') {
                                    content = args.CodeContent;
                                } else if (call.name === 'replace_file_content') {
                                    if (content) {
                                        content = content.replace(args.TargetContent, args.ReplacementContent);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e) {}
        }
    }
}
fs.writeFileSync('HomeScreen_fully_recovered.kt', content);
console.log('Recovery complete. Length:', content.length);
