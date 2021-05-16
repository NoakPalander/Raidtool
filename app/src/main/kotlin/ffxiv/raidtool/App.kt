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
        "shirk" to { _, e -> e.sendMessage("I provoke, you shirk... I have no shirk!") },
        "divination" to { _, e -> e.sendMessage("Divination is late..") },
        "source" to { _, e -> e.sendMessage("https://github.com/NoakPalander/Raidtool/blob/master/README.md") },
        "t0nk" to { _, e -> e.sendMessage("Did you mean blue dps?") },
        "tonk" to { _, e -> e.sendMessage("Did you mean blue dps?") },
        "paladin" to { _, e -> e.channel.sendMessage("Don't curse in front of Snowman!").queue {
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
                .setDescription("Different commandos for the raidtool bot")
                .addField("${botConfig.prefix}help", "Displays the help page", false)
                .addField("${botConfig.prefix}schedule new", "Removes the old and starts a new schedule", false)
                .addField("${botConfig.prefix}schedule set", "Assigns a new schedule, requires a date followed by a time", false)
                .addField("${botConfig.prefix}schedule when", "Displays the current schedule", false)
                .addField("${botConfig.prefix}schedule cancel <dagar>", "Unbooks a day/days", false)
                .addField("${botConfig.prefix}schedule add <dagar>", "Books additional days", false)
                .addField("${botConfig.prefix}schedule clean", "Removes the old schedule", false)
                .addField("${botConfig.prefix}bis", "Displays your BIS set, don't forget the class name [e.g: war]", false)
                .addField("${botConfig.prefix}guides", "Displays the guides we tend to use", false)
                .addField("${botConfig.prefix}source", "Displays the 'readme' file.", false)
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
                            builder.setTitle("Don't forget raid")
                            builder.setColor(Color.GREEN)
                            builder.setFooter("- Snowman's Angels")
                            data.booked.forEach { builder.addField(it.day, it.time, false) }
                            channel.sendMessage(builder.build()).queue { it.addReaction("❤").queue() }
                        }
                    }
                }
                catch(_: FileNotFoundException) {
                    e.channel.sendMessage("Couldn't find any raid sessions").queue {
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
