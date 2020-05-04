using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	public class ReservationDTO
	{
		public long ZoneId { get; set; }
		public long ParkingPlaceId { get; set; }

		public ReservationDTO()
		{

		}
	}
}