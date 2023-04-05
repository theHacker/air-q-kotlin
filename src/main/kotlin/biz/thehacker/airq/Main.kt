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

    println("Getting data from air-Q...")
    println(airQ.data)
    println()

    println("Letting air-Q's LEDs blink...")
    println("blink() returned air-Q's ID as '${airQ.blink()}'.")
}
