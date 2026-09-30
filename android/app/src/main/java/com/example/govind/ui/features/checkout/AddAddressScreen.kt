package com.example.govind.ui.features.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.govind.theme.GovindTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddAddressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Address", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.error?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error)
            }
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateField("name", it) },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = { viewModel.updateField("phone", it) },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.house,
                onValueChange = { viewModel.updateField("house", it) },
                label = { Text("House / Flat No.") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.street,
                onValueChange = { viewModel.updateField("street", it) },
                label = { Text("Street / Road Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.area,
                onValueChange = { viewModel.updateField("area", it) },
                label = { Text("Area / Locality") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.city,
                    onValueChange = { viewModel.updateField("city", it) },
                    label = { Text("City") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.pincode,
                    onValueChange = { viewModel.updateField("pincode", it) },
                    label = { Text("Pincode") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.saveAddress() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.govindGreen)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = GovindTheme.colors.pureWhite, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Address", color = GovindTheme.colors.pureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
