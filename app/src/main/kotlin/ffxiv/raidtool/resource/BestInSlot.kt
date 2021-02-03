package ffxiv.raidtool.resource

import com.fasterxml.jackson.annotation.JsonProperty

data class BestInSlot(
    @JsonProperty("title") val title: String,
    @JsonProperty("url") val url: String,
    @JsonProperty("color") val color: List<Int>
)