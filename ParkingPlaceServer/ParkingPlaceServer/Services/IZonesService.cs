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
		List<Zone> getZones();
		Zone getZone(long zoneId);
		List<Zone> getZones(long[] zoneIds);
		ParkingPlace GetParkingPlace(long parkingPlaceId);
	}
}