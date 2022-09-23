package net.thatapex.shellbot

object Environment {

    val TOKEN: String
        get() = System.getenv("SHELLBOT_TOKEN")

}
