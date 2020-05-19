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

		public List<PaidParkingPlace> GetPaidParkingPlaces()
		{
			return paidParkingPlaceDAO.GetPaidParkingPlaces();
		}

		public void AddPaidParkingPlace(PaidParkingPlace paidParkingPlace)
		{
			paidParkingPlaceDAO.AddPaidParkingPlace(paidParkingPlace);
		}

		public bool RemovePaidParkingPlace(User loggedUser, long parkingPlaceId)
		{
			if (loggedUser.RegularPaidParkingPlace == null)
			{
				return false;
			}

			if (loggedUser.RegularPaidParkingPlace.ParkingPlace.Id != parkingPlaceId)
			{
				return false;
			}

			List<PaidParkingPlace> paidParkingPlaces = paidParkingPlaceDAO.GetPaidParkingPlaces();
			lock(paidParkingPlaces)
			{
				if (paidParkingPlaces.Count == 0)
				{
					return false;
				}

				if (paidParkingPlaces.Contains(loggedUser.RegularPaidParkingPlace))
				{
					paidParkingPlaces.Remove(loggedUser.RegularPaidParkingPlace);
					loggedUser.RegularPaidParkingPlace = null;
					return true;
				}
				else
				{
					return false;
				}
			}
			
		}
	}
}