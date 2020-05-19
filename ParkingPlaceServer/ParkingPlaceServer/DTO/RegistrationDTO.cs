using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	public class RegistrationDTO
	{
		public string Username { get; set; }

		public string Password { get; set; }

		public string RepeatPassword { get; set; }

		public string CarRegistrationNumber { get; set; }

		public RegistrationDTO()
		{

		}
	}
}