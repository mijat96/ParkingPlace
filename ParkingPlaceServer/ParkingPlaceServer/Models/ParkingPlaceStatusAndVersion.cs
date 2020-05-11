using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Models
{
	public class ParkingPlaceStatusAndVersion
	{
		public long Id { get; set; }

		public ParkingPlaceStatus Status { get; set; }

		public long Version { get; set; }


		public ParkingPlaceStatusAndVersion(long id, ParkingPlaceStatus status, long version)
		{
			Id = id;
			Status = status;
			Version = version;
		}
	}
}