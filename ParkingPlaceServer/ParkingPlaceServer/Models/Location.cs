using Newtonsoft.Json;

namespace ParkingPlaceServer.Models
{
	public class Location
	{
		[JsonProperty("latitude")]
		public double Latitude { get; set; }

		[JsonProperty("longitude")]
		public double Longitude { get; set; }

		[JsonProperty("address")]
		public string Address { get; set; }


		public Location()
		{

		}

		public Location(Location location)
		{
			Latitude = location.Latitude;
			Longitude = location.Longitude;
			Address = location.Address;
		}
	}
}