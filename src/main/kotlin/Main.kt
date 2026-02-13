import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Serializable
data class Institution(
    val id: Int,
    val name: String,
    val country: String,
    val city: String,
    val phone: String,
    val website: String,
    val description: String,
    val successfulCases: Int,
    val isSponsored: Boolean,
    val adCost: Int
)

private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

private fun dataDir(): Path {
    val home = System.getProperty("user.home") ?: "."
    val dir = Paths.get(home, ".student-dispatch")
    Files.createDirectories(dir)
    return dir
}

private fun dataFile(): Path = dataDir().resolve("institutions.json")

private fun defaultSeed(): List<Institution> = listOf(
    Institution(1, "موسسه نمونه ۱", "ایران", "تهران", "021000000", "example.com", "توضیحات نمونه", 12, true, 150),
    Institution(2, "موسسه نمونه ۲", "ترکیه", "استانبول", "+90 000", "example.org", "توضیحات نمونه", 8, false, 0),
    Institution(3, "موسسه نمونه ۳", "آلمان", "برلین", "+49 000", "example.net", "توضیحات نمونه", 20, true, 300)
)

private fun loadInstitutions(): MutableList<Institution> {
    return try {
        val p = dataFile()
        if (!Files.exists(p)) {
            val seed = defaultSeed()
            Files.writeString(p, json.encodeToString(seed))
            seed.toMutableList()
        } else {
            json.decodeFromString<List<Institution>>(Files.readString(p)).toMutableList()
        }
    } catch (_: Throwable) {
        defaultSeed().toMutableList()
    }
}

private fun saveInstitutions(items: List<Institution>) {
    try {
        Files.writeString(dataFile(), json.encodeToString(items))
    } catch (_: Throwable) {
        // ignore
    }
}

fun main() = application {
    AppWindow()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationScope.AppWindow() {
    var institutions by remember { mutableStateOf(loadInstitutions()) }
    var query by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    val countries = remember(institutions) { institutions.map { it.country }.distinct().sorted() }
    val cities = remember(institutions, country) {
        institutions
            .filter { country.isBlank() || it.country == country }
            .map { it.city }
            .distinct()
            .sorted()
    }

    val filtered = remember(institutions, query, country, city) {
        institutions.filter { inst ->
            val q = query.trim()
            val matchQuery = q.isBlank() || inst.name.contains(q, true) || inst.description.contains(q, true)
            val matchCountry = country.isBlank() || inst.country == country
            val matchCity = city.isBlank() || inst.city == city
            matchQuery && matchCountry && matchCity
        }
    }

    var detailId by remember { mutableStateOf<Int?>(null) }
    var adminOpen by remember { mutableStateOf(false) }

    Window(onCloseRequest = ::exitApplication, title = "اعزام دانشجو - Desktop") {
        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("اعزام دانشجو") },
                        actions = {
                            TextButton(onClick = { adminOpen = true }) { Text("ادمین") }
                        }
                    )
                }
            ) { pad ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(pad).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("جستجو (نام/توضیحات)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DropdownField(
                            label = "کشور",
                            value = country,
                            items = listOf("") + countries,
                            onSelect = {
                                country = it
                                // reset city if country changes
                                if (country.isBlank()) city = "" else if (city.isNotBlank() && city !in cities) city = ""
                            },
                            modifier = Modifier.weight(1f)
                        )
                        DropdownField(
                            label = "شهر",
                            value = city,
                            items = listOf("") + cities,
                            onSelect = { city = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("نتایج: ${filtered.size}", style = MaterialTheme.typography.labelLarge)

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                        items(filtered) { inst ->
                            InstitutionCard(inst) { detailId = inst.id }
                        }
                    }
                }
            }

            val inst = detailId?.let { id -> institutions.firstOrNull { it.id == id } }
            if (inst != null) {
                DetailDialog(inst = inst, onClose = { detailId = null })
            }

            if (adminOpen) {
                AdminDialog(
                    institutions = institutions,
                    onClose = { adminOpen = false },
                    onApply = { newList ->
                        institutions = newList.toMutableList()
                        saveInstitutions(institutions)
                    }
                )
            }
        }
    }
}

@Composable
private fun InstitutionCard(inst: Institution, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(inst.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (inst.isSponsored) {
                    AssistChip(onClick = {}, label = { Text("تبلیغ") })
                }
            }
            Text("${inst.country} - ${inst.city}")
            Text("کارهای موفق: ${inst.successfulCases}")
            if (inst.isSponsored) Text("هزینه تبلیغ: ${inst.adCost}")
        }
    }
}

@Composable
private fun DetailDialog(inst: Institution, onClose: () -> Unit) {
    Dialog(onCloseRequest = onClose, title = "جزئیات موسسه") {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(inst.name, style = MaterialTheme.typography.headlineSmall)
                if (inst.isSponsored) {
                    AssistChip(onClick = {}, label = { Text("تبلیغ | هزینه: ${inst.adCost}") })
                }
                Text("کشور: ${inst.country}")
                Text("شهر: ${inst.city}")
                Text("تلفن: ${inst.phone}")
                Text("وبسایت: ${inst.website}")
                Text("کارهای موفق: ${inst.successfulCases}")
                Divider()
                Text(inst.description)
                Divider()
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onClose) { Text("بستن") }
                }
            }
        }
    }
}

@Composable
private fun DropdownField(
    label: String,
    value: String,
    items: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = if (value.isBlank()) "همه" else value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(if (item.isBlank()) "همه" else item) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminDialog(
    institutions: List<Institution>,
    onClose: () -> Unit,
    onApply: (List<Institution>) -> Unit
) {
    var pass by remember { mutableStateOf("") }
    var isAuthed by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    // simple password; you can change
    val adminPassword = "1234"

    Dialog(onCloseRequest = onClose, title = "پنل ادمین") {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!isAuthed) {
                    Text("ورود ادمین", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it; error = "" },
                        label = { Text("رمز ادمین") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = {
                            if (pass == adminPassword) {
                                isAuthed = true
                            } else {
                                error = "رمز اشتباه است."
                            }
                        }) { Text("ورود") }
                        OutlinedButton(onClick = onClose) { Text("بستن") }
                    }
                } else {
                    Text("مدیریت داده", style = MaterialTheme.typography.titleMedium)
                    Text("تعداد رکورد: ${institutions.size}")

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = {
                            val seed = defaultSeed()
                            onApply(seed)
                        }) { Text("ریست + نمونه") }
                        OutlinedButton(onClick = {
                            // add sample new record
                            val nextId = (institutions.maxOfOrNull { it.id } ?: 0) + 1
                            val newInst = Institution(
                                id = nextId,
                                name = "موسسه جدید $nextId",
                                country = "ایران",
                                city = "تهران",
                                phone = "",
                                website = "",
                                description = "",
                                successfulCases = 0,
                                isSponsored = false,
                                adCost = 0
                            )
                            onApply(institutions + newInst)
                        }) { Text("افزودن سریع") }
                        OutlinedButton(onClick = onClose) { Text("بستن") }
                    }

                    Divider()

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 360.dp)) {
                        items(institutions) { inst ->
                            Card(Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(inst.name, style = MaterialTheme.typography.titleSmall)
                                        Text("${inst.country} - ${inst.city} | موفق: ${inst.successfulCases}")
                                    }
                                    OutlinedButton(onClick = {
                                        onApply(institutions.filterNot { it.id == inst.id })
                                    }) { Text("حذف") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
