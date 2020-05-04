using Newtonsoft.Json;
using ParkingPlaceServer.DTO;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Models
{
	public class Zone
	{
		[JsonProperty("id")]
		public long Id { get; set; }

		[JsonProperty("name")]
		public string Name { get; set; }

		[JsonProperty("version")]
		public long Version { get; set; }

		[JsonProperty("parkingPlaces")]
		public List<ParkingPlace> ParkingPlaces { get; set; }

		[JsonIgnore]
		public List<ParkingPlaceDTO> ParkingPlaceChanges { get; set; }

		[JsonProperty("ticketPrices")]
		public List<TicketPrice> TicketPrices { get; set; }

		public Zone()
		{
			ParkingPlaceChanges = new List<ParkingPlaceDTO>();
			TicketPrices = new List<TicketPrice>();

		}

		public Zone(Zone zone)
		{
			Id = zone.Id;
			Name = zone.Name;
			Version = -1;
			ParkingPlaces = null;
			ParkingPlaceChanges = null;
			TicketPrices = new List<TicketPrice>();
			foreach (TicketPrice ticketPrice in zone.TicketPrices)
			{
				TicketPrices.Add(ticketPrice);
			}
		}

		public ParkingPlace getParkingPlace(long parkingPlaceId)
		{
			if (ParkingPlaces == null)
			{
				return null;
			}
			else
			{
				return ParkingPlaces.Where(pp => pp.Id == parkingPlaceId)
							.Single();
			}
		}

		public TicketPrice GetTicketPrice(TicketType ticketType)
		{
			foreach (TicketPrice ticketPrice in TicketPrices)
			{
				if (ticketPrice.TicketType == ticketType)
				{
					return ticketPrice;
				}
			}

			return null;
		}

	}
}