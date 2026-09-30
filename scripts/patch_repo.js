const fs = require('fs');
const file = 'android/app/src/main/java/com/example/govind/data/repository/SupabaseGovindRepositoryImpl.kt';
let content = fs.readFileSync(file, 'utf8');
const search = `                    put("discount", kotlinx.serialization.json.JsonPrimitive(
                        pricing.totalDiscount
                            item.product.price - item.product.sellingPrice 
                        else 0.0
                    ))`;
const replace = `                    put("discount", kotlinx.serialization.json.JsonPrimitive(pricing.totalDiscount))`;

if (!content.includes(search)) {
    // try with CRLF
    const searchCRLF = search.replace(/\n/g, '\r\n');
    if (content.includes(searchCRLF)) {
        content = content.replace(searchCRLF, replace.replace(/\n/g, '\r\n'));
    } else {
        console.error('Target not found in file!');
        process.exit(1);
    }
} else {
    content = content.replace(search, replace);
}

fs.writeFileSync(file, content, 'utf8');
console.log('Successfully updated placeGlobalOrder discount line');
