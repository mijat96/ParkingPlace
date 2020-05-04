using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace ParkingPlaceServer.Models
{
	public class ParkingPlace
	{
		[JsonProperty("id")]
		public long Id { get; set; }

		[JsonProperty("location")]
		public Location Location { get; set; }

		[JsonProperty("status")]
		[JsonConverter(typeof(StringEnumConverter))]
		public ParkingPlaceStatus Status { get; set; }

		// [JsonProperty("zone")]
		[JsonIgnore]
		public Zone Zone { get; set; }


		public ParkingPlace()
		{
			
		}

		public ParkingPlace(ParkingPlace parkingPlace)
		{
			Id = parkingPlace.Id;
			Location = new Location(parkingPlace.Location);
			Status = parkingPlace.Status;
			Zone = new Zone(parkingPlace.Zone);
		}
	}
}