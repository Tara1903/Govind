package com.example.govind.ui.features.address

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.govind.data.model.Address
import com.example.govind.theme.GovindTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedAddressesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddAddress: () -> Unit,
    viewModel: SavedAddressesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editingAddress by remember { mutableStateOf<Address?>(null) }
    var addressToDelete by remember { mutableStateOf<Address?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Saved Addresses",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GovindTheme.colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GovindTheme.colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GovindTheme.colors.surfaceContainerLowest
                )
            )
        },
        bottomBar = {
            Surface(
                color = GovindTheme.colors.surfaceContainerLowest,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onNavigateToAddAddress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GovindTheme.colors.govindGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Address", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        containerColor = GovindTheme.colors.surfaceContainerLow
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GovindTheme.colors.govindGreen
                    )
                }
                uiState.addresses.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GovindTheme.colors.surfaceContainerHighest,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = GovindTheme.colors.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No addresses saved",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GovindTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Save your home, office or kitchen location for quick checkout and live delivery updates.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GovindTheme.colors.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.addresses, key = { it.id }) { address ->
                            AddressCard(
                                address = address,
                                onSetDefault = { viewModel.setDefault(address.id) },
                                onEdit = { editingAddress = address },
                                onDelete = { addressToDelete = address }
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Address Dialog
    editingAddress?.let { addr ->
        var name by remember { mutableStateOf(addr.name) }
        var phone by remember { mutableStateOf(addr.phone) }
        var house by remember { mutableStateOf(addr.house) }
        var street by remember { mutableStateOf(addr.street) }
        var area by remember { mutableStateOf(addr.area) }
        var landmark by remember { mutableStateOf(addr.landmark ?: "") }
        var city by remember { mutableStateOf(addr.city) }
        var pincode by remember { mutableStateOf(addr.pincode) }
        var isDefault by remember { mutableStateOf(addr.isDefault) }

        AlertDialog(
            onDismissRequest = { editingAddress = null },
            title = { Text("Edit Address", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Recipient Name") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true)
                    OutlinedTextField(value = house, onValueChange = { house = it }, label = { Text("House / Flat") }, singleLine = true)
                    OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street") }, singleLine = true)
                    OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Area / Sector") }, singleLine = true)
                    OutlinedTextField(value = landmark, onValueChange = { landmark = it }, label = { Text("Landmark (Optional)") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = pincode, onValueChange = { pincode = it }, label = { Text("Pincode") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAddress(
                            id = addr.id,
                            name = name,
                            phone = phone,
                            house = house,
                            street = street,
                            area = area,
                            landmark = landmark.ifBlank { null },
                            city = city,
                            pincode = pincode,
                            isDefault = isDefault
                        )
                        editingAddress = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.govindGreen)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingAddress = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    addressToDelete?.let { addr ->
        AlertDialog(
            onDismissRequest = { addressToDelete = null },
            title = { Text("Delete Address?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove '${addr.house}, ${addr.street}' from your saved addresses?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAddress(addr.id)
                        addressToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { addressToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddressCard(
    address: Address,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GovindTheme.colors.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (address.isDefault) {
                    Modifier.border(1.5.dp, GovindTheme.colors.govindGreen, RoundedCornerShape(16.dp))
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = GovindTheme.colors.govindGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = address.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GovindTheme.colors.onSurface
                    )
                }

                if (address.isDefault) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GovindTheme.colors.govindGreen.copy(alpha = 0.12f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GovindTheme.colors.govindGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "DEFAULT",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = GovindTheme.colors.govindGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${address.house}, ${address.street}",
                style = MaterialTheme.typography.bodyMedium,
                color = GovindTheme.colors.onSurface
            )
            Text(
                text = "${address.area}, ${address.city} - ${address.pincode}",
                style = MaterialTheme.typography.bodyMedium,
                color = GovindTheme.colors.onSurfaceVariant
            )
            if (!address.landmark.isNullOrBlank()) {
                Text(
                    text = "Landmark: ${address.landmark}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GovindTheme.colors.onSurfaceVariant
                )
            }
            Text(
                text = "Phone: ${address.phone}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = GovindTheme.colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = GovindTheme.colors.surfaceContainerHighest)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!address.isDefault) {
                    TextButton(onClick = onSetDefault) {
                        Text(
                            text = "Set as Default",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GovindTheme.colors.govindGreen
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = GovindTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
