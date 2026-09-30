import json

with open(r'C:\Users\taras\.gemini\antigravity\brain\f1b0d9bd-f993-4422-b7ac-8a745a4369df\.system_generated\logs\transcript_full.jsonl', 'r', encoding='utf-8') as f:
    for line in f:
        try:
            data = json.loads(line)
            if 'tool_calls' in data:
                for call in data['tool_calls']:
                    if call['name'] in ['write_to_file', 'replace_file_content']:
                        args = call.get('args', {})
                        target = args.get('TargetFile', '')
                        if 'HomeScreen.kt' in target:
                            print(f"Step {data.get('step_index')}: {call['name']}")
                            if call['name'] == 'write_to_file':
                                with open('HomeScreen_recovered.kt', 'w', encoding='utf-8') as out:
                                    out.write(args.get('CodeContent', ''))
        except:
            pass
