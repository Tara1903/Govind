const fs = require('fs');
const lines = fs.readFileSync('C:/Users/taras/.gemini/antigravity/brain/f1b0d9bd-f993-4422-b7ac-8a745a4369df/.system_generated/logs/transcript_full.jsonl', 'utf-8').split('\n');

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
                        console.log('Step ' + data.step_index + ': ' + call.name);
                        if (call.name === 'write_to_file') {
                            fs.writeFileSync('HomeScreen_recovered.kt', args.CodeContent);
                        } else if (call.name === 'replace_file_content') {
                            let content = fs.readFileSync('HomeScreen_recovered.kt', 'utf-8');
                            let targetContent = args.TargetContent.replace(/\r\n/g, '\n');
                            let replContent = args.ReplacementContent;
                            // A simple replace
                            content = content.replace(args.TargetContent, replContent);
                            fs.writeFileSync('HomeScreen_recovered.kt', content);
                        }
                    }
                }
            }
        }
    } catch (e) {}
}
