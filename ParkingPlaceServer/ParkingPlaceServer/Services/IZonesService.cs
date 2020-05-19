using ParkingPlaceServer.DTO;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Services
{
	public interface IZonesService
	{
		List<Zone> GetZones();

		Zone GetZone(long zoneId);
		
		List<Zone> GetZones(long[] zoneIds);
		
		ParkingPlace GetParkingPlace(long parkingPlaceId);
	}
}