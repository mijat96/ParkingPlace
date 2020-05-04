using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	public class TokenDTO
	{
		[JsonProperty("token")]
		public string Token { get; set; }


		public TokenDTO(string token)
		{
			Token = token;
		}
	}
}