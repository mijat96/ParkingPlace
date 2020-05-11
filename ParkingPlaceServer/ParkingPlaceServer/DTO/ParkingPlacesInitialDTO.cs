using Newtonsoft.Json;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	
	public class ParkingPlacesInitialDTO
	{

		[JsonProperty("zoneId")]
		public long ZoneId { get; set; }


		[JsonProperty("version")]
		public long Version { get; set; }


		[JsonProperty("parkingPlaces")]
		public List<ParkingPlace> ParkingPlaces { get; set; }


		public ParkingPlacesInitialDTO()
		{

		}

		public ParkingPlacesInitialDTO(long zoneId, long version, List<ParkingPlace> parkingPlaces)
		{
			ZoneId = zoneId;
			Version = version;
			ParkingPlaces = parkingPlaces;
		}
	}
}