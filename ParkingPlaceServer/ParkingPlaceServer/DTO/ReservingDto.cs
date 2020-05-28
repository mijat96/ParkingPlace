using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	public class ReservingDto: Dto
	{
		public double CurrentLocationLatitude { get; set; }

		public double CurrentLocationLongitude { get; set; }

		public ReservingDto()
		{

		}
	}
}