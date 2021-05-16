package ffxiv.raidtool

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color
import java.io.File
import java.io.FileNotFoundException
import com.fasterxml.jackson.annotation.JsonProperty

data class RaidPoint(
    @JsonProperty("day") val day: String,
    @JsonProperty("time") val time: String
)

data class Booked(@JsonProperty("booked") val booked: List<RaidPoint>)

fun newPoll(event: GuildMessageReceivedEvent) {
    arrayOf("Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday").forEach {
        event.channel.sendMessage(it).queue { message ->
            message.addReaction("✔").queue()
            message.addReaction("✖").queue()
        }

        Thread.sleep(200)
    }

    event.channel.sendMessage("Don't forget to fill in the schedule later! @everyone").queue()
}

fun setSchedule(resourcePath: String, args: List<String>, event: GuildMessageReceivedEvent) {
    // Manually books the raid
    val data = args.drop(1).chunked(2).map { (days, time) -> RaidPoint(days.capitalize(), time) }
    File("${resourcePath}data.json").delete()
    ObjectMapper().writeValue(File("${resourcePath}data.json"), Booked(data))

    getSchedule(resourcePath, event)
    Thread.sleep(100)
    event.channel.sendMessage("@everyone").queue()
}

fun getSchedule(resourcePath: String, event: GuildMessageReceivedEvent) {
    try {
        val data: Booked = ObjectMapper().readValue(File("${resourcePath}data.json"))
        val builder = EmbedBuilder()
        builder.setTitle("Raid")
        builder.setColor(Color.GREEN)
        builder.setFooter("- Snowman's Angels")
        data.booked.forEach { builder.addField(it.day, it.time, false) }
        event.channel.sendMessage(builder.build()).queue()
    }
    catch(_: FileNotFoundException) {
        event.channel.sendMessage("Didn't find any booked raid sessions!").queue {
            it.addReaction("\uD83D\uDE22").queue()
        }
    }
}

fun addDate(resourcePath: String, args: List<String>, event: GuildMessageReceivedEvent) {
    try {
        val current: Booked = ObjectMapper().readValue(File("${resourcePath}data.json"))
        val new = current.booked.toMutableList().apply {
            addAll(args.drop(1).chunked(2).map { (days, time) -> RaidPoint(days.capitalize(), time) })
        }

        // Sorts the new schedule in weekday order
        new.sortBy { arrayOf("Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday").indexOf(it.day) }
        File("${resourcePath}data.json").delete()
        ObjectMapper().writeValue(File("${resourcePath}data.json"), Booked(new))

        getSchedule(resourcePath, event)
        event.channel.sendMessage(
            "Note: [${args.drop(1).chunked(2).joinToString(",") { it.first() }}] is now also booked @everyone"
        ).queue()
    }
    catch(_: FileNotFoundException) {
        event.channel.sendMessage("Didn't find any booked sessions, create a new schedule if you wanna add a date!").queue()
    }
}

fun cancelDate(resourcePath: String, args: List<String>, event: GuildMessageReceivedEvent) {
    try {
        val current: Booked = ObjectMapper().readValue(File("${resourcePath}data.json"))
        val indices = current.booked.mapIndexedNotNull { index, point ->
            if (args.drop(1).map { it.capitalize() }.contains(point.day.capitalize())) index else null
        }

        val new = current.booked.mapIndexedNotNull { index, point -> if (indices.contains(index)) null else point }
        File("${resourcePath}data.json").delete()
        ObjectMapper().writeValue(File("${resourcePath}data.json"), Booked(new))
        getSchedule(resourcePath, event)
        event.channel.sendMessage(
            "Note: [${args.drop(1).chunked(2).joinToString(",") { it.first() }}] are now canceled! @everyone"
        ).queue()
    }
    catch(_: FileNotFoundException) {
        event.channel.sendMessage("Can't remove a date that isn't booked!").queue()
    }
}

fun clearSchedule(resourcePath: String) {
    File("${resourcePath}data.json").delete()
}