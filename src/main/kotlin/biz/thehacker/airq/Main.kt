package biz.thehacker.airq

fun main() {
    val host = System.getenv("AIRQ_HOST")
        ?: throw IllegalStateException("You must provide air-Q's host by environment variable AIRQ_HOST.")
    val password = System.getenv("AIRQ_PASSWORD")
        ?: throw IllegalStateException("You must provide air-Q's password by environment variable AIRQ_PASSWORD.")

    val airQ = AirQ(host, password)

//    println("Configuring static IP configuration for air-Q...")
//    airQ.ifconfig.setStatic("192.168.0.42", "255.255.255.0", "192.168.0.1", "192.168.0.5")

//    println("Configuring NTP server for air-Q...")
//    airQ.ntpServer = "192.168.0.6"
//    println("NTP server is set to '${airQ.ntpServer}'.")

//    println("Configuring DHCP IP configuration for air-Q...")
//    airQ.ifconfig.setDHCP()

//    println("Configuring air-Q's LED brightness...")
//    airQ.setLedBrightness(7.0)
//    airQ.setLedBrightness(6.0, 2.0, LocalTime.of(8, 0)..LocalTime.of(21, 30))

//    println("Restarting air-Q...")
//    airQ.restart()

//    println("Gracefully shutting down air-Q...")
//    airQ.shutdown()

//    println("Disabling cloud remote access O.o...")
//    airQ.cloudRemote = false

//    println("Pinging air-Q...")
//    println("Ping ${if (airQ.ping()) "OK :-)" else "failed :-("}.")
//    println()

//    println("Getting config from air-Q...")
//    println(AirQConfigFormatter().format(airQ.config))
//    println()

//    println("Available LED themes:")
//    println(airQ.availableLedThemes)

//    println("Getting current LED theme...")
//    println("Left side is set to " + airQ.ledTheme.left + ".")
//    println("Right side is set to " + airQ.ledTheme.right + ".")
//
//    println("Changing LED theme...")
//    airQ.ledTheme.left = "VOC"
//    airQ.ledTheme.right = "CO2"

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
