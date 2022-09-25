package net.thatapex.shellbot.discord

import dev.kord.common.entity.Snowflake

data class BotConfiguration(
    val token: String,
    val channel: Snowflake,
    val managers: List<Snowflake>
)