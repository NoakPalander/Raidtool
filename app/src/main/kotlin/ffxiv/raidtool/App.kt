package ffxiv.raidtool

import Booked
import RaidPoint
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ffxiv.raidtool.resource.BestInSlot
import ffxiv.raidtool.resource.Config
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import java.awt.Color
import java.io.File
import java.io.FileNotFoundException

fun main(args: Array<String>) {
    val resourcePath = if(args.isEmpty()) "" else args.first() + '/'

    val botConfig: Config = ObjectMapper().readValue(File("${resourcePath}discord_config.json"))
    val parser = EventParser(botConfig.prefix)

    parser.hooks = hashMapOf(
        "t0nk" to { _, e -> e.sendMessage("Menade du blå dps?").queue() },
        "tonk" to { _, e -> e.sendMessage("Menade du dps?").queue() },
        "paladin" to { _, e -> e.sendMessage("Vi svär inte framför Snowman!").queue {
           it.addReaction("😠").queue()
        }},
        "snowman" to { _, e -> e.sendMessage(botConfig.snowman).queue() },
        "snowman2" to { _, e -> e.sendMessage(botConfig.snowman2).queue() },
        "schedule" to { commandArgs, e ->
            if (commandArgs.isNotEmpty()) {
                when (commandArgs.first()) {
                    // Creates a new schedule
                    "new" -> {
                        arrayOf("Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag", "Söndag").forEach {
                            e.sendMessage(it).queue { message ->
                                message.addReaction("✔").queue()
                                message.addReaction("✖").queue()
                            }

                            Thread.sleep(200)
                        }

                        e.sendMessage("Glöm inte fylla i schemat senare! @everyone").queue()
                    }
                    // Books the new schedule
                    "set" -> {
                        // Manually books the raid
                        val data = commandArgs.drop(1).chunked(2).map { (days, time) -> RaidPoint(days.capitalize(), time) }
                        File("${resourcePath}data.json").delete()
                        ObjectMapper().writeValue(File("${resourcePath}data.json"), Booked(data))

                        val builder = EmbedBuilder()
                        builder.setTitle("Raid")
                        builder.setColor(Color.GREEN)
                        builder.setFooter("- Snowman's Angels")
                        data.forEach { builder.addField(it.day, it.time, false) }

                        e.sendMessage(builder.build()).queue()
                        Thread.sleep(100)
                        e.sendMessage("@everyone").queue()
                    }
                    // Gets the current schedule
                    "when" -> {
                        try {
                            val data: Booked = ObjectMapper().readValue(File("${resourcePath}data.json"))

                            val builder = EmbedBuilder()
                            builder.setTitle("Raid")
                            builder.setColor(Color.GREEN)
                            builder.setFooter("- Snowman's Angels")
                            data.booked.forEach { builder.addField(it.day, it.time, false) }
                            e.sendMessage(builder.build()).queue()
                        }
                        catch(_: FileNotFoundException) {
                            e.sendMessage("Hittade inga bookade raid-sessioner").queue {
                                it.addReaction("\uD83D\uDE22").queue()
                            }
                        }
                    }
                    // Removes the current schedule
                    "clean" -> {
                        File("${resourcePath}data.json").delete()
                    }
                }
            }
        },
        "bis" to { commandArgs, e ->
            if (commandArgs.isNotEmpty()) {
                // Loads the BIS-sets
                val sets = ObjectMapper().readValue<Map<String, List<BestInSlot>>>(File("${resourcePath}bis.json"))
                if (sets.containsKey(commandArgs.first())) {
                    val builder = EmbedBuilder()
                    builder.setTitle(commandArgs.first().toUpperCase())
                    sets[commandArgs.first()]!!.forEach { bis ->
                        builder.setColor(Color(bis.color[0], bis.color[1], bis.color[2]))
                        builder.addField(bis.title, bis.url, false)
                    }

                    e.sendMessage(builder.build()).queue()
                }
                else {
                    e.sendMessage("BIS är inte tillgängligt för Snowman").queue {
                        it.addReaction("\uD83D\uDE20")
                    }
                }
            }
        },
        "help" to { _, e ->
            e.sendMessage(EmbedBuilder()
                .setTitle("Raidtool")
                .setColor(Color.ORANGE)
                .setThumbnail(botConfig.snowman)
                .setDescription("Olika kommandon för raidtool botten")
                .addField("${botConfig.prefix}help", "Visar hjälpsidan", false)
                .addField("${botConfig.prefix}schedule new", "Tar bort det gamla och startar ett nytt schema", false)
                .addField("${botConfig.prefix}schedule set", "Sätter en nytt schema, kräver datum dag följt av tid", false)
                .addField("${botConfig.prefix}schedule when", "Visar det nuvarande schemat", false)
                .addField("${botConfig.prefix}schedule clean", "Tar bort det gamla schemat", false)
                .addField("${botConfig.prefix}bis", "Visar ditt BIS-set, glöm inte klassnamnet [e.x: war]", false)
                .build()
            ).queue()
        }
    )

    JDABuilder.createDefault(botConfig.token)
        .addEventListeners(parser)
        .setActivity(Activity.watching("over Snowman"))
        .setStatus(OnlineStatus.ONLINE)
        .build()
}