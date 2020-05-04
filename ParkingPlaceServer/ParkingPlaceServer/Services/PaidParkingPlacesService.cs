using ParkingPlaceServer.DAO;
using ParkingPlaceServer.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace ParkingPlaceServer.Services
{
	public class PaidParkingPlacesService : IPaidParkingPlacesService
	{
		private static object lockObject = new object();

		private static PaidParkingPlacesService instance = null;

		public static PaidParkingPlacesService Instance
		{
			get
			{
				lock (lockObject)
				{
					if (instance == null)
					{
						instance = new PaidParkingPlacesService();
					}
				}


				return instance;
			}
		}

		private PaidParkingPlaceDAO paidParkingPlaceDAO;

		private PaidParkingPlacesService()
		{
			paidParkingPlaceDAO = new PaidParkingPlaceDAO();
		}

		public List<PaidParkingPlace> getPaidParkingPlaces()
		{
			return paidParkingPlaceDAO.getPaidParkingPlaces();
		}

		public void AddPaidParkingPlace(PaidParkingPlace paidParkingPlace)
		{
			paidParkingPlaceDAO.AddPaidParkingPlace(paidParkingPlace);
		}
	}
}