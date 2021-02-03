import com.fasterxml.jackson.annotation.JsonProperty

data class RaidPoint(
    @JsonProperty("day") val day: String,
    @JsonProperty("time") val time: String
)

data class Booked(@JsonProperty("booked") val booked: List<RaidPoint>)