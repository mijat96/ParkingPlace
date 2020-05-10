using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	public class ParkingPlacesUpdatingDTO
	{
		[JsonProperty("changes")]
		public List<ParkingPlaceChangesDTO> Changes { get; set; }

		[JsonProperty("initials")]
		public List<ParkingPlacesInitialDTO> Initials { get; set; }

		public ParkingPlacesUpdatingDTO(List<ParkingPlaceChangesDTO> changes, List<ParkingPlacesInitialDTO> initials)
		{
			Changes = changes;
			Initials = initials;
		}
	}
}