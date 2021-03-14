package ffxiv.raidtool

import com.fasterxml.jackson.annotation.JsonProperty

data class Config(
    @JsonProperty("id") val id: Long,
    @JsonProperty("admin") val admin: String,
    @JsonProperty("secret") val secret: String,
    @JsonProperty("key") val key: String,
    @JsonProperty("token") val token: String,
    @JsonProperty("prefix") val prefix: String,
    @JsonProperty("snowman") val snowman: String,
    @JsonProperty("snowman2") val snowman2: String,
    @JsonProperty("war-chad") val warChad: String,
    @JsonProperty("limit") val limit: String
)