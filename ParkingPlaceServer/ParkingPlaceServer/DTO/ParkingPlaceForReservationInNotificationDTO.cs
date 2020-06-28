using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using ParkingPlaceServer.Models;

namespace ParkingPlaceServer.DTO
{
    public class ParkingPlaceForReservationInNotificationDTO
    {
		[JsonProperty("id")]
		public long Id { get; set; }

		[JsonProperty("location")]
		public Location Location { get; set; }

		[JsonProperty("status")]
		[JsonConverter(typeof(StringEnumConverter))]
		public ParkingPlaceStatus Status { get; set; }

		[JsonProperty("zone")]
		//[JsonIgnore]
		public Zone Zone { get; set; }


		public ParkingPlaceForReservationInNotificationDTO()
		{

		}

		public ParkingPlaceForReservationInNotificationDTO(ParkingPlace parkingPlace)
		{
			Id = parkingPlace.Id;
			Location = new Location(parkingPlace.Location);
			Status = parkingPlace.Status;
			Zone = new Zone(parkingPlace.Zone);
		}
	}
}