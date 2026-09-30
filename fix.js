const fs = require('fs');
const path = 'android/app/src/main/java/com/example/govind/data/repository/SupabaseGovindRepositoryImpl.kt';
let content = fs.readFileSync(path, 'utf8');

content = content.replace(
    /override suspend fun verifyOtp\(email: String, token: String\): Result<Unit> \{[\s\S]*?override suspend fun sendOtp\(email: String\): Result<Unit> \{[\s\S]*?Result\.failure\(e\)\n\s*\}/,
    override suspend fun verifyOtp(email: String, token: String): Result<Unit> {
        return try {
            val response = api.verifyOtp(com.example.govind.data.model.VerifyOtpRequest(type = "email", email = email, token = token))
            sessionManager.accessToken = response.accessToken
            sessionManager.refreshToken = response.refreshToken
            sessionManager.userId = response.user.id
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendOtp(email: String): Result<Unit> {
        return try {
            api.sendOtp(com.example.govind.data.model.SendOtpRequest(email = email, createUser = true))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
);
fs.writeFileSync(path, content);
