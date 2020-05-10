using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	public class TakingDTO : Dto
	{
		public TicketType TicketType { get; set; }

		public TakingDTO()
		{

		}
	}
}