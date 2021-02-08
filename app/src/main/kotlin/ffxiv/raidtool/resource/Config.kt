package ffxiv.raidtool.resource

import com.fasterxml.jackson.annotation.JsonProperty

data class Config(
    @JsonProperty("id") val id: Long,
    @JsonProperty("secret") val secret: String,
    @JsonProperty("key") val key: String,
    @JsonProperty("token") val token: String,
    @JsonProperty("prefix") val prefix: String,
    @JsonProperty("snowman") val snowman: String,
    @JsonProperty("snowman2") val snowman2: String,
    @JsonProperty("war-chad") val warChad: String
)