package biz.thehacker.airq

fun main() {
    val host = System.getenv("AIRQ_HOST")
        ?: throw IllegalStateException("You must provide air-Q's host by environment variable AIRQ_HOST.")
    val password = System.getenv("AIRQ_PASSWORD")
        ?: throw IllegalStateException("You must provide air-Q's password by environment variable AIRQ_PASSWORD.")

    val airQ = AirQ(host, password)

    println("Pinging air-Q...")
    println("Ping ${if (airQ.ping()) "OK :-)" else "failed :-("}.")
    println()

//    println("Getting config from air-Q...")
//    println(AirQConfigFormatter().format(airQ.config))
//    println()

//    println("Available LED themes:")
//    println(airQ.availableLedThemes)

//    println("Getting current LED theme...")
//    println("Left side is set to " + airQ.ledTheme.left + ".")
//    println("Right side is set to " + airQ.ledTheme.right + ".")

//    print("Setting device name...")
//    airQ.deviceName = "demo-air-q"
//    println(" done")
//    println()

//    println("Getting device name from air-Q...")
//    println("air-Q's name is '${airQ.deviceName}'.")
//    println()

//    println("Getting data from air-Q...")
//    println(airQ.data)
//    println()

//    println("Letting air-Q's LEDs blink...")
//    println("blink() returned air-Q's ID as '${airQ.blink()}'.")
}
