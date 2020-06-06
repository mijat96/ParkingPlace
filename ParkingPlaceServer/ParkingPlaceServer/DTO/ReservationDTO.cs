using Newtonsoft.Json;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	public class ReservationDTO
	{
		[JsonProperty("id")]
		public long Id { get; set; }

		[JsonProperty("startDateTimeAndroid")]
		public string StartDateTimeAndroid { get; set; }

		[JsonProperty("startDateTimeServer")]
		public string StartDateTimeServer { get; set; }

		[JsonProperty("parkingPlace")]
		public ParkingPlace ParkingPlace { get; set; }

		[JsonProperty("zoneId")]
		public long ZoneId { get; set; }

		public ReservationDTO()
		{

		}

		public ReservationDTO(Reservation reservation)
		{
			Id = reservation.Id;
			StartDateTimeAndroid = reservation.GetStartDateTimeAndroidString();
			StartDateTimeServer = reservation.GetStartDateTimeServerString();
			ParkingPlace = reservation.ParkingPlace;
			ZoneId = ParkingPlace.Zone.Id;
		}

	}
}