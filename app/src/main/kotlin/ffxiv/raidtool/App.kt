package ffxiv.raidtool

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import java.awt.Color
import java.io.File
import java.io.FileNotFoundException


fun main(args: Array<String>) {
    fun GuildMessageReceivedEvent.sendMessage(message: String) { this.channel.sendMessage(message).queue() }

    val resourcePath = if(args.isEmpty()) "" else args.first() + '/'

    val botConfig: Config = ObjectMapper().readValue(File("${resourcePath}discord_config.json"))
    val parser = EventParser(botConfig.prefix)

    parser.hooks = hashMapOf(
        "shirk" to { _, e -> e.sendMessage("Jag provokar och sen lägger jag shirk... jag har ingen shirk!") },
        "divination" to { _, e -> e.sendMessage("Divination blir sen..") },
        "source" to { _, e -> e.sendMessage("https://github.com/NoakPalander/Raidtool/blob/master/README.md") },
        "t0nk" to { _, e -> e.sendMessage("Menade du blå dps?") },
        "tonk" to { _, e -> e.sendMessage("Menade du dps?") },
        "paladin" to { _, e -> e.channel.sendMessage("Vi svär inte framför Snowman!").queue {
           it.addReaction("😠").queue()
        }},
        "snowman" to { _, e -> e.sendMessage(botConfig.snowman) },
        "snowman2" to { _, e -> e.sendMessage(botConfig.snowman2) },
        "war-chad" to { _, e -> e.sendMessage(botConfig.warChad) },
        "schedule" to { commandArgs, event ->
            if (commandArgs.isNotEmpty()) {
                // Checks the argument
                when (commandArgs.first()) {
                    // Creates a new schedule
                    "new" -> newPoll(event)
                    // Books the new schedule
                    "set" -> setSchedule(resourcePath, commandArgs, event)
                    // Gets the current schedule
                    "when" -> getSchedule(resourcePath, event)
                    // Adds a target date
                    "add" -> addDate(resourcePath, commandArgs, event)
                    // Cancels a target date
                    "cancel" -> cancelDate(resourcePath, commandArgs, event)
                    // Removes the current schedule
                    "clean" -> clearSchedule(resourcePath)
                }
            }
        },
        "bis" to { commandArgs, event ->
            getBestInSlot(resourcePath, commandArgs, event)
        },
        "guides" to { _, event ->
            getGuide(resourcePath, event, botConfig.limit)
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
                .addField("${botConfig.prefix}schedule cancel <dagar>", "Avbokar dag/dagar", false)
                .addField("${botConfig.prefix}schedule add <dagar>", "Bokar ytterligare dagar", false)
                .addField("${botConfig.prefix}schedule clean", "Tar bort det gamla schemat", false)
                .addField("${botConfig.prefix}bis", "Visar ditt BIS-set, glöm inte klassnamnet [e.x: war]", false)
                .addField("${botConfig.prefix}guide", "Visar de olika guiderna vi brukar använda oss av", false)
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