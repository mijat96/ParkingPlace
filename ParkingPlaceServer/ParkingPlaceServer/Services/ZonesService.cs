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


		public List<Zone> getZones()
		{
			return zoneDAO.getZones();
		}

		public Zone getZone(long id)
		{
			return zoneDAO.getZones()
					.Where(z => z.Id == id)
					.Single();
		}

		public List<Zone> getZones(long[] zoneIds)
		{
			return zoneDAO.getZones()
					.Where(z => zoneIds.Contains(z.Id))
					.ToList();
		}
	}
}