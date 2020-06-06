using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	public class PaidParkingPlaceDTO
	{
		[JsonProperty("id")]
		public long Id { get; set; }

		[JsonProperty("startDateTimeAndroid")]
		public string StartDateTimeAndroid { get; set; }

		[JsonProperty("startDateTimeServer")]
		public string StartDateTimeServer { get; set; }

		[JsonProperty("ticketType")]
		[JsonConverter(typeof(StringEnumConverter))]
		public TicketType TicketType { get; set; }

		[JsonProperty("arrogantUser")]
		public bool ArrogantUser { get; set; }

		[JsonProperty("parkingPlace")]
		public ParkingPlace ParkingPlace { get; set; }

		[JsonProperty("zoneId")]
		public long ZoneId { get; set; }

		public PaidParkingPlaceDTO()
		{

		}

		public PaidParkingPlaceDTO(PaidParkingPlace paidParkingPlace)
		{
			Id = paidParkingPlace.Id;
			StartDateTimeAndroid = paidParkingPlace.GetStartDateTimeAndroidString();
			StartDateTimeServer = paidParkingPlace.GetStartDateTimeServerString();
			TicketType = paidParkingPlace.TicketType;
			ArrogantUser = paidParkingPlace.ArrogantUser;
			ParkingPlace = paidParkingPlace.ParkingPlace;
			ZoneId = ParkingPlace.Zone.Id;
		}

	}
}