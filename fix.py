import sys

file_path = r'c:\Web Apps\Govind\android\app\src\main\java\com\example\govind\ui\features\home\HomeScreen.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

import re
# Find HomeTopBar function
pattern = r'(@Composable\s+fun HomeTopBar.*?)\s+Spacer\(modifier = Modifier.height\(16.dp\)\)'
replacement = '''@Composable
fun HomeTopBar(onSearchClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.govind.R.drawable.ic_launcher_squircle),
                        contentDescription = "Govind Logo",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "GOVIND", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = com.example.govind.theme.GovindTheme.colors.govindGreen)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            com.example.govind.ui.shared.PillExperienceSwitcher()
            Spacer(modifier = Modifier.height(16.dp))'''

new_content = re.sub(pattern, replacement, content, flags=re.DOTALL)
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)
