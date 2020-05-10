using Newtonsoft.Json;
using ParkingPlaceServer.Comparers;
using ParkingPlaceServer.DTO;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Models
{
	public class Zone
	{
		public static readonly int PARKING_PLACE_CHANGES_MAX_SIZE = 100;

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

		[JsonIgnore]
		public Dictionary<long, ParkingPlaceStatusAndVersion> ParkingPlaceChanges { get; set; }

		[JsonProperty("ticketPrices")]
		public List<TicketPrice> TicketPrices { get; set; }

		public Zone()
		{
			ParkingPlaceChanges = new Dictionary<long, ParkingPlaceStatusAndVersion>();
			TicketPrices = new List<TicketPrice>();

		}

		public Zone(Zone zone)
		{
			Id = zone.Id;
			Name = zone.Name;
			Version = -1;
			NorthEast = null;
			SouthWest = null;
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

		public void AddParkingPlaceChange(long id, ParkingPlaceStatus status)
		{
			ParkingPlaceChanges[id] = new ParkingPlaceStatusAndVersion(id, status, Version);
			if (ParkingPlaceChanges.Count > PARKING_PLACE_CHANGES_MAX_SIZE)
			{
				removeParkingPlaceChangesWithMinVersions();
			}
		}

		private void removeParkingPlaceChangesWithMinVersions()
		{
			int forRemovingCount = ParkingPlaceChanges.Count - PARKING_PLACE_CHANGES_MAX_SIZE;
			List<ParkingPlaceStatusAndVersion> values = ParkingPlaceChanges.Values.ToList();
			values.Sort(new ParkingPlaceStatusAndVersionComparer());
			foreach (ParkingPlaceStatusAndVersion p in values.GetRange(0, forRemovingCount)) {
				if (ParkingPlaceChanges.ContainsKey(p.Id))
				{
					ParkingPlaceChanges.Remove(p.Id);
				}
			}

		}


	}
}