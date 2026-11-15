package com.example.nexaroute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController

// ---------- Theme colors (from the design) ----------
val Navy = Color(0xFF0F2A47)
val Lime = Color(0xFFC6F26B)
val MintBg = Color(0xFFE6F7E1)
val ScreenBg = Color(0xFFF4F6F8)

// ---------- Models & shared state ----------
data class Train(val dep: String, val arr: String, val duration: String, val price: Int)

val trains = listOf(
    Train("06:10", "10:40", "4h 30m", 650),
    Train("07:00", "11:50", "4h 50m", 750),
    Train("08:00", "12:30", "4h 30m", 700),
)

class BookingState {
    var from by mutableStateOf("New Delhi (NDLS)")
    var to by mutableStateOf("Jaipur Jn (JP)")
    var date by mutableStateOf("Fri 20 Nov 2026")
    var train by mutableStateOf(trains[0])
    var classIsCC by mutableStateOf(true)
    var name by mutableStateOf("Ananya Sharma")
    var email by mutableStateOf("ananya.sharma@example.com")
    var mobile by mutableStateOf("+91 90000 00000")
    var seatPref by mutableStateOf("Window")
    val fare get() = if (classIsCC) train.price else 1200
    val className get() = if (classIsCC) "AC Chair Car (CC)" else "Executive Chair Car (EC)"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { NexaApp() } }
    }
}

@Composable
fun NexaApp() {
    val nav = rememberNavController()
    val state = remember { BookingState() }
    NavHost(nav, startDestination = "signin") {
        composable("signin") { SignIn(nav) }
        composable("signup") { SignUp(nav) }
        composable("search") { Search(nav, state) }
        composable("trains") { Trains(nav, state) }
        composable("fare") { Fare(nav, state) }
        composable("passenger") { Passenger(nav, state) }
        composable("review") { Review(nav, state) }
        composable("payment") { Payment(nav, state) }
        composable("ticket") { Ticket(state) }
    }
}

// ---------- Reusable components ----------
@Composable
fun Screen(title: String? = null, step: Int? = null, onBack: (() -> Unit)? = null,
           bottom: @Composable (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(ScreenBg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) Text("←", fontSize = 22.sp, modifier = Modifier.clickable { onBack() }.padding(end = 12.dp))
            Text(title ?: "NexaRoute", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Navy)
        }
        if (step != null) StepBar(step)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), content = content)
        if (bottom != null) Surface(shadowElevation = 8.dp, color = Color.White) {
            Column(Modifier.padding(16.dp).navigationBarsPadding()) { bottom() }
        }
    }
}

@Composable
fun StepBar(active: Int) {
    val labels = listOf("Train", "Fare", "You", "Pay")
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        labels.forEachIndexed { i, l ->
            Column(Modifier.weight(1f)) {
                Box(Modifier.fillMaxWidth().height(3.dp).background(if (i <= active) Navy else Color.LightGray))
                Text(l, fontSize = 10.sp, color = if (i <= active) Navy else Color.Gray)
            }
        }
    }
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Color.White)
    ) { Text("$text  →", fontWeight = FontWeight.SemiBold) }
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, password: Boolean = false, hint: String = "") {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, fontSize = 12.sp, color = Navy, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value, onValueChange = onChange, singleLine = true,
            placeholder = { Text(hint) },
            visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
        )
    }
}

@Composable
fun Card(selected: Boolean = false, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 6.dp)
            .clip10().background(if (selected) MintBg else Color.White)
            .border(BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Color(0xFF3FA34D) else Color(0xFFE0E4E8)), RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        content = content
    )
}
private fun Modifier.clip10() = this.then(Modifier.background(Color.Transparent, RoundedCornerShape(10.dp)))

@Composable
fun RouteRow(s: BookingState, t: Train = s.train) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(t.dep, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text("${t.duration} · Direct", fontSize = 12.sp, color = Color.Gray)
        Text(t.arr, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Navy)
    }
}

// ---------- Screens ----------
@Composable
fun SignIn(nav: NavController) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    Screen {
        Text("WELCOME BACK", fontSize = 11.sp, color = Color.Gray)
        Text("Your next Indian Railways journey starts here.", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy)
        Spacer(Modifier.height(8.dp))
        Text("Sign in for faster bookings, saved journeys and all your tickets in one place.", color = Color.DarkGray)
        Spacer(Modifier.height(16.dp))
        Field("Email address", email, { email = it }, hint = "ananya.sharma@example.com")
        Field("Password", pass, { pass = it }, password = true)
        Spacer(Modifier.height(12.dp))
        PrimaryButton("Sign in", enabled = email.contains("@") && pass.length >= 8) {
            nav.navigate("search") { popUpTo("signin") { inclusive = true } }
        }
        Spacer(Modifier.height(12.dp))
        Text("New to NexaRoute? Create an account", color = Navy, modifier = Modifier.clickable { nav.navigate("signup") })
        Spacer(Modifier.height(8.dp))
        Text("Continue without an account →", color = Navy, modifier = Modifier.clickable { nav.navigate("search") })
    }
}

@Composable
fun SignUp(nav: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val passOk = pass.length >= 8 && pass.any { it.isDigit() } && pass.any { it.isUpperCase() }
    Screen(onBack = { nav.popBackStack() }) {
        Text("All aboard, India.", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy)
        Spacer(Modifier.height(12.dp))
        Field("Full name", name, { name = it }, hint = "Your first and last name")
        Field("Email address", email, { email = it }, hint = "you@example.com")
        Field("Password", pass, { pass = it }, password = true, hint = "Create a password")
        Text("Use at least 8 characters, including a number and a capital letter.", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Create my account", enabled = name.isNotBlank() && email.contains("@") && passOk) {
            nav.navigate("search") { popUpTo("signin") { inclusive = true } }
        }
    }
}

