using ParkingPlaceServer.DAO;
using ParkingPlaceServer.DTO;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Services
{
	public class ZonesService : IZonesService
	{
		private static object lockObject = new object();

		private static ZonesService instance = null;

		public static ZonesService Instance 
		{ 
			get 
			{
				lock (lockObject) {
					if (instance == null)
					{
						instance = new ZonesService();
					}
				}
				

				return instance;
			}
		}


		private ZoneDAO zoneDAO;

		private ZonesService()
		{
			zoneDAO = new ZoneDAO();
		}


		public List<Zone> GetZones()
		{
			return zoneDAO.GetZones();
		}

		public Zone GetZone(long id)
		{
			return zoneDAO.GetZones()
					.Where(z => z.Id == id)
					.Single();
		}

		public List<Zone> GetZones(long[] zoneIds)
		{
			return zoneDAO.GetZones()
					.Where(z => zoneIds.Contains(z.Id))
					.ToList();
		}

		public ParkingPlace GetParkingPlace(long parkingPlaceId)
		{
			List<Zone> zones = zoneDAO.GetZones();
			foreach(Zone zone in zones)
			{
				foreach(ParkingPlace pp in zone.ParkingPlaces)
				{
					if (pp.Id == parkingPlaceId)
					{
						return pp;
					}
				}
			}
			return null;
		}
	}
}