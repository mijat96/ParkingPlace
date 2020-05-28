using ParkingPlaceServer.DAO;
using ParkingPlaceServer.Models;
using ParkingPlaceServer.Utils;
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
		private static readonly double MAX_DISTANCE_CURRENT_LOCATION_TO_FAVORITE_PLACE = 100.0; // meters

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

		public bool CheckWheterIsParkingPlaceNearByFavoritePlace(List<FavoritePlace> favoritePlaces, 
																double currentLocationLatitude, 
																double currentLocationLongitude)
		{
			double distance;
			Location location;

			foreach (FavoritePlace favoritePlace in favoritePlaces)
			{
				location = favoritePlace.Location;
				distance = Distance.computeDistance(currentLocationLatitude, currentLocationLongitude,
													location.Latitude, location.Longitude);
				if (distance <= MAX_DISTANCE_CURRENT_LOCATION_TO_FAVORITE_PLACE)
				{
					return true;
				}
			}

			return false;
		}

	}
}