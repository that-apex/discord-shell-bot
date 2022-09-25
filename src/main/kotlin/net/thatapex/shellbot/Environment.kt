package net.thatapex.shellbot

object Environment {

    val TOKEN: String
        get() = System.getenv("SHELLBOT_TOKEN")

    val CHANNEL_ID: String
        get() = System.getenv("SHELLBOT_CHANNEL_ID")

    val BOT_MANAGERS: List<String>
        get() = System.getenv("SHELLBOT_MANAGERS").split(",")

}
