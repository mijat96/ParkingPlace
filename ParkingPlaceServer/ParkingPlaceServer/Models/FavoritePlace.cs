using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Models
{
	public class FavoritePlace
	{
		[JsonProperty("id")]
		public long Id { get; set; }

		[JsonProperty("name")]
		public string Name { get; set; }

		[JsonProperty("type")]
		[JsonConverter(typeof(StringEnumConverter))]
		public FavoritePlaceType Type { get; set; }

		[JsonProperty("location")]
		public Location Location { get; set; }

		public FavoritePlace()
		{

		}
	}
}