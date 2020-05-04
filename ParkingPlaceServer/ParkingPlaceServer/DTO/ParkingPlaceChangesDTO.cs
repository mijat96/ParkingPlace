using Newtonsoft.Json;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	public class ParkingPlaceChangesDTO
	{

		[JsonProperty("zoneId")]
		public long ZoneId { get; set; }


		[JsonProperty("version")]
		public long Version { get; set; }


		[JsonProperty("parkingPlaceChanges")]
		public List<ParkingPlaceDTO> ParkingPlaceChanges { get; set; }


		public ParkingPlaceChangesDTO()
		{

		}

		public ParkingPlaceChangesDTO (long zoneId, long version, List<ParkingPlaceDTO> parkingPlaceChanges)
		{
			ZoneId = zoneId;
			Version = version;
			ParkingPlaceChanges = parkingPlaceChanges;
		}
	}
}