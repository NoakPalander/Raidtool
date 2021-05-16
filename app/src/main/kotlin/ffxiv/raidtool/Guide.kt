package ffxiv.raidtool

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color
import java.io.File

data class Guide(@JsonProperty("title") val title: String,
                 @JsonProperty("url") val urls: List<String>)

data class Guides(@JsonProperty("videos") val guides: List<Guide>)

fun getGuide(resourcePath: String, event: GuildMessageReceivedEvent, image: String) {
    val json = ObjectMapper().readValue<Guides>(File("${resourcePath}guide.json"))
    val builder = EmbedBuilder()
        .setThumbnail(image)
        .setAuthor("Snowman raid guide")
        .setColor(Color.RED)
        .setTitle("Eden Savage Guides")
        .addField("NOTE!", "Don't forget that we don't always play according to the guides, we only use them as a guideline!", false)

    json.guides.forEach { guide ->
        if (guide.urls.size == 1) {
            builder.addField(guide.title, guide.urls[0], false)
        }
        else {
            guide.urls.forEachIndexed { index, url ->
              builder.addField("${guide.title} (${index + 1}/${guide.urls.size})", url, true)
            }
        }
    }

    builder.setFooter("Before raid, don't forget: repaired gear, raid food, pots och most importantly.. bring focus")
    event.channel.sendMessage(builder.build()).queue()
}