const fs = require('fs');
const path = require('path');

const cartScreenPath = path.resolve('android/app/src/main/java/com/example/govind/ui/features/cart/CartScreen.kt');
let content = fs.readFileSync(cartScreenPath, 'utf8');

// Replace bottom bar calculation
const oldBottomCalcRegex = /val subtotal = uiState\.cart!!\.items\.sumOf[\s\S]*?val toPay = sellingTotal \+ delivery/;
const newBottomCalc = `val subtotal = uiState.totalAmount
                        val discount = uiState.totalSavings
                        val delivery = if (subtotal >= 500.0) 0.0 else 40.0
                        val toPay = subtotal + delivery`;
content = content.replace(oldBottomCalcRegex, newBottomCalc);

// Replace free delivery threshold banner
const oldDeliveryBannerRegex = /val items = uiState\.cart!!\.items\s*val sellingTotal = items\.sumOf[\s\S]*?if \(sellingTotal <= 150\) \{[\s\S]*?val needed = 150\.0 - sellingTotal[\s\S]*?Text\(\s*text = "Yay! You got FREE Delivery"[\s\S]*?\}\s*\}/;
const newDeliveryBanner = `val subtotal = uiState.totalAmount
                    if (subtotal < 500.0) {
                        val needed = 500.0 - subtotal
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = GovindTheme.colors.softOrange,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Add ₹\${needed.toInt()} more for FREE Delivery",
                                color = GovindTheme.colors.govindOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = GovindTheme.colors.softFresh,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Yay! You got FREE Delivery",
                                color = GovindTheme.colors.freshGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }`;
content = content.replace(oldDeliveryBannerRegex, newDeliveryBanner);

// Replace Order summary calculation
const oldSummaryCalcRegex = /val items = uiState\.cart!!\.items\s*val subtotal = items\.sumOf[\s\S]*?val grandTotal = sellingTotal \+ delivery/;
const newSummaryCalc = `val subtotal = uiState.totalAmount
                    val discount = uiState.totalSavings
                    val delivery = if (subtotal >= 500.0) 0.0 else 40.0
                    val grandTotal = subtotal + delivery`;
content = content.replace(oldSummaryCalcRegex, newSummaryCalc);

fs.writeFileSync(cartScreenPath, content, 'utf8');
console.log('CartScreen.kt pricing engine integration updated.');
