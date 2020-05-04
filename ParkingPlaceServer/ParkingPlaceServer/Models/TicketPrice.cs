using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace ParkingPlaceServer.Models
{
	public class TicketPrice
	{
		[JsonProperty("duration")]
		public int Duration { get; set; } // hours

		[JsonProperty("ticketType")]
		[JsonConverter(typeof(StringEnumConverter))]
		public TicketType TicketType { get; set; }

		[JsonProperty("price")]
		public float Price { get; set; }

		public TicketPrice()
		{

		}
	}
}