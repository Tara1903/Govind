package com.example.govind.ui.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.govind.theme.GovindTheme

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: (userRole: String?) -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var otpInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onAuthSuccess(uiState.userRole)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GovindTheme.colors.warmWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = com.example.govind.R.drawable.logo),
            contentDescription = "Govind Logo",
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(24.dp))
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = if (uiState.otpSent) "Verify Your Email" else "Welcome to Govind",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = GovindTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (uiState.otpSent) "Enter the 6-digit code sent to ${uiState.email}" else "Login or Sign up to continue",
            style = MaterialTheme.typography.bodyLarge,
            color = GovindTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (!uiState.otpSent) {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("Email Address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GovindTheme.colors.govindGreen,
                    focusedLabelColor = GovindTheme.colors.govindGreen
                )
            )
        } else {
            OutlinedTextField(
                value = otpInput,
                onValueChange = { if (it.length <= 8) otpInput = it },
                label = { Text("Enter OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GovindTheme.colors.govindGreen,
                    focusedLabelColor = GovindTheme.colors.govindGreen
                )
            )
        }

        uiState.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        if (uiState.isLoading) {
            CircularProgressIndicator(color = GovindTheme.colors.govindGreen)
        } else {
            Button(
                onClick = {
                    if (uiState.otpSent) viewModel.verifyOtp(otpInput)
                    else viewModel.sendOtp()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.govindGreen)
            ) {
                Text(
                    text = if (uiState.otpSent) "Verify OTP" else "Continue with Email",
                    style = MaterialTheme.typography.titleMedium,
                    color = GovindTheme.colors.textOnDark
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.otpSent && !uiState.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { viewModel.changeEmail() }) {
                    Text("Change Email", color = GovindTheme.colors.govindGreen)
                }
                TextButton(onClick = { viewModel.sendOtp() }) {
                    Text("Resend OTP", color = GovindTheme.colors.govindGreen)
                }
            }
        }

        if (!uiState.otpSent) {
            TextButton(
                onClick = {
                    viewModel.continueAsGuest()
                    onContinueAsGuest()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue as Guest", color = GovindTheme.colors.textSecondary)
            }
        }
    }
}
