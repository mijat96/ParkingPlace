using Newtonsoft.Json;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	public class ZoneDTO
	{
		[JsonProperty("id")]
		public long Id { get; set; }

		[JsonProperty("name")]
		public string Name { get; set; }

		[JsonProperty("version")]
		public long Version { get; set; }

		[JsonProperty("northEast")]
		public Location NorthEast { get; set; }

		[JsonProperty("southWest")]
		public Location SouthWest { get; set; }

		[JsonProperty("parkingPlaces")]
		public List<ParkingPlace> ParkingPlaces { get; set; }

		[JsonProperty("ticketPrices")]
		public List<TicketPrice> TicketPrices { get; set; }

		public ZoneDTO ()
		{

		}


		public ZoneDTO(Zone zone)
		{
			Id = zone.Id;
			Name = zone.Name;
			Version = -1;
			NorthEast = zone.NorthEast;
			SouthWest = zone.SouthWest;
			TicketPrices = zone.TicketPrices;
			ParkingPlaces = new List<ParkingPlace>();
		}
	}
}