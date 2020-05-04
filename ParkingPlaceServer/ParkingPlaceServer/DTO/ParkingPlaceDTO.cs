using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.DTO
{
	public class ParkingPlaceDTO
	{

		[JsonProperty("id")]
		public long Id { get; set; }


		[JsonProperty("status")]
		[JsonConverter(typeof(StringEnumConverter))]
		public ParkingPlaceStatus Status { get; set; }
	
		
		public ParkingPlaceDTO()
		{

		}

		public ParkingPlaceDTO(long id, ParkingPlaceStatus status)
		{
			Id = id;
			Status = status;
		}
	}
}