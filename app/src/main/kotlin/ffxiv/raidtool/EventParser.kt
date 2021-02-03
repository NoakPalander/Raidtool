package ffxiv.raidtool

import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class EventParser(private val prefix: String) : ListenerAdapter() {
    lateinit var hooks: HashMap<String, (List<String>, e: TextChannel) -> Unit>

    // Someone joined the discord
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        event.guild.defaultChannel?.sendMessage("Var frälsad ${event.member.asMention}!")?.queue {
            it.addReaction("\uD83D\uDE03").queue()
        }
    }

    // Someone sent a message
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        // A valid bot message was sent
        if (event.message.contentRaw.startsWith(prefix) && event.message.contentRaw.length > 3) {
            val (command, args) = event.message.contentRaw.substring(prefix.length).split(" ").let {
                Pair(it.first(), it.drop(1))
            }

            if (hooks.containsKey(command))
                hooks[command]!!.invoke(args, event.channel)
        }
    }
}