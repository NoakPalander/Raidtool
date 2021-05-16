package ffxiv.raidtool

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color
import java.io.File

data class BestInSlot(
    @JsonProperty("title") val title: String,
    @JsonProperty("url") val url: String,
    @JsonProperty("color") val color: List<Int>
)

fun getBestInSlot(resourcePath: String, commandArgs: List<String>, event: GuildMessageReceivedEvent) {
    if (commandArgs.isNotEmpty()) {
        // Loads the BIS-sets
        val sets = ObjectMapper().readValue<Map<String, List<BestInSlot>>>(File("${resourcePath}bis.json"))
        if (sets.containsKey(commandArgs.first().toLowerCase())) {
            val builder = EmbedBuilder()
            builder.setTitle(commandArgs.first().toUpperCase())
            sets[commandArgs.first().toLowerCase()]!!.forEach { bis ->
                builder.setColor(Color(bis.color[0], bis.color[1], bis.color[2]))
                builder.addField(bis.title, bis.url, false)
            }

            event.channel.sendMessage(builder.build()).queue()
        }
        else {
            event.channel.sendMessage("BIS isn't available for Snowman").queue {
                it.addReaction("\uD83D\uDE20")
            }
        }
    }
}