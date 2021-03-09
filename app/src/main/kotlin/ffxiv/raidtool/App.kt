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
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import java.awt.Color
import java.io.File
import java.io.FileNotFoundException

fun GuildMessageReceivedEvent.sendMessage(message: String) {
    this.channel.sendMessage(message).queue()
}

fun main(args: Array<String>) {
    val resourcePath = if(args.isEmpty()) "" else args.first() + '/'

    val botConfig: Config = ObjectMapper().readValue(File("${resourcePath}discord_config.json"))
    val parser = EventParser(botConfig.prefix)

    parser.hooks = hashMapOf(
        "shirk" to { _, e -> e.sendMessage("Jag provokar och sen lägger jag shirk... jag har ingen shirk!") },
        "divination" to { _, e -> e.sendMessage("Divination blir sen..") },
        "source" to { _, e, -> e.sendMessage("https://github.com/NoakPalander/Raidtool/blob/master/README.md") },
        "t0nk" to { _, e -> e.sendMessage("Menade du blå dps?") },
        "tonk" to { _, e -> e.sendMessage("Menade du dps?") },
        "paladin" to { _, e -> e.channel.sendMessage("Vi svär inte framför Snowman!").queue {
           it.addReaction("😠").queue()
        }},
        "snowman" to { _, e -> e.sendMessage(botConfig.snowman) },
        "snowman2" to { _, e -> e.sendMessage(botConfig.snowman2) },
        "war-chad" to { _, e -> e.sendMessage(botConfig.warChad) },
        "schedule" to { commandArgs, e ->
            if (commandArgs.isNotEmpty()) {
                when (commandArgs.first()) {
                    // Creates a new schedule
                    "new" -> {
                        arrayOf("Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag", "Söndag", "Måndag").forEach {
                            e.channel.sendMessage(it).queue { message ->
                                message.addReaction("✔").queue()
                                message.addReaction("✖").queue()
                            }

                            Thread.sleep(200)
                        }

                        e.sendMessage("Glöm inte fylla i schemat senare! @everyone")
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

                        e.channel.sendMessage(builder.build())
                        Thread.sleep(100)
                        e.sendMessage("@everyone")
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
                            e.channel.sendMessage(builder.build()).queue()
                        }
                        catch(_: FileNotFoundException) {
                            e.channel.sendMessage("Hittade inga bookade raid-sessioner").queue {
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
                if (sets.containsKey(commandArgs.first().toLowerCase())) {
                    val builder = EmbedBuilder()
                    builder.setTitle(commandArgs.first().toUpperCase())
                    sets[commandArgs.first().toLowerCase()]!!.forEach { bis ->
                        builder.setColor(Color(bis.color[0], bis.color[1], bis.color[2]))
                        builder.addField(bis.title, bis.url, false)
                    }

                    e.channel.sendMessage(builder.build()).queue()
                }
                else {
                    e.channel.sendMessage("BIS är inte tillgängligt för Snowman").queue {
                        it.addReaction("\uD83D\uDE20")
                    }
                }
            }
        },
        "help" to { _, e ->
            e.channel.sendMessage(EmbedBuilder()
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
                .addField("${botConfig.prefix}source", "Visar 'readme' filen.", false)
                .build()
            ).queue()
        },
        "notify" to { _, e ->
            val role = e.guild.getRolesByName(botConfig.admin, true).first()
            if (e.member!!.roles.contains(role)) {
                try {
                    // Retrieves the entire member list and iterates over it, excludes bots and the original user
                    e.guild.members.filterNot { it.user.isBot || it.user == e.author }.forEach { member ->
                        member.user.openPrivateChannel().queue { channel ->
                            val data: Booked = ObjectMapper().readValue(File("${resourcePath}data.json"))
                            val builder = EmbedBuilder()
                            builder.setTitle("Glöm inte raid")
                            builder.setColor(Color.GREEN)
                            builder.setFooter("- Snowman's Angels")
                            data.booked.forEach { builder.addField(it.day, it.time, false) }
                            channel.sendMessage(builder.build()).queue { it.addReaction("❤").queue() }
                        }
                    }
                }
                catch(_: FileNotFoundException) {
                    e.channel.sendMessage("Hittade inga bookade raid-sessioner").queue {
                        it.addReaction("\uD83D\uDE22").queue()
                    }
                }
            }
        }
    )

    JDABuilder.createDefault(botConfig.token)
        .enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS)
        .addEventListeners(parser)
        .setActivity(Activity.watching("over Snowman"))
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .setStatus(OnlineStatus.ONLINE)
        .build()
}