@Composable
fun Search(nav: NavController, s: BookingState) {
    Screen(bottom = { PrimaryButton("Find my train") { nav.navigate("trains") } }) {
        Text("Where in India next?", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy)
        Spacer(Modifier.height(12.dp))
        Field("From", s.from, { s.from = it })
        Field("To", s.to, { s.to = it })
        Field("Departure", s.date, { s.date = it })
        Text("Passengers: 1 adult", color = Navy, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun Trains(nav: NavController, s: BookingState) {
    Screen("Find your train", 0, { nav.popBackStack() },
        bottom = { PrimaryButton("View fares · ₹${s.train.price}") { nav.navigate("fare") } }) {
        Text("${s.from} to ${s.to} on ${s.date}", color = Color.DarkGray)
        Spacer(Modifier.height(8.dp))
        trains.forEach { t ->
            Card(selected = s.train == t, onClick = { s.train = t }) {
                RouteRow(s, t)
                Text("₹${t.price}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Navy, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

@Composable
fun Fare(nav: NavController, s: BookingState) {
    Screen("Journey & fare", 1, { nav.popBackStack() },
        bottom = { Text("Total to pay ₹${s.fare}", fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); PrimaryButton("Continue with this fare") { nav.navigate("passenger") } }) {
        Text("Your way to Jaipur.", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Navy)
        Card { RouteRow(s); Text("Via Gurugram, Rewari & Alwar", fontSize = 12.sp, color = Color.Gray) }
        Text("Choose your class", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
        Card(selected = s.classIsCC, onClick = { s.classIsCC = true }) {
            Text("AC Chair Car (CC)  ₹${s.train.price}", fontWeight = FontWeight.Bold)
            Text("General quota. Cancellation as per Indian Railways policy.", fontSize = 12.sp)
        }
        Card(selected = !s.classIsCC, onClick = { s.classIsCC = false }) {
            Text("Executive Chair Car (EC)  ₹1,200", fontWeight = FontWeight.Bold)
            Text("General quota. Cancellation as per Indian Railways policy.", fontSize = 12.sp)
        }
    }
}

@Composable
fun Passenger(nav: NavController, s: BookingState) {
    Screen("Passenger details", 2, { nav.popBackStack() },
        bottom = { PrimaryButton("Review & pay · ₹${s.fare}", enabled = s.name.isNotBlank() && s.email.contains("@")) { nav.navigate("review") } }) {
        Text("Who's coming along?", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Navy)
        Field("Full name", s.name, { s.name = it })
        Field("Email address", s.email, { s.email = it })
        Field("Mobile number", s.mobile, { s.mobile = it })
        Text("Your seat, your way (optional)", fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Window", "Aisle", "No preference").forEach { p ->
                FilterChip(selected = s.seatPref == p, onClick = { s.seatPref = p }, label = { Text(p) })
            }
        }
    }
}

@Composable
fun Review(nav: NavController, s: BookingState) {
    Screen("Review & pay", 3, { nav.popBackStack() },
        bottom = { PrimaryButton("Pay ₹${s.fare} & book") { nav.navigate("payment") } }) {
        Text("One last stop.", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Navy)
        Card { RouteRow(s); Text(s.className + " · General quota", fontSize = 12.sp) }
        Card { Text(s.name, fontWeight = FontWeight.Bold); Text("1 adult · ${s.seatPref} seat requested", fontSize = 12.sp) }
        Card {
            Text("Adult · ${s.className} × 1    ₹${s.fare}")
            Text("Booking fee    ₹0")
        }
    }
}

@Composable
fun Payment(nav: NavController, s: BookingState) {
    var method by remember { mutableStateOf("Card") }
    var card by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf(s.name) }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    Screen("Payment", 3, { nav.popBackStack() },
        bottom = { PrimaryButton("Pay ₹${s.fare} & book") { nav.navigate("ticket") { popUpTo("search") } } }) {
        Text("Secure payment.", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Navy)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("Card", "UPI", "Net banking").forEach { m ->
                FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
            }
        }
        if (method == "Card") {
            Field("Card number", card, { card = it })
            Field("Name on card", cardName, { cardName = it })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) { Field("Expiry date", expiry, { expiry = it }, hint = "MM / YY") }
                Box(Modifier.weight(1f)) { Field("Security code", cvv, { cvv = it }, password = true) }
            }
        } else Text("You'll be redirected to complete payment via $method.", color = Color.DarkGray)
    }
}

@Composable
fun Ticket(s: BookingState) {
    Column(Modifier.fillMaxSize().background(Navy).statusBarsPadding().padding(20.dp)) {
        Text("NexaRoute", color = Color.White)
        Spacer(Modifier.height(12.dp))
        Text("Jaipur, here you come.", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("You're booked. ₹${s.fare} paid.", color = Lime)
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(16.dp)) {
            Text("YOUR DIGITAL TICKET", fontSize = 11.sp, color = Color.Gray)
            Text(s.date, fontSize = 12.sp)
            RouteRow(s)
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("PASSENGER", fontSize = 10.sp); Text(s.name, fontWeight = FontWeight.Bold) }
                Column { Text("COACH", fontSize = 10.sp); Text("C1", fontWeight = FontWeight.Bold) }
                Column { Text("SEAT", fontSize = 10.sp); Text("42", fontWeight = FontWeight.Bold) }
            }
            Text("${s.className} · General quota", fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            // TODO: replace with a real QR (ZXing: com.journeyapps:zxing-android-embedded)
            Box(Modifier.padding(vertical = 16.dp).size(140.dp).background(Color(0xFFEEEEEE)).align(Alignment.CenterHorizontally))
            Text("PNR 1234567890", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Navy)) { Text("Save ticket offline") }
    }
}