import sys

file_path = "C:\\Web Apps\\Govind\\android\\app\\src\\main\\java\\com\\example\\govind\\data\\repository\\SupabaseGovindRepositoryImpl.kt"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("override suspend fun login(email: String, password: String): Result<Unit> {", "override suspend fun sendOtp(email: String): Result<Unit> {")
content = content.replace("val request = com.example.govind.data.model.AuthRequest(email, password)", "val request = com.example.govind.data.model.SendOtpRequest(email = email, createUser = true)")
content = content.replace("val response = api.login(request)", "api.sendOtp(request)")
content = content.replace("sessionManager.saveAuth(response.accessToken ?: \"\", response.refreshToken ?: \"\", response.user.id)", "")

content = content.replace("override suspend fun signUp(email: String, password: String): Result<Unit> {", "override suspend fun verifyOtp(email: String, token: String): Result<Unit> {")
content = content.replace("val request = com.example.govind.data.model.AuthRequest(email, password)\\n            val response = api.signUp(request)", "val request = com.example.govind.data.model.VerifyOtpRequest(type = \"email\", email = email, token = token)\\n            val response = api.verifyOtp(request)")
content = content.replace("sessionManager.saveAuth(response.accessToken ?: \"\", response.refreshToken ?: \"\", response.user.id)", "sessionManager.saveAuth(response.accessToken ?: \"\", response.refreshToken ?: \"\", response.user.id)")

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Done")